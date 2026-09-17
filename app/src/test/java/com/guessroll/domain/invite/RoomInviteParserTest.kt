package com.guessroll.domain.invite

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RoomInviteParserTest {
    @Test
    fun parsesPlainRoomCode() {
        assertEquals("AB12C", RoomInviteParser.parseRoomCode("ab12c"))
    }

    @Test
    fun parsesGuessRollJoinDeepLink() {
        assertEquals("Q7W8E", RoomInviteParser.parseRoomCode("guessroll://join?code=Q7W8E"))
    }

    @Test
    fun parsesGuessRollHttpsInviteLink() {
        assertEquals("Q7W8E", RoomInviteParser.parseRoomCode("https://guessroll-app-links.pages.dev/join?code=Q7W8E"))
    }

    @Test
    fun parsesLegacyGuessRollHttpsInviteLink() {
        assertEquals("Q7W8E", RoomInviteParser.parseRoomCode("https://guessroll.app/join?code=Q7W8E"))
    }

    @Test
    fun rejectsWrongHttpsHostsAndMissingCodes() {
        assertNull(RoomInviteParser.parseRoomCode("https://evil.example/join?code=Q7W8E"))
        assertNull(RoomInviteParser.parseRoomCode("https://guessroll-app-links.pages.dev/join"))
        assertNull(RoomInviteParser.parseRoomCode("https://guessroll-app-links.pages.dev/join?room=Q7W8E"))
    }

    @Test
    fun rejectsUnsafeOrTooLongCodes() {
        assertNull(RoomInviteParser.parseRoomCode("https://guessroll-app-links.pages.dev/join?code=AB12C!!!"))
        assertNull(RoomInviteParser.parseRoomCode("https://guessroll-app-links.pages.dev/join?code=ABCDEFGHI"))
        assertNull(RoomInviteParser.parseRoomCode("ABC DEF"))
        assertNull(RoomInviteParser.parseRoomCode("ABCDEFGHI"))
    }

    @Test
    fun rejectsUnrelatedQrPayloads() {
        assertNull(RoomInviteParser.parseRoomCode("https://example.com/not-guessroll?code=ABCDE"))
        assertNull(RoomInviteParser.parseRoomCode("https://guessroll-app-links.pages.dev/not-join?code=ABCDE"))
        assertNull(RoomInviteParser.parseRoomCode("this is not a room code"))
        assertNull(RoomInviteParser.parseRoomCode("ABC"))
    }

    @Test
    fun buildsShareInviteWithCodeAndDeepLink() {
        val invite = RoomInviteShareBuilder.build("ab12c")

        assertEquals("AB12C", invite.roomCode)
        assertEquals("https://guessroll-app-links.pages.dev/join?code=AB12C", invite.link)
        assertEquals(true, invite.text.contains("Код комнаты: AB12C"))
        assertEquals(true, invite.text.contains("https://guessroll-app-links.pages.dev/join?code=AB12C"))
    }

    @Test
    fun buildsQrPayloadWithHttpsAppLink() {
        assertEquals(
            "https://guessroll-app-links.pages.dev/join?code=AB12C",
            RoomInviteParser.payloadForRoomCode("ab12c"),
        )
    }
}
