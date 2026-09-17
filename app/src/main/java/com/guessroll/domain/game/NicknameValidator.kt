package com.guessroll.domain.game

object NicknameValidator {
    const val MinLength = 2
    const val MaxLength = 18

    fun normalize(input: String): String = input.trim().replace(Regex("\\s+"), " ")

    fun validate(input: String): NicknameValidation {
        val normalized = normalize(input)
        return when {
            normalized.length < MinLength -> NicknameValidation.Invalid("Минимум $MinLength символа.")
            normalized.length > MaxLength -> NicknameValidation.Invalid("Максимум $MaxLength символов.")
            else -> NicknameValidation.Valid(normalized)
        }
    }
}

sealed interface NicknameValidation {
    data class Valid(val value: String) : NicknameValidation
    data class Invalid(val message: String) : NicknameValidation
}
