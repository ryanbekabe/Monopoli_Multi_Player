package com.hanyajasa.monopolimultiplayer.network

import android.util.Log
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class GameClient {

    private var socket: Socket? = null
    private var reader: BufferedReader? = null
    private var writer: PrintWriter? = null
    private var isRunning = false
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    var onMessageReceived: ((NetworkMessage) -> Unit)? = null

    fun connect(host: String, port: Int = 8888) {
        isRunning = true
        scope.launch {
            try {
                socket = Socket(host, port)
                reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
                writer = PrintWriter(socket!!.getOutputStream(), true)

                while (isRunning) {
                    val line = reader?.readLine() ?: break
                    val message = NetworkUtils.fromJson(line)
                    onMessageReceived?.invoke(message)
                }
            } catch (e: Exception) {
                Log.e("GameClient", "Error: ${e.message}")
            } finally {
                stop()
            }
        }
    }

    fun send(message: NetworkMessage) {
        scope.launch {
            val json = NetworkUtils.toJson(message)
            writer?.println(json)
        }
    }

    fun stop() {
        isRunning = false
        try {
            socket?.close()
        } catch (e: Exception) {}
        scope.cancel()
    }
}
