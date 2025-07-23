package me.evo.seraphiel.extension

fun String.containsAny(vararg strings: String): Boolean {
    return strings.any { this.contains(it) }
}

fun String.containsAny(strings: List<String>): Boolean {
    return strings.any { this.contains(it) }
}