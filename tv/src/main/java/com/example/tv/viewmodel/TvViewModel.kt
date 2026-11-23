package com.example.tv.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tv.data.TvRepository
import com.example.tv.input.TvGamepadInputManager
import com.example.tv.model.HardwarePadState
import com.example.tv.model.TvGameId
import com.example.tv.model.TvHighScores
import com.example.tv.model.TvLanguage
import com.example.tv.model.TvRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TvViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TvRepository(application)
    val inputManager = TvGamepadInputManager()

    private val _currentRoute = MutableStateFlow(TvRoute.HOME)
    val currentRoute: StateFlow<TvRoute> = _currentRoute.asStateFlow()

    val padState: StateFlow<HardwarePadState> = inputManager.padState

    val highScores: StateFlow<TvHighScores> = repository.scoresFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TvHighScores()
    )

    fun navigateTo(route: TvRoute) {
        _currentRoute.value = route
    }

    fun navigateHome() {
        _currentRoute.value = TvRoute.HOME
    }

    fun recordScore(gameId: TvGameId, score: Int) {
        viewModelScope.launch {
            repository.saveHighScoreIfBest(gameId, score)
        }
    }

    fun toggleLanguage() {
        val next = if (highScores.value.language == TvLanguage.BENGALI) {
            TvLanguage.ENGLISH
        } else {
            TvLanguage.BENGALI
        }
        viewModelScope.launch {
            repository.setLanguage(next)
        }
    }
}
