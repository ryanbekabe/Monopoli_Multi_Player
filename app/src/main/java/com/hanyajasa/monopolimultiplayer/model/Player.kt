package com.hanyajasa.monopolimultiplayer.model

data class Player(
    val id: String,
    val name: String,
    var position: Int = 0,
    var balance: Int = 1500,
    var isInJail: Boolean = false,
    var jailTurns: Int = 0,
    val color: Int // Color resource or hex
)
