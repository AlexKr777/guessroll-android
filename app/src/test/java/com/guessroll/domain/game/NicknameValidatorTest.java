package com.guessroll.domain.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NicknameValidatorTest {
    @Test
    public void normalizesRepeatedSpaces() {
        NicknameValidation result = NicknameValidator.INSTANCE.validate("  Ada   Lovelace  ");

        assertTrue(result instanceof NicknameValidation.Valid);
        assertEquals("Ada Lovelace", ((NicknameValidation.Valid) result).getValue());
    }

    @Test
    public void rejectsNicknamesThatAreTooShort() {
        NicknameValidation result = NicknameValidator.INSTANCE.validate(" A ");

        assertTrue(result instanceof NicknameValidation.Invalid);
    }
}
