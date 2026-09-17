package com.guessroll.domain.invite

data class RoomInviteShare(
    val roomCode: String,
    val link: String,
    val text: String,
)

object RoomInviteShareBuilder {
    fun build(roomCode: String): RoomInviteShare {
        val normalized = RoomInviteParser.parseRoomCode(roomCode) ?: roomCode
            .uppercase()
            .filter { it.isLetterOrDigit() }
            .take(8)
        val link = RoomInviteParser.httpsLinkForRoomCode(normalized)
        return RoomInviteShare(
            roomCode = normalized,
            link = link,
            text = buildString {
                appendLine("Залетай в GuessRoll")
                appendLine("Код комнаты: $normalized")
                appendLine("Ссылка для входа:")
                append(link)
            },
        )
    }
}
