package com.guessroll.domain.invite

import java.net.URLDecoder
import java.net.URI
import java.nio.charset.StandardCharsets

object RoomInviteParser {
    private val PlainCodePattern = Regex("^[A-Z0-9]{4,8}$")
    private const val ShareHost = "guessroll-app-links.pages.dev"
    private val AllowedHttpsHosts = setOf(
        ShareHost,
        "guessroll.app",
    )

    fun payloadForRoomCode(roomCode: String): String {
        return httpsLinkForRoomCode(roomCode)
    }

    fun httpsLinkForRoomCode(roomCode: String): String {
        return "https://$ShareHost/join?code=${roomCodeForOutboundLink(roomCode)}"
    }

    fun parseRoomCode(rawValue: String): String? {
        val value = rawValue.trim()
        if (value.isEmpty()) return null

        canonicalRoomCode(value)?.let { plainCode ->
            return plainCode
        }

        val uri = runCatching { URI(value) }.getOrNull() ?: return null
        val isAppSchemeJoin = uri.scheme.equals("guessroll", ignoreCase = true) &&
            (uri.host.equals("join", ignoreCase = true) || uri.path.equals("/join", ignoreCase = true))
        val isHttpsJoin = uri.scheme.equals("https", ignoreCase = true) &&
            AllowedHttpsHosts.any { host -> uri.host.equals(host, ignoreCase = true) } &&
            uri.path.equals("/join", ignoreCase = true)
        val isGuessRollJoin = isAppSchemeJoin || isHttpsJoin
        if (!isGuessRollJoin) return null

        val code = uri.query
            ?.split("&")
            ?.firstNotNullOfOrNull { part ->
                val pieces = part.split("=", limit = 2)
                if (pieces.size == 2 && pieces[0].equals("code", ignoreCase = true)) {
                    decodeQueryValue(pieces[1])
                } else {
                    null
                }
            }
            ?: return null

        return canonicalRoomCode(code)
    }

    internal fun roomCodeForOutboundLink(value: String): String {
        return canonicalRoomCode(value)
            ?: value.uppercase().filter { it.isLetterOrDigit() }.take(8)
    }

    private fun canonicalRoomCode(value: String): String? {
        val normalized = value.trim().uppercase()
        return normalized.takeIf { it.matches(PlainCodePattern) }
    }

    private fun decodeQueryValue(value: String): String {
        return runCatching {
            URLDecoder.decode(value, StandardCharsets.UTF_8.name())
        }.getOrDefault(value)
    }
}
