package com.hanyajasa.monopolimultiplayer.model

enum class SquareType {
    PROPERTY, RAILROAD, UTILITY, CHANCE, COMMUNITY_CHEST, TAX, GO, JAIL, FREE_PARKING, GO_TO_JAIL
}

// Making it a concrete data class for easier serialization/deserialization with Gson
data class Square(
    val id: Int,
    val name: String,
    val type: SquareType,
    val position: Int,
    // Property specific fields (optional/nullable)
    val price: Int = 0,
    val rent: Int = 0,
    val rentLevels: List<Int> = emptyList(), // Rent with 1, 2, 3, 4 houses and 1 hotel
    val housePrice: Int = 0,
    val colorGroup: String? = null,
    var ownerId: String? = null,
    var houses: Int = 0, // 5 houses = 1 hotel
    var isMortgaged: Boolean = false,
    // Special square fields
    val taxAmount: Int = 0
)
