package com.hanyajasa.monopolimultiplayer.network

import com.hanyajasa.monopolimultiplayer.model.GameState

enum class MessageType {
    JOIN,           // Client wants to join
    SYNC_STATE,     // Server sends current state to all
    ROLL_DICE,      // Client rolls dice
    BUY_PROPERTY,   // Client buys property
    END_TURN,       // Client ends turn
    CHAT,           // Chat message
    START_GAME      // Host starts game
}

data class NetworkMessage(
    val type: MessageType,
    val senderId: String? = null,
    val content: String? = null, // JSON content depending on type
    val gameState: GameState? = null
)
