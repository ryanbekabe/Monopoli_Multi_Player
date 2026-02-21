package com.hanyajasa.monopolimultiplayer.model

data class GameState(
    val players: MutableList<Player> = mutableListOf(),
    var currentPlayerIndex: Int = 0,
    val squares: MutableList<Square> = BoardProvider.createBoard().toMutableList(),
    var isGameOver: Boolean = false,
    var lastDiceRoll: Pair<Int, Int> = Pair(0, 0)
) {
    fun getCurrentPlayer(): Player? {
        return if (players.isNotEmpty()) players[currentPlayerIndex] else null
    }

    fun nextTurn() {
        if (players.isNotEmpty()) {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size
        }
    }
}
