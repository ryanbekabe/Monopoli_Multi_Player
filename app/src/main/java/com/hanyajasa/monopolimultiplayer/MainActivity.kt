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
import android.media.AudioManager
import android.media.ToneGenerator

class MainActivity : AppCompatActivity() {

    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    
    private fun playDiceSound() {
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
    }
    
    private fun playMoneySound() {
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 200)
    }

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

        setupEmojiBar()

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
                MessageType.EMOJI -> {
                    boardView.showEmoji(message.senderId!!, message.content!!)
                    if (isHost) {
                        gameServer?.broadcast(message) // Forward to others
                    }
                }
                MessageType.AUCTION -> {
                    handleAuctionMessage(message)
                }
                else -> {}
            }
        }
    }

    private fun setupEmojiBar() {
        val emos = listOf(R.id.emo1, R.id.emo2, R.id.emo3, R.id.emo4, R.id.emo5)
        emos.forEach { id ->
            findViewById<TextView>(id).setOnClickListener { v ->
                val emoji = (v as TextView).text.toString()
                boardView.showEmoji(myPlayerId, emoji)
                val msg = NetworkMessage(MessageType.EMOJI, myPlayerId, content = emoji)
                if (isHost) gameServer?.broadcast(msg) else gameClient?.send(msg)
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
        playDiceSound()

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

    private fun drawCard(player: Player, type: CardType) {
        if (!isHost) return // Only host decides the card for synchronization

        val deck = if (type == CardType.CHANCE) CardProvider.getChanceCards() else CardProvider.getCommunityChestCards()
        val card = deck.random()
        
        val actionResult = card.action(player, gameState)
        
        log("KARTU: ${card.description}")
        log(actionResult)
        
        syncAfterAction()
        updateUI()
    }

    private fun handleLanding(player: Player) {
        val square = gameState.squares[player.position]
        when (square.type) {
            SquareType.PROPERTY, SquareType.RAILROAD, SquareType.UTILITY -> {
                if (square.ownerId == null) {
                    showBuyDialog(square, player)
                } else if (square.ownerId == player.id) {
                    // Check if player can build
                    if (hasAllPropertiesInGroup(player.id, square.colorGroup) && square.houses < 5) {
                        showBuildDialog(square, player)
                    }
                } else if (square.ownerId != player.id) {
                    // Pay rent
                    var rent = square.rent
                    if (square.houses > 0 && square.rentLevels.size >= square.houses) {
                        rent = square.rentLevels[square.houses - 1]
                    } else if (square.houses == 0) {
                        // In monopoly, owning all properties of a color group doubles rent with no houses
                        val hasAll = hasAllPropertiesInGroup(square.ownerId!!, square.colorGroup)
                        if (hasAll && square.type == SquareType.PROPERTY) {
                            rent *= 2
                        }
                    }
                    
                    player.balance -= rent
                    val owner = gameState.players.find { it.id == square.ownerId }
                    owner?.balance = (owner?.balance ?: 0) + rent
                    log("${player.name} membayar sewa $rent ke ${owner?.name ?: "Bank"}")
                    playMoneySound()
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
            SquareType.CHANCE -> {
                drawCard(player, CardType.CHANCE)
            }
            SquareType.COMMUNITY_CHEST -> {
                drawCard(player, CardType.COMMUNITY_CHEST)
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
                    playMoneySound()
                    syncAfterAction()
                    updateUI()
                }
                .setNegativeButton("Lelang") { _, _ ->
                    if (isHost) startAuction(square)
                    else gameClient?.send(NetworkMessage(MessageType.AUCTION, myPlayerId, content = "START:${square.id}"))
                }
                .show()
        }
    }

    private var currentAuctionSquare: Square? = null
    private var highestBid = 0
    private var highestBidderId: String? = null

    private fun startAuction(square: Square) {
        currentAuctionSquare = square
        highestBid = square.price / 2
        highestBidderId = null
        val msg = NetworkMessage(MessageType.AUCTION, myPlayerId, content = "OPEN:${square.id}:$highestBid")
        gameServer?.broadcast(msg)
        showAuctionDialog(square, highestBid, null)
    }

    private var auctionDialog: AlertDialog? = null

    private fun handleAuctionMessage(message: NetworkMessage) {
        val parts = message.content?.split(":") ?: return
        when (parts[0]) {
            "OPEN" -> {
                val squareId = parts[1].toInt()
                val startPrice = parts[2].toInt()
                val square = gameState.squares.find { it.id == squareId } ?: return
                showAuctionDialog(square, startPrice, null)
            }
            "BID" -> {
                val bidAmount = parts[1].toInt()
                if (bidAmount > highestBid) {
                    highestBid = bidAmount
                    highestBidderId = message.senderId
                    val bidderName = gameState.players.find { it.id == highestBidderId }?.name ?: "Pemain"
                    updateAuctionDialog(bidAmount, bidderName)
                    if (isHost) {
                        gameServer?.broadcast(NetworkMessage(MessageType.AUCTION, highestBidderId, content = "UPDATE:$highestBid:$bidderName"))
                    }
                }
            }
            "UPDATE" -> {
                val bid = parts[1].toInt()
                val name = parts[2]
                updateAuctionDialog(bid, name)
            }
            "WIN" -> {
                val winnerId = parts[1]
                val squareId = parts[2].toInt()
                val finalPrice = parts[3].toInt()
                val square = gameState.squares.find { it.id == squareId } ?: return
                val winner = gameState.players.find { it.id == winnerId } ?: return
                
                square.ownerId = winnerId
                winner.balance -= finalPrice
                log("${winner.name} memenangkan lelang ${square.name} seharga $finalPrice!")
                auctionDialog?.dismiss()
                updateUI()
            }
            "START" -> { // Client requesting host to start auction
                if (isHost) {
                    val square = gameState.squares.find { it.id == parts[1].toInt() } ?: return
                    startAuction(square)
                }
            }
        }
    }

    private fun showAuctionDialog(square: Square, currentBid: Int, bidderName: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("LELANG: ${square.name}")
        val name = bidderName ?: "Belum ada"
        builder.setMessage("Harga saat ini: $$currentBid\nPenawar tertinggi: $name")
        
        builder.setPositiveButton("Tawar +$10") { _, _ ->
            val myBid = highestBid + 10
            val me = gameState.players.find { it.id == myPlayerId }
            if (me != null && me.balance >= myBid) {
                val msg = NetworkMessage(MessageType.AUCTION, myPlayerId, content = "BID:$myBid")
                if (isHost) handleAuctionMessage(msg) else gameClient?.send(msg)
            } else {
                Toast.makeText(this, "Uang tidak cukup!", Toast.LENGTH_SHORT).show()
            }
        }
        
        if (isHost) {
            builder.setNeutralButton("Selesaikan Lelang") { _, _ ->
                if (highestBidderId != null) {
                    val msg = NetworkMessage(MessageType.AUCTION, highestBidderId, content = "WIN:$highestBidderId:${square.id}:$highestBid")
                    gameServer?.broadcast(msg)
                    handleAuctionMessage(msg)
                } else {
                    log("Lelang ${square.name} dibatalkan (tidak ada penawar).")
                }
            }
        }

        builder.setCancelable(false)
        auctionDialog = builder.create()
        auctionDialog?.show()
    }

    private fun updateAuctionDialog(bid: Int, bidderName: String) {
        highestBid = bid
        runOnUiThread {
            auctionDialog?.setMessage("Harga saat ini: $$bid\nPenawar tertinggi: $bidderName")
        }
    }

    private fun showBuildDialog(square: Square, player: Player) {
        val type = if (square.houses < 4) "Rumah" else "Hotel"
        if (player.balance >= square.housePrice) {
            AlertDialog.Builder(this)
                .setTitle("Bangun $type?")
                .setMessage("Apakah kamu ingin membangun $type di ${square.name} seharga $${square.housePrice}?")
                .setPositiveButton("Bangun") { _, _ ->
                    square.houses++
                    player.balance -= square.housePrice
                    log("${player.name} membangun $type di ${square.name}")
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

    private fun hasAllPropertiesInGroup(playerId: String, colorGroup: String?): Boolean {
        if (colorGroup == null) return false
        val groupProperties = gameState.squares.filter { it.colorGroup == colorGroup }
        return groupProperties.all { it.ownerId == playerId }
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