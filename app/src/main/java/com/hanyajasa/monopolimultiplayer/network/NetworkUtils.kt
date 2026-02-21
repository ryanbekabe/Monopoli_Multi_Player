package com.hanyajasa.monopolimultiplayer.network

import com.google.gson.Gson

object NetworkUtils {
    private val gson = Gson()

    fun toJson(message: NetworkMessage): String {
        return gson.toJson(message) + "\n" // Newline as message delimiter
    }

    fun fromJson(json: String): NetworkMessage {
        return gson.fromJson(json, NetworkMessage::class.java)
    }
}
