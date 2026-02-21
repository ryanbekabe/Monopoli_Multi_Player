package com.hanyajasa.monopolimultiplayer.model

object BoardProvider {
    fun createBoard(): List<Square> {
        val board = mutableListOf<Square>()

        // 0: GO
        board.add(Square(0, "GO", SquareType.GO, 0))

        // 1: Jakarta (Mediterranean)
        board.add(Square(1, "Jakarta", SquareType.PROPERTY, 1, 60, 2, listOf(10, 30, 90, 160, 250), 50, "BROWN"))

        // 2: Community Chest
        board.add(Square(2, "Community Chest", SquareType.COMMUNITY_CHEST, 2))

        // 3: Surabaya (Baltic)
        board.add(Square(3, "Surabaya", SquareType.PROPERTY, 3, 60, 4, listOf(20, 60, 180, 320, 450), 50, "BROWN"))

        // 4: Income Tax
        board.add(Square(4, "Income Tax", SquareType.TAX, 4, taxAmount = 200))

        // 5: Reading Railroad
        board.add(Square(5, "Stasiun Gambir", SquareType.RAILROAD, 5, 200, 25))

        // 6: Bandung (Oriental)
        board.add(Square(6, "Bandung", SquareType.PROPERTY, 6, 100, 6, listOf(30, 90, 270, 400, 550), 50, "LIGHTBLUE"))

        // 7: Chance
        board.add(Square(7, "Chance", SquareType.CHANCE, 7))

        // 8: Semarang (Vermont)
        board.add(Square(8, "Semarang", SquareType.PROPERTY, 8, 100, 6, listOf(30, 90, 270, 400, 550), 50, "LIGHTBLUE"))

        // 9: Medan (Connecticut)
        board.add(Square(9, "Medan", SquareType.PROPERTY, 9, 120, 8, listOf(40, 100, 300, 450, 600), 50, "LIGHTBLUE"))

        // 10: JAIL
        board.add(Square(10, "JAIL", SquareType.JAIL, 10))

        // 11: Makassar (St. Charles)
        board.add(Square(11, "Makassar", SquareType.PROPERTY, 11, 140, 10, listOf(50, 150, 450, 625, 750), 100, "PINK"))

        // 12: Electric Company
        board.add(Square(12, "Electric Company", SquareType.UTILITY, 12, 150, 0))

        // 13: Palembang (States Ave)
        board.add(Square(13, "Palembang", SquareType.PROPERTY, 13, 140, 10, listOf(50, 150, 450, 625, 750), 100, "PINK"))

        // 14: Balikpapan (Virginia)
        board.add(Square(14, "Balikpapan", SquareType.PROPERTY, 14, 160, 12, listOf(60, 180, 500, 700, 900), 100, "PINK"))

        // 15: Pennsylvania Railroad
        board.add(Square(15, "Stasiun Senen", SquareType.RAILROAD, 15, 200, 25))

        // 16: Denpasar (St. James)
        board.add(Square(16, "Denpasar", SquareType.PROPERTY, 16, 180, 14, listOf(70, 200, 550, 750, 950), 100, "ORANGE"))

        // 17: Community Chest
        board.add(Square(17, "Community Chest", SquareType.COMMUNITY_CHEST, 17))

        // 18: Malang (Tennessee)
        board.add(Square(18, "Malang", SquareType.PROPERTY, 18, 180, 14, listOf(70, 200, 550, 750, 950), 100, "ORANGE"))

        // 19: Manado (New York)
        board.add(Square(19, "Manado", SquareType.PROPERTY, 19, 200, 16, listOf(80, 220, 600, 800, 1000), 100, "ORANGE"))

        // 20: FREE PARKING
        board.add(Square(20, "FREE PARKING", SquareType.FREE_PARKING, 20))

        // 21: Solo (Kentucky)
        board.add(Square(21, "Solo", SquareType.PROPERTY, 21, 220, 18, listOf(90, 250, 700, 875, 1050), 150, "RED"))

        // 22: Chance
        board.add(Square(22, "Chance", SquareType.CHANCE, 22))

        // 23: Yogyakarta (Indiana)
        board.add(Square(23, "Yogyakarta", SquareType.PROPERTY, 23, 220, 18, listOf(90, 250, 700, 875, 1050), 150, "RED"))

        // 24: Pontianak (Illinois)
        board.add(Square(24, "Pontianak", SquareType.PROPERTY, 24, 240, 20, listOf(100, 300, 750, 925, 1100), 150, "RED"))

        // 25: B. & O. Railroad
        board.add(Square(25, "Stasiun Lempuyangan", SquareType.RAILROAD, 25, 200, 25))

        // 26: Samarinda (Atlantic)
        board.add(Square(26, "Samarinda", SquareType.PROPERTY, 26, 260, 22, listOf(110, 330, 800, 975, 1150), 150, "YELLOW"))

        // 27: Banjarmasin (Ventnor)
        board.add(Square(27, "Banjarmasin", SquareType.PROPERTY, 27, 260, 22, listOf(110, 330, 800, 975, 1150), 150, "YELLOW"))

        // 28: Water Works
        board.add(Square(28, "Water Works", SquareType.UTILITY, 28, 150, 0))

        // 29: Ambon (Marvin Gardens)
        board.add(Square(29, "Ambon", SquareType.PROPERTY, 29, 280, 24, listOf(120, 360, 850, 1025, 1200), 150, "YELLOW"))

        // 30: GO TO JAIL
        board.add(Square(30, "GO TO JAIL", SquareType.GO_TO_JAIL, 30))

        // 31: Jayapura (Pacific)
        board.add(Square(31, "Jayapura", SquareType.PROPERTY, 31, 300, 26, listOf(130, 390, 900, 1100, 1275), 200, "GREEN"))

        // 32: Mataram (North Carolina)
        board.add(Square(32, "Mataram", SquareType.PROPERTY, 32, 300, 26, listOf(130, 390, 900, 1100, 1275), 200, "GREEN"))

        // 33: Community Chest
        board.add(Square(33, "Community Chest", SquareType.COMMUNITY_CHEST, 33))

        // 34: Kupang (Pennsylvania Ave)
        board.add(Square(34, "Kupang", SquareType.PROPERTY, 34, 320, 28, listOf(150, 450, 1000, 1200, 1400), 200, "GREEN"))

        // 35: Short Line
        board.add(Square(35, "Stasiun Gubeng", SquareType.RAILROAD, 35, 200, 25))

        // 36: Chance
        board.add(Square(36, "Chance", SquareType.CHANCE, 36))

        // 37: Batam (Park Place)
        board.add(Square(37, "Batam", SquareType.PROPERTY, 37, 350, 35, listOf(175, 500, 1100, 1300, 1500), 200, "DARKBLUE"))

        // 38: Luxury Tax
        board.add(Square(38, "Luxury Tax", SquareType.TAX, 38, taxAmount = 100))

        // 39: IKN (Boardwalk)
        board.add(Square(39, "IKN", SquareType.PROPERTY, 39, 400, 50, listOf(200, 600, 1400, 1700, 2000), 200, "DARKBLUE"))

        return board
    }
}
