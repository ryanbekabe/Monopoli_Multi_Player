package com.hanyajasa.monopolimultiplayer.model

enum class CardType {
    CHANCE, COMMUNITY_CHEST
}

data class Card(
    val type: CardType,
    val description: String,
    val action: (Player, GameState) -> String // Returns a message about what happened
)

object CardProvider {
    fun getChanceCards(): List<Card> {
        return listOf(
            Card(CardType.CHANCE, "Majukan ke IKN (Boardwalk)") { player, state ->
                player.position = 39
                "Anda mendapat tiket gratis ke IKN!"
            },
            Card(CardType.CHANCE, "Kembali ke Jakarta (Mediterranean)") { player, state ->
                player.position = 1
                "Rindu Jakarta? Silakan kembali."
            },
            Card(CardType.CHANCE, "Menerima Deviden Bank $50") { player, state ->
                player.balance += 50
                "Bank bagi-bagi untung! +$50"
            },
            Card(CardType.CHANCE, "Bayar Denda Tilang $15") { player, state ->
                player.balance -= 15
                "Waduh, kena tilang. -$15"
            },
            Card(CardType.CHANCE, "Pergi ke Penjara!") { player, state ->
                player.position = 10
                player.isInJail = true
                "Tidakkk! Langsung ke penjara."
            }
        )
    }

    fun getCommunityChestCards(): List<Card> {
        return listOf(
            Card(CardType.COMMUNITY_CHEST, "Warisan dari Paman $100") { player, state ->
                player.balance += 100
                "Wah, paman baik sekali! +$100"
            },
            Card(CardType.COMMUNITY_CHEST, "Kesalahan Bank, Anda Untung $200") { player, state ->
                player.balance += 200
                "Bank salah transfer! +$200"
            },
            Card(CardType.COMMUNITY_CHEST, "Bayar Iuran Sekolah $50") { player, state ->
                player.balance -= 50
                "Pendidikan itu penting. -$50"
            },
            Card(CardType.COMMUNITY_CHEST, "Ulang Tahun! Terima $10 dari tiap pemain") { player, state ->
                var total = 0
                state.players.forEach {
                    if (it.id != player.id) {
                        it.balance -= 10
                        total += 10
                    }
                }
                player.balance += total
                "Selamat Ulang Tahun! Terkumpul +$$total"
            },
            Card(CardType.COMMUNITY_CHEST, "Jual Saham $50") { player, state ->
                player.balance += 50
                "Pasar saham sedang naik. +$50"
            }
        )
    }
}
