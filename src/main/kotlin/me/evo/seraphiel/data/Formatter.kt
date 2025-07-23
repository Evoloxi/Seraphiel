package me.evo.seraphiel.data

object Formatter {

    fun prestige(stars: Int): String {
        val unique = mapOf(69 to "c6eabd5")

        val icons = listOf(
            '✫',
            '✪',
            '⚝',
            '✥',
            '✷'
        )
        val colorCodes = listOf(
            "7", // 70-100
            "f", // 101-200
            "6", // 201-300
            "b", // 301-400
            "2", // 401-500
            "3", // 501-600
            "4", // 601-700
            "d", // 701-800
            "9", // 801-900
            "c", // 901-1000
            "c6eabd5", // 1000-999
            "7ffff77", // 1100-1199
            "7eeee67", // 1200-1299
            "7dddd37", // 1300-1399
            "7aaaa27", // 1400-1499
            "7333397", // 1500-1599
            "7cccc47", // 1600-1699
            "7dddd57", // 1700-1799
            "7999917", // 1800-1899
            "7555587", // 1900-1999
            "87ff778", // 2000-2099
            "ff33666", // 2100-2199
            "66ffb33", // 2200-2299
            "55dd6ee", // 2300-2399
            "bbff778", // 2400-2499
            "ffaa222", // 2500-2599
            "44ccdd5", // 2600-2699
            "eeff888", // 2700-2799
            "aa2266e", // 2800-2899
            "bb33991", // 2900-2999
            "ee66cc4", // 3000+
            "993366e", // 3100+ Sunshine Prestige
            "c4884cc", // 3200+ Eclipse Prestige
            "999dcc4", // 3300+ Gamma Prestige
            "22add55", // 3400+ Majestic Prestige
            "66dd6ee", // 3500+ Andesine Prestige
            "aaab991", // 3600+ Marine Prestige
            "44ccb33", // 3700+ Element Prestige
            "119955d", // 3800+ Galaxy Prestige
            "4422399", // 3900+ Atomic Prestige
            "55cc66e", // 4000+ Sunset Prestige
            "ee6cdd5", // 4100+ Time Prestige
            "193bf77", // 4200+ Winter Prestige
            "0588550", // 4300+ Obsidian Prestige
            "22ae65d", // 4400+ Spring Prestige
            "ffbb333", // 4500+ Ice Prestige
            "3bee6d5", // 4600+ Summer Prestige
            "74cc919", // 4700+ Spinel Prestige
            "55c6eb3", // 4800+ Autumn Prestige
            "2affaa2", // 4900+ Mystic Prestige
            "4459910" // 5000+ Eternal Prestige
        )

        val color = unique[stars] ?: colorCodes.getOrNull(stars / 100) ?: colorCodes.last()

        return buildString {
            val template = "[$stars${icons[(stars - 100).coerceAtLeast(0) / 1000]}]"
            for ((i, element) in template.withIndex()) {
                append("§${color.getOrElse(i) { color[0] }}$element")
            }
        }
    }


    fun formatHp(hp: Int): String {
        return when {
            hp < 1 -> "§c${hp}"
            hp < 6 -> "§6${hp}"
            hp < 11 -> "§e${hp}"
            else -> "§a${hp}"
        }
    }

    fun formatBBLR(r: Double, precision: Int = 2): String {
        val formattedValue = String.format("%.${precision}f", r)
        return when {
            r < 0.9 -> "§7$formattedValue"
            r < 1.3 -> "§6$formattedValue"
            r < 2.5 -> "§c$formattedValue"
            else -> "§4$formattedValue"
        }
    }

    fun formatFKDR(finalKDR: Double, precision: Int = 2): String {
        val formattedValue = String.format("%.${precision}f", finalKDR)
        return when {
            finalKDR < 1 -> "§7$formattedValue"
            finalKDR < 3 -> "§6$formattedValue"
            finalKDR < 7 -> "§c$formattedValue"
            else -> "§4$formattedValue"
        }
    }

    fun formatWLR(wlr: Double, precision: Int = 2): String {
        val formattedValue = String.format("%.${precision}f", wlr)
        return when {
            wlr < 1 -> "§7$formattedValue"
            wlr < 2 -> "§6$formattedValue"
            wlr < 3.5 -> "§c$formattedValue"
            else -> "§4$formattedValue"
        }
    }

    fun formatRank(user: String, rank: String, rankPlusColor: String?, monthlyRankColor: String?): String {
        return when (rank) {
            "VIP" -> "§a[VIP] "
            "VIP+" -> "§a[VIP§6+§a] "
            "MVP" -> "§b[MVP] "
            "MVP+" -> "§b[MVP${formatColor(rankPlusColor) ?: "§c"}+§b] "
            "MVP++" -> "${formatColor(monthlyRankColor) ?: "§b"}[MVP${formatColor(rankPlusColor) ?: "§c"}++${
                formatColor(
                    monthlyRankColor
                ) ?: "§b"
            }] "

            "YOUTUBE" -> "§c[§fYOUTUBE§c] "
            "HELPER" -> "§9[HELPER] "
            "MODERATOR" -> "§2[MOD] "
            "ADMIN" -> "§c[ADMIN] "
            "OWNER" -> "§c[OWNER] "
            else -> "§7"
        }.let { "$it$user" }
    }
    // TODO: Use EnumChatFormatting
    private fun formatColor(color: String?): String? {
        return when (color) {
            "WHITE" -> "§f"
            "BLACK" -> "§0"
            "DARK_BLUE" -> "§1"
            "DARK_GREEN" -> "§2"
            "DARK_AQUA" -> "§3"
            "DARK_RED" -> "§4"
            "DARK_PURPLE" -> "§5"
            "GOLD" -> "§6"
            "GRAY" -> "§7"
            "DARK_GRAY" -> "§8"
            "BLUE" -> "§9"
            "GREEN" -> "§a"
            "AQUA" -> "§b"
            "RED" -> "§c"
            "LIGHT_PURPLE" -> "§d"
            "YELLOW" -> "§e"
            else -> null
        }
    }
}