package com.guessroll.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import android.content.Context
import com.guessroll.data.local.LocalFeedbackSettingsStore
import com.guessroll.data.local.LocalWinStreakStore
import com.guessroll.data.supabase.RoomRepository
import com.guessroll.ui.navigation.Routes
import com.guessroll.ui.components.GameMusicHost
import com.guessroll.ui.screens.CreateRoomScreen
import com.guessroll.ui.screens.GameRoundScreen
import com.guessroll.ui.screens.HomeScreen
import com.guessroll.ui.screens.JoinRoomScreen
import com.guessroll.ui.screens.LobbyScreen
import com.guessroll.ui.screens.NicknameScreen
import com.guessroll.ui.screens.ResultsScreen
import com.guessroll.ui.theme.GuessRollTheme
import com.guessroll.ui.theme.Ink
import com.guessroll.ui.theme.LocalGuessRollMotion

@Composable
fun GuessRollApp(
    repository: RoomRepository,
    isSupabaseConfigured: Boolean,
    incomingInvite: String? = null,
    onIncomingInviteConsumed: () -> Unit = {},
) {
    GuessRollTheme {
        val navController = rememberNavController()
        val appContext = LocalContext.current.applicationContext
        val reducedMotion = LocalGuessRollMotion.current.reducedMotion
        val enterDuration = if (reducedMotion) 0 else 155
        val exitDuration = if (reducedMotion) 0 else 95
        val viewModel: GuessRollViewModel = viewModel(
            factory = GuessRollViewModelFactory(repository, appContext),
        )
        val currentBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = currentBackStackEntry?.destination?.route
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        val musicAllowedOnRoute = when (currentRoute) {
            Routes.Nickname,
            Routes.Home,
            Routes.CreateRoom,
            Routes.JoinRoom,
            Routes.Lobby,
            Routes.Results -> true
            else -> false
        }

        LaunchedEffect(currentRoute) {
            viewModel.clearTransientErrors(
                preservePhotoPrep = currentRoute == Routes.Lobby,
                preserveGame = currentRoute == Routes.Game || currentRoute == Routes.Results,
            )
        }

        LaunchedEffect(incomingInvite) {
            if (!incomingInvite.isNullOrBlank()) {
                viewModel.applyIncomingInvite(incomingInvite)
                onIncomingInviteConsumed()
            }
        }

        LaunchedEffect(state.pendingInviteRoomCode, state.nickname) {
            if (state.pendingInviteRoomCode != null && state.nickname != null) {
                viewModel.consumePendingInviteRoute()
                navController.navigate(Routes.JoinRoom) {
                    launchSingleTop = true
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Ink),
        ) {
            GameMusicHost(enabled = state.musicEnabled && musicAllowedOnRoute)
            NavHost(
                navController = navController,
                startDestination = Routes.Nickname,
                modifier = Modifier.fillMaxSize(),
                enterTransition = {
                    fadeIn(animationSpec = tween(durationMillis = enterDuration)) +
                        slideInVertically(
                            animationSpec = tween(durationMillis = enterDuration),
                            initialOffsetY = { if (reducedMotion) 0 else it / 90 },
                        )
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(durationMillis = exitDuration))
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(durationMillis = enterDuration)) +
                        slideInVertically(
                            animationSpec = tween(durationMillis = enterDuration),
                            initialOffsetY = { if (reducedMotion) 0 else -it / 100 },
                        )
                },
                popExitTransition = {
                    fadeOut(animationSpec = tween(durationMillis = exitDuration)) +
                        slideOutVertically(
                            animationSpec = tween(durationMillis = exitDuration),
                            targetOffsetY = { if (reducedMotion) 0 else it / 100 },
                        )
                },
            ) {
                composable(Routes.Nickname) {
                    NicknameScreen(
                        viewModel = viewModel,
                        onContinue = {
                            val targetRoute = if (viewModel.consumePendingInviteRoute()) {
                                Routes.JoinRoom
                            } else {
                                Routes.Home
                            }
                            navController.navigate(targetRoute)
                        },
                    )
                }
                composable(Routes.Home) {
                    HomeScreen(
                        viewModel = viewModel,
                        isSupabaseConfigured = isSupabaseConfigured,
                        onCreateRoom = { navController.navigate(Routes.CreateRoom) },
                        onJoinRoom = { navController.navigate(Routes.JoinRoom) },
                    )
                }
                composable(Routes.CreateRoom) {
                    CreateRoomScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onRoomCreated = {
                            navController.navigate(Routes.Lobby) {
                                popUpTo(Routes.Home)
                            }
                        },
                    )
                }
                composable(Routes.JoinRoom) {
                    JoinRoomScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onJoined = {
                            navController.navigate(Routes.Lobby) {
                                popUpTo(Routes.Home)
                            }
                        },
                    )
                }
                composable(Routes.Lobby) {
                    LobbyScreen(
                        viewModel = viewModel,
                        onBackHome = {
                            viewModel.returnHome()
                            navController.navigate(Routes.Home) {
                                popUpTo(Routes.Home) {
                                    inclusive = true
                                }
                            }
                        },
                        onStartGame = {
                            viewModel.startGame(
                                onStarted = {
                                    navController.navigate(Routes.Game)
                                },
                                onFinished = {
                                    navController.navigate(Routes.Results)
                                },
                            )
                        },
                        onGameStarted = {
                            navController.navigate(Routes.Game)
                        },
                        onFinished = {
                            navController.navigate(Routes.Results)
                        },
                    )
                }
                composable(Routes.Game) {
                    GameRoundScreen(
                        viewModel = viewModel,
                        onBackHome = {
                            viewModel.returnHome()
                            navController.navigate(Routes.Home) {
                                popUpTo(Routes.Home) {
                                    inclusive = true
                                }
                            }
                        },
                        onFinished = {
                            navController.navigate(Routes.Results) {
                                popUpTo(Routes.Game) {
                                    inclusive = true
                                }
                            }
                        },
                    )
                }
                composable(Routes.Results) {
                    ResultsScreen(
                        viewModel = viewModel,
                        onBackHome = {
                            viewModel.returnHome()
                            navController.navigate(Routes.Home) {
                                popUpTo(Routes.Home) {
                                    inclusive = true
                                }
                            }
                        },
                        onRematch = {
                            viewModel.startRematch(
                                onStarted = {
                                    navController.navigate(Routes.Game) {
                                        popUpTo(Routes.Results) {
                                            inclusive = true
                                        }
                                    }
                                },
                            )
                        },
                        onRematchStarted = {
                            navController.navigate(Routes.Game) {
                                popUpTo(Routes.Results) {
                                    inclusive = true
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

private class GuessRollViewModelFactory(
    private val repository: RoomRepository,
    private val appContext: Context,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GuessRollViewModel(
            repository = repository,
            localWinStreakStore = LocalWinStreakStore(appContext),
            localFeedbackSettingsStore = LocalFeedbackSettingsStore(appContext),
        ) as T
    }
}
