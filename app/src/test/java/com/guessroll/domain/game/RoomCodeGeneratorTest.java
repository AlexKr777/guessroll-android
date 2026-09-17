package com.guessroll.domain.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RoomCodeGeneratorTest {
    @Test
    public void generatesFiveCharacterCode() {
        String code = new RoomCodeGenerator().generate(5);

        assertEquals(5, code.length());
    }

    @Test
    public void usesReadableUppercasePartyCodeAlphabet() {
        String code = new RoomCodeGenerator().generate(8);
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

        for (int index = 0; index < code.length(); index++) {
            assertTrue(alphabet.indexOf(code.charAt(index)) >= 0);
        }
    }
}
