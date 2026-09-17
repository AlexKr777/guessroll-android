package com.guessroll

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.guessroll.data.supabase.MissingSupabaseRoomRepository
import com.guessroll.data.supabase.SupabaseClientFactory
import com.guessroll.data.supabase.SupabaseConfig
import com.guessroll.data.supabase.SupabaseRoomRepository
import com.guessroll.ui.GuessRollApp

class MainActivity : ComponentActivity() {
    private var incomingInvite: String? by mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        incomingInvite = intent?.dataString

        val config = SupabaseConfig.fromBuildConfig()
        val repository = if (config.isConfigured) {
            SupabaseRoomRepository(SupabaseClientFactory.create(config))
        } else {
            MissingSupabaseRoomRepository()
        }

        setContent {
            GuessRollApp(
                repository = repository,
                isSupabaseConfigured = config.isConfigured,
                incomingInvite = incomingInvite,
                onIncomingInviteConsumed = { incomingInvite = null },
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        incomingInvite = intent.dataString
    }
}
