package com.guessroll.data.supabase

object StoragePathBuilder {
    fun photoPath(roomId: String, playerId: String, mediaId: String): String {
        return "rooms/$roomId/players/$playerId/$mediaId.jpg"
    }
}
