package com.guessroll.domain.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import org.junit.Test;

public class LobbyReadinessTest {
    @Test
    public void requiresAtLeastTwoPlayers() {
        LobbyReadinessStatus status = LobbyReadiness.INSTANCE.evaluate(1, 1);

        assertFalse(status.getCanStart());
        assertEquals(1, status.getMissingPlayerCount());
        assertEquals(0, status.getMissingPhotoCount());
    }

    @Test
    public void requiresRequiredPhotosPerPlayer() {
        LobbyReadinessStatus status = LobbyReadiness.INSTANCE.evaluate(
                3,
                Arrays.asList(
                        new PlayerMediaProgress("p1", "Mira", 8, 8),
                        new PlayerMediaProgress("p2", "Alex", 2, 8),
                        new PlayerMediaProgress("p3", "Nika", 0, 8)
                ),
                8
        );

        assertFalse(status.getCanStart());
        assertEquals(0, status.getMissingPlayerCount());
        assertEquals(14, status.getMissingPhotoCount());
    }

    @Test
    public void canStartWhenEveryPlayerHasRequiredPhotos() {
        LobbyReadinessStatus status = LobbyReadiness.INSTANCE.evaluate(
                3,
                Arrays.asList(
                        new PlayerMediaProgress("p1", "Mira", 8, 8),
                        new PlayerMediaProgress("p2", "Alex", 8, 8),
                        new PlayerMediaProgress("p3", "Nika", 9, 8)
                ),
                8
        );

        assertTrue(status.getCanStart());
        assertEquals(0, status.getMissingPhotoCount());
    }
}
