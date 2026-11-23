package com.example.tv

import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tv.model.TvGameId
import com.example.tv.model.TvRoute
import com.example.tv.ui.games.BrickBreakerGame
import com.example.tv.ui.games.NeonSnakeGame
import com.example.tv.ui.games.SpaceStrikerGame
import com.example.tv.ui.screens.GamepadTesterScreen
import com.example.tv.ui.screens.TvHomeScreen
import com.example.tv.ui.theme.TvArcadeTheme
import com.example.tv.viewmodel.TvViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TvViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            TvArcadeTheme {
                val route by viewModel.currentRoute.collectAsStateWithLifecycle()
                val padState by viewModel.padState.collectAsStateWithLifecycle()
                val highScores by viewModel.highScores.collectAsStateWithLifecycle()

                when (route) {
                    TvRoute.HOME -> TvHomeScreen(
                        highScores = highScores,
                        padState = padState,
                        onNavigate = viewModel::navigateTo,
                        onToggleLanguage = viewModel::toggleLanguage
                    )
                    TvRoute.GAME_SPACE_STRIKER -> SpaceStrikerGame(
                        padState = padState,
                        bestScore = highScores.spaceStrikerBest,
                        language = highScores.language,
                        onScoreRecorded = { viewModel.recordScore(TvGameId.SPACE_STRIKER, it) },
                        onExit = viewModel::navigateHome
                    )
                    TvRoute.GAME_NEON_SNAKE -> NeonSnakeGame(
                        padState = padState,
                        bestScore = highScores.neonSnakeBest,
                        language = highScores.language,
                        onScoreRecorded = { viewModel.recordScore(TvGameId.NEON_SNAKE, it) },
                        onExit = viewModel::navigateHome
                    )
                    TvRoute.GAME_BRICK_BREAKER -> BrickBreakerGame(
                        padState = padState,
                        bestScore = highScores.brickBreakerBest,
                        language = highScores.language,
                        onScoreRecorded = { viewModel.recordScore(TvGameId.BRICK_BREAKER, it) },
                        onExit = viewModel::navigateHome
                    )
                    TvRoute.INPUT_TESTER -> GamepadTesterScreen(
                        padState = padState,
                        language = highScores.language,
                        onExit = viewModel::navigateHome
                    )
                }
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val currentRoute = viewModel.currentRoute.value
        if (currentRoute != TvRoute.HOME) {
            if (event.keyCode == KeyEvent.KEYCODE_BUTTON_B && event.action == KeyEvent.ACTION_DOWN) {
                viewModel.navigateHome()
                return true
            }
            if (viewModel.inputManager.onKeyEvent(event)) {
                return true
            }
        } else {
            // Still update padState name/status on home while letting Compose focus navigation handle D-pad
            viewModel.inputManager.onKeyEvent(event)
        }
        return super.dispatchKeyEvent(event)
    }

    override fun dispatchGenericMotionEvent(ev: MotionEvent): Boolean {
        if (viewModel.inputManager.onGenericMotionEvent(ev)) {
            if (viewModel.currentRoute.value != TvRoute.HOME) {
                return true
            }
        }
        return super.dispatchGenericMotionEvent(ev)
    }
}
