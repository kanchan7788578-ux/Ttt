package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.screens.GamepadScreen
import com.example.ui.theme.TvPadTheme
import com.example.viewmodel.GamepadViewModel

class MainActivity : ComponentActivity() {

    private val gamepadViewModel: GamepadViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            TvPadTheme {
                GamepadScreen(viewModel = gamepadViewModel)
            }
        }
    }
}
