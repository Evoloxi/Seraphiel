package me.evo.seraphiel

class UnknownCommandException(message: String) : Exception("Unknown command '$message'. This command does not exist or is not registered.")
