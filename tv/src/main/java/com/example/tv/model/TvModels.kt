package com.example.tv.model

enum class TvGameId {
    SPACE_STRIKER,
    NEON_SNAKE,
    BRICK_BREAKER
}

enum class TvRoute {
    HOME,
    GAME_SPACE_STRIKER,
    GAME_NEON_SNAKE,
    GAME_BRICK_BREAKER,
    INPUT_TESTER
}

enum class TvLanguage {
    BENGALI,
    ENGLISH
}

data class TvHighScores(
    val spaceStrikerBest: Int = 0,
    val neonSnakeBest: Int = 0,
    val brickBreakerBest: Int = 0,
    val language: TvLanguage = TvLanguage.BENGALI
)

data class HardwarePadState(
    val up: Boolean = false,
    val down: Boolean = false,
    val left: Boolean = false,
    val right: Boolean = false,
    val buttonA: Boolean = false,
    val buttonB: Boolean = false,
    val buttonX: Boolean = false,
    val buttonY: Boolean = false,
    val buttonL1: Boolean = false,
    val buttonR1: Boolean = false,
    val buttonL2: Boolean = false,
    val buttonR2: Boolean = false,
    val buttonSelect: Boolean = false,
    val buttonStart: Boolean = false,
    val leftX: Float = 0f,
    val leftY: Float = 0f,
    val rightX: Float = 0f,
    val rightY: Float = 0f,
    val activeControllerName: String? = null
) {
    val effectiveX: Float
        get() = when {
            left -> -1f
            right -> 1f
            kotlin.math.abs(leftX) > 0.18f -> leftX
            else -> 0f
        }

    val effectiveY: Float
        get() = when {
            up -> -1f
            down -> 1f
            kotlin.math.abs(leftY) > 0.18f -> leftY
            else -> 0f
        }

    val isMovingUp: Boolean get() = up || leftY < -0.45f
    val isMovingDown: Boolean get() = down || leftY > 0.45f
    val isMovingLeft: Boolean get() = left || leftX < -0.45f
    val isMovingRight: Boolean get() = right || leftX > 0.45f
}
