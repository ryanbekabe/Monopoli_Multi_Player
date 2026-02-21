package com.hanyajasa.monopolimultiplayer.network

import android.util.Log
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.CopyOnWriteArrayList

class GameServer(private val port: Int = 8888) {

    private var serverSocket: ServerSocket? = null
    private val clients = CopyOnWriteArrayList<ClientHandler>()
    private var isRunning = false
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    var onMessageReceived: ((NetworkMessage) -> Unit)? = null

    fun start() {
        isRunning = true
        scope.launch {
            try {
                serverSocket = ServerSocket(port)
                Log.d("GameServer", "Server started on port $port")
                while (isRunning) {
                    val clientSocket = serverSocket?.accept()
                    if (clientSocket != null) {
                        val handler = ClientHandler(clientSocket)
                        clients.add(handler)
                        handler.start()
                    }
                }
            } catch (e: Exception) {
                Log.e("GameServer", "Error: ${e.message}")
            }
        }
    }

    fun stop() {
        isRunning = false
        serverSocket?.close()
        clients.forEach { it.stop() }
        clients.clear()
        scope.cancel()
    }

    fun broadcast(message: NetworkMessage) {
        val json = NetworkUtils.toJson(message)
        clients.forEach { it.send(json) }
    }

    inner class ClientHandler(private val socket: Socket) {
        private var reader: BufferedReader? = null
        private var writer: PrintWriter? = null
        private var isHandlerRunning = false

        fun start() {
            isHandlerRunning = true
            scope.launch {
                try {
                    reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                    writer = PrintWriter(socket.getOutputStream(), true)

                    while (isHandlerRunning) {
                        val line = reader?.readLine() ?: break
                        val message = NetworkUtils.fromJson(line)
                        onMessageReceived?.invoke(message)
                    }
                } catch (e: Exception) {
                    Log.e("GameServer", "Handler error: ${e.message}")
                } finally {
                    stop()
                }
            }
        }

        fun send(json: String) {
            scope.launch {
                writer?.println(json)
            }
        }

        fun stop() {
            isHandlerRunning = false
            try {
                socket.close()
            } catch (e: Exception) {}
            clients.remove(this)
        }
    }
}
