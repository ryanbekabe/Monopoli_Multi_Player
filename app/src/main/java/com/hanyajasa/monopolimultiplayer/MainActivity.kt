package com.hanyajasa.monopolimultiplayer

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.hanyajasa.monopolimultiplayer.model.*
import com.hanyajasa.monopolimultiplayer.network.*
import com.hanyajasa.monopolimultiplayer.ui.MonopolyBoardView
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var boardView: MonopolyBoardView
    private lateinit var btnRollDice: Button
    private lateinit var btnEndTurn: Button
    private lateinit var btnHost: Button
    private lateinit var btnJoin: Button
    private lateinit var txtGameLog: TextView
    private lateinit var txtPlayerStats: TextView

    private var gameServer: GameServer? = null
    private var gameClient: GameClient? = null
    private var networkDiscovery: NetworkDiscovery? = null

    private var gameState = GameState()
    private var myPlayerId: String = UUID.randomUUID().toString()

    private var isHost = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        boardView = findViewById(R.id.boardView)
        btnRollDice = findViewById(R.id.btnRollDice)
        btnEndTurn = findViewById(R.id.btnEndTurn)
        btnHost = findViewById(R.id.btnHost)
        btnJoin = findViewById(R.id.btnJoin)
        txtGameLog = findViewById(R.id.txtGameLog)
        txtPlayerStats = findViewById(R.id.txtPlayerStats)

        txtGameLog.movementMethod = android.text.method.ScrollingMovementMethod()

        boardView.setSquares(gameState.squares)
        updateUI()

        btnHost.setOnClickListener { startHosting() }
        btnJoin.setOnClickListener { startJoining() }
        btnRollDice.setOnClickListener { rollDice() }
        btnEndTurn.setOnClickListener { endTurn() }

        networkDiscovery = NetworkDiscovery(this)
    }

    private fun startHosting() {
        isHost = true
        gameServer = GameServer().apply {
            onMessageReceived = { message -> handleIncomingMessage(message) }
            start()
        }
        networkDiscovery?.registerService(8888)
        
        // Add myself as first player
        val hostPlayer = Player(myPlayerId, "Host (Me)", color = Color.BLUE)
        gameState.players.add(hostPlayer)
        
        btnHost.visibility = View.GONE
        btnJoin.visibility = View.GONE
        log("Server dimulai. Menunggu pemain lain...")
        updateUI()
    }

    private fun startJoining() {
        isHost = false
        networkDiscovery?.onServiceFound = { host, port ->
            runOnUiThread {
                Toast.makeText(this, "Game ditemukan: $host", Toast.LENGTH_SHORT).show()
                connectToServer(host, port)
            }
            networkDiscovery?.stopDiscovery()
        }
        networkDiscovery?.discoverServices()
        btnHost.visibility = View.GONE
        btnJoin.visibility = View.GONE
        log("Mencari game di jaringan LAN...")
    }

    private fun connectToServer(host: String, port: Int) {
        gameClient = GameClient().apply {
            onMessageReceived = { message -> handleIncomingMessage(message) }
            connect(host, port)
        }
        // Send JOIN message
        val joinMsg = NetworkMessage(MessageType.JOIN, myPlayerId, content = "Pemain Client")
        gameClient?.send(joinMsg)
    }

    private fun handleIncomingMessage(message: NetworkMessage) {
        runOnUiThread {
            when (message.type) {
                MessageType.JOIN -> {
                    if (isHost) {
                        val newPlayer = Player(message.senderId!!, message.content ?: "Guest", color = getNextPlayerColor())
                        gameState.players.add(newPlayer)
                        broadcastState()
                        log("${newPlayer.name} bergabung!")
                    }
                }
                MessageType.SYNC_STATE -> {
                    gameState = message.gameState!!
                    boardView.setSquares(gameState.squares) // Update board view with synced squares
                    if (isHost) {
                        broadcastState() // Redistribute state to all clients
                    }
                    updateUI()
                }
                else -> {}
            }
        }
    }

    private fun getNextPlayerColor(): Int {
        val colors = listOf(Color.RED, Color.GREEN, Color.YELLOW, Color.CYAN, Color.MAGENTA)
        return colors[gameState.players.size % colors.size]
    }

    private fun broadcastState() {
        if (isHost) {
            gameServer?.broadcast(NetworkMessage(MessageType.SYNC_STATE, gameState = gameState))
        }
    }

    private fun rollDice() {
        val currentPlayer = gameState.getCurrentPlayer() ?: return
        if (currentPlayer.id != myPlayerId) {
            Toast.makeText(this, "Bukan giliranmu!", Toast.LENGTH_SHORT).show()
            return
        }

        val d1 = (1..6).random()
        val d2 = (1..6).random()
        gameState.lastDiceRoll = Pair(d1, d2)

        val oldPos = currentPlayer.position
        currentPlayer.position = (currentPlayer.position + d1 + d2) % 40
        
        log("${currentPlayer.name} mengocok dadu: $d1 + $d2. Pindah ke ${gameState.squares[currentPlayer.position].name}")

        // Check for passing GO
        if (currentPlayer.position < oldPos) {
            currentPlayer.balance += 200
            log("${currentPlayer.name} melewati GO! +$200")
        }

        handleLanding(currentPlayer)

        btnRollDice.isEnabled = false
        btnEndTurn.isEnabled = true
        
        if (isHost) {
            broadcastState()
        } else {
            gameClient?.send(NetworkMessage(MessageType.SYNC_STATE, myPlayerId, gameState = gameState))
        }
        updateUI()
    }

    private fun handleLanding(player: Player) {
        val square = gameState.squares[player.position]
        when (square.type) {
            SquareType.PROPERTY, SquareType.RAILROAD, SquareType.UTILITY -> {
                if (square.ownerId == null) {
                    showBuyDialog(square, player)
                } else if (square.ownerId != player.id) {
                    // Pay rent
                    val rent = square.rent
                    player.balance -= rent
                    val owner = gameState.players.find { it.id == square.ownerId }
                    owner?.balance = (owner?.balance ?: 0) + rent
                    log("${player.name} membayar sewa $rent ke ${owner?.name ?: "Bank"}")
                }
            }
            SquareType.TAX -> {
                player.balance -= square.taxAmount
                log("${player.name} membayar pajak ${square.taxAmount}")
            }
            SquareType.GO_TO_JAIL -> {
                player.position = 10
                player.isInJail = true
                log("${player.name} masuk PENJARA!")
            }
            else -> {}
        }
    }

    private fun showBuyDialog(square: Square, player: Player) {
        if (player.balance >= square.price) {
            AlertDialog.Builder(this)
                .setTitle("Beli Properti?")
                .setMessage("Apakah kamu ingin membeli ${square.name} seharga $${square.price}?")
                .setPositiveButton("Beli") { _, _ ->
                    square.ownerId = player.id
                    player.balance -= square.price
                    log("${player.name} membeli ${square.name}")
                    syncAfterAction()
                    updateUI()
                }
                .setNegativeButton("Lewati", null)
                .show()
        }
    }

    private fun syncAfterAction() {
        if (isHost) broadcastState()
        else gameClient?.send(NetworkMessage(MessageType.SYNC_STATE, myPlayerId, gameState = gameState))
    }

    private fun endTurn() {
        gameState.nextTurn()
        btnRollDice.isEnabled = true
        btnEndTurn.isEnabled = false
        
        log("Giliran ${gameState.getCurrentPlayer()?.name}")
        
        syncAfterAction()
        updateUI()
    }

    private fun updateUI() {
        boardView.setPlayers(gameState.players)
        val me = gameState.players.find { it.id == myPlayerId }
        txtPlayerStats.text = "Uang: $${me?.balance ?: 1500}"
        
        val currentPlayer = gameState.getCurrentPlayer()
        btnRollDice.isEnabled = (currentPlayer?.id == myPlayerId && !btnEndTurn.isEnabled)
    }

    private fun log(message: String) {
        runOnUiThread {
            val currentText = txtGameLog.text.toString()
            txtGameLog.text = "$message\n$currentText"
            boardView.setCenterMessage(message) // Also show last action in center of board
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        gameServer?.stop()
        gameClient?.stop()
        networkDiscovery?.unregisterService()
        networkDiscovery?.stopDiscovery()
    }
}