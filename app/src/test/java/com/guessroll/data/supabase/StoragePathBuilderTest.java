package com.guessroll.data.supabase;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StoragePathBuilderTest {
    @Test
    public void buildsPredictableRoomPlayerMediaPhotoPath() {
        String path = StoragePathBuilder.INSTANCE.photoPath("room-1", "player-2", "media-3");

        assertEquals("rooms/room-1/players/player-2/media-3.jpg", path);
    }
}
