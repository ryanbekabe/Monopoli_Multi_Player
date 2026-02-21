package com.hanyajasa.monopolimultiplayer.model

object BoardProvider {
    fun createBoard(): List<Square> {
        val board = mutableListOf<Square>()

        // 0: GO
        board.add(Square(0, "GO", SquareType.GO, 0))

        // 1: Mediterranean Avenue / Jakarta
        board.add(Square(1, "Jakarta", SquareType.PROPERTY, 1, 60, 2, "BROWN"))

        // 2: Community Chest
        board.add(Square(2, "Community Chest", SquareType.COMMUNITY_CHEST, 2))

        // 3: Baltic Avenue / Surabaya
        board.add(Square(3, "Surabaya", SquareType.PROPERTY, 3, 60, 4, "BROWN"))

        // 4: Income Tax
        board.add(Square(4, "Income Tax", SquareType.TAX, 4, taxAmount = 200))

        // 5: Reading Railroad
        board.add(Square(5, "Stasiun Gambir", SquareType.RAILROAD, 5, 200, 25))

        // 6: Oriental Avenue / Bandung
        board.add(Square(6, "Bandung", SquareType.PROPERTY, 6, 100, 6, "LIGHTBLUE"))

        // 7: Chance
        board.add(Square(7, "Chance", SquareType.CHANCE, 7))

        // 8: Vermont Avenue / Semarang
        board.add(Square(8, "Semarang", SquareType.PROPERTY, 8, 100, 6, "LIGHTBLUE"))

        // 9: Connecticut Avenue / Medan
        board.add(Square(9, "Medan", SquareType.PROPERTY, 9, 120, 8, "LIGHTBLUE"))

        // 10: JAIL
        board.add(Square(10, "JAIL", SquareType.JAIL, 10))

        // 11: St. Charles Place / Makassar
        board.add(Square(11, "Makassar", SquareType.PROPERTY, 11, 140, 10, "PINK"))

        // 12: Electric Company
        board.add(Square(12, "Electric Company", SquareType.UTILITY, 12, 150, 0))

        // 13: States Avenue / Palembang
        board.add(Square(13, "Palembang", SquareType.PROPERTY, 13, 140, 10, "PINK"))

        // 14: Virginia Avenue / Balikpapan
        board.add(Square(14, "Balikpapan", SquareType.PROPERTY, 14, 160, 12, "PINK"))

        // 15: Pennsylvania Railroad
        board.add(Square(15, "Stasiun Senen", SquareType.RAILROAD, 15, 200, 25))

        // 16: St. James Place / Denpasar
        board.add(Square(16, "Denpasar", SquareType.PROPERTY, 16, 180, 14, "ORANGE"))

        // 17: Community Chest
        board.add(Square(17, "Community Chest", SquareType.COMMUNITY_CHEST, 17))

        // 18: Tennessee Avenue / Malang
        board.add(Square(18, "Malang", SquareType.PROPERTY, 18, 180, 14, "ORANGE"))

        // 19: New York Avenue / Manado
        board.add(Square(19, "Manado", SquareType.PROPERTY, 19, 200, 16, "ORANGE"))

        // 20: FREE PARKING
        board.add(Square(20, "FREE PARKING", SquareType.FREE_PARKING, 20))

        // 21: Kentucky Avenue / Solo
        board.add(Square(21, "Solo", SquareType.PROPERTY, 21, 220, 18, "RED"))

        // 22: Chance
        board.add(Square(22, "Chance", SquareType.CHANCE, 22))

        // 23: Indiana Avenue / Yogyakarta
        board.add(Square(23, "Yogyakarta", SquareType.PROPERTY, 23, 220, 18, "RED"))

        // 24: Illinois Avenue / Pontianak
        board.add(Square(24, "Pontianak", SquareType.PROPERTY, 24, 240, 20, "RED"))

        // 25: B. & O. Railroad
        board.add(Square(25, "Stasiun Lempuyangan", SquareType.RAILROAD, 25, 200, 25))

        // 26: Atlantic Avenue / Samarinda
        board.add(Square(26, "Samarinda", SquareType.PROPERTY, 26, 260, 22, "YELLOW"))

        // 27: Ventnor Avenue / Banjarmasin
        board.add(Square(27, "Banjarmasin", SquareType.PROPERTY, 27, 260, 22, "YELLOW"))

        // 28: Water Works
        board.add(Square(28, "Water Works", SquareType.UTILITY, 28, 150, 0))

        // 29: Marvin Gardens / Ambon
        board.add(Square(29, "Ambon", SquareType.PROPERTY, 29, 280, 24, "YELLOW"))

        // 30: GO TO JAIL
        board.add(Square(30, "GO TO JAIL", SquareType.GO_TO_JAIL, 30))

        // 31: Pacific Avenue / Jayapura
        board.add(Square(31, "Jayapura", SquareType.PROPERTY, 31, 300, 26, "GREEN"))

        // 32: North Carolina Avenue / Mataram
        board.add(Square(32, "Mataram", SquareType.PROPERTY, 32, 300, 26, "GREEN"))

        // 33: Community Chest
        board.add(Square(33, "Community Chest", SquareType.COMMUNITY_CHEST, 33))

        // 34: Pennsylvania Avenue / Kupang
        board.add(Square(34, "Kupang", SquareType.PROPERTY, 34, 320, 28, "GREEN"))

        // 35: Short Line
        board.add(Square(35, "Stasiun Gubeng", SquareType.RAILROAD, 35, 200, 25))

        // 36: Chance
        board.add(Square(36, "Chance", SquareType.CHANCE, 36))

        // 37: Park Place / Batam
        board.add(Square(37, "Batam", SquareType.PROPERTY, 37, 350, 35, "DARKBLUE"))

        // 38: Luxury Tax
        board.add(Square(38, "Luxury Tax", SquareType.TAX, 38, taxAmount = 100))

        // 39: Boardwalk / IKN
        board.add(Square(39, "IKN", SquareType.PROPERTY, 39, 400, 50, "DARKBLUE"))

        return board
    }
}
