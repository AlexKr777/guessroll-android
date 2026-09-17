package com.guessroll.domain.game

import kotlin.random.Random

class RoomCodeGenerator(
    private val random: Random = Random.Default,
) {
    fun generate(length: Int = DefaultLength): String {
        require(length in 4..8) { "Код комнаты должен оставаться коротким для игры." }
        return buildString(length) {
            repeat(length) {
                append(Alphabet[random.nextInt(Alphabet.length)])
            }
        }
    }

    companion object {
        const val DefaultLength = 5
        private const val Alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    }
}
