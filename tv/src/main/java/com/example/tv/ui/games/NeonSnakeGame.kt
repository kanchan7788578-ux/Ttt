package com.example.tv.ui.games

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.tv.model.HardwarePadState
import com.example.tv.model.TvLanguage
import com.example.tv.ui.theme.AmberGold
import com.example.tv.ui.theme.ConsoleObsidian
import com.example.tv.ui.theme.ConsoleObsidianDeep
import com.example.tv.ui.theme.ConsoleOutline
import com.example.tv.ui.theme.ConsoleSurface
import com.example.tv.ui.theme.CoralPulse
import com.example.tv.ui.theme.CyanGlow
import com.example.tv.ui.theme.EmeraldConnected
import com.example.tv.ui.theme.TextPrimary
import com.example.tv.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlin.random.Random

private enum class SnakeDir { UP, DOWN, LEFT, RIGHT }
private data class GridPoint(val x: Int, val y: Int)

@Composable
fun NeonSnakeGame(
    padState: HardwarePadState,
    bestScore: Int,
    language: TvLanguage,
    onScoreRecorded: (Int) -> Unit,
    onExit: () -> Unit
) {
    BackHandler { onExit() }
    val isBn = language == TvLanguage.BENGALI
    val cols = 28
    val rows = 16

    var snake by remember { mutableStateOf(listOf(GridPoint(14, 8), GridPoint(13, 8), GridPoint(12, 8))) }
    var currentDir by remember { mutableStateOf(SnakeDir.RIGHT) }
    var pendingDir by remember { mutableStateOf(SnakeDir.RIGHT) }
    var food by remember { mutableStateOf(GridPoint(20, 8)) }
    var score by remember { mutableIntStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }

    val latestOnScore by rememberUpdatedState(onScoreRecorded)

    fun spawnFood(occupied: List<GridPoint>): GridPoint {
        val candidates = mutableListOf<GridPoint>()
        for (x in 0 until cols) {
            for (y in 0 until rows) {
                val p = GridPoint(x, y)
                if (p !in occupied) candidates.add(p)
            }
        }
        return if (candidates.isEmpty()) GridPoint(0, 0) else candidates[Random.nextInt(candidates.size)]
    }

    fun resetGame() {
        val initial = listOf(GridPoint(14, 8), GridPoint(13, 8), GridPoint(12, 8))
        snake = initial
        currentDir = SnakeDir.RIGHT
        pendingDir = SnakeDir.RIGHT
        food = spawnFood(initial)
        score = 0
        isGameOver = false
    }

    // Update direction from D-Pad or Analog Stick
    LaunchedEffect(padState) {
        if (isGameOver && (padState.buttonA || padState.buttonStart)) {
            resetGame()
            return@LaunchedEffect
        }
        when {
            padState.isMovingUp && currentDir != SnakeDir.DOWN -> pendingDir = SnakeDir.UP
            padState.isMovingDown && currentDir != SnakeDir.UP -> pendingDir = SnakeDir.DOWN
            padState.isMovingLeft && currentDir != SnakeDir.RIGHT -> pendingDir = SnakeDir.LEFT
            padState.isMovingRight && currentDir != SnakeDir.LEFT -> pendingDir = SnakeDir.RIGHT
        }
    }

    // Snake tick loop
    LaunchedEffect(isGameOver) {
        while (!isGameOver) {
            val tickMs = (135L - (score / 100) * 6L).coerceAtLeast(65L)
            delay(tickMs)

            currentDir = pendingDir
            val head = snake.first()
            val nextHead = when (currentDir) {
                SnakeDir.UP -> GridPoint(head.x, head.y - 1)
                SnakeDir.DOWN -> GridPoint(head.x, head.y + 1)
                SnakeDir.LEFT -> GridPoint(head.x - 1, head.y)
                SnakeDir.RIGHT -> GridPoint(head.x + 1, head.y)
            }

            if (nextHead.x !in 0 until cols || nextHead.y !in 0 until rows || nextHead in snake) {
                isGameOver = true
                latestOnScore(score)
                break
            }

            val ate = nextHead == food
            val nextSnake = buildList {
                add(nextHead)
                addAll(if (ate) snake else snake.dropLast(1))
            }
            snake = nextSnake
            if (ate) {
                score += 50
                food = spawnFood(nextSnake)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ConsoleObsidianDeep)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    HudBadge(
                        label = if (isBn) "স্কোর: $score" else "SCORE: $score",
                        color = EmeraldConnected
                    )
                    HudBadge(
                        label = if (isBn) "সেরা: ${maxOf(bestScore, score)}" else "BEST: ${maxOf(bestScore, score)}",
                        color = AmberGold
                    )
                }
                OutlinedButton(
                    onClick = onExit,
                    modifier = Modifier.height(48.dp).testTag("exit_snake_game"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                ) {
                    Text(if (isBn) "বের হন" else "Exit")
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(ConsoleObsidian)
                    .border(2.dp, ConsoleOutline, RoundedCornerShape(18.dp))
                    .padding(8.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cellW = size.width / cols
                    val cellH = size.height / rows

                    // Subtle grid lines
                    for (c in 1 until cols) {
                        drawLine(
                            color = ConsoleOutline.copy(alpha = 0.25f),
                            start = Offset(c * cellW, 0f),
                            end = Offset(c * cellW, size.height)
                        )
                    }
                    for (r in 1 until rows) {
                        drawLine(
                            color = ConsoleOutline.copy(alpha = 0.25f),
                            start = Offset(0f, r * cellH),
                            end = Offset(size.width, r * cellH)
                        )
                    }

                    // Food
                    drawCircle(
                        color = CoralPulse,
                        radius = minOf(cellW, cellH) * 0.38f,
                        center = Offset((food.x + 0.5f) * cellW, (food.y + 0.5f) * cellH)
                    )

                    // Snake segments
                    snake.forEachIndexed { index, pt ->
                        val isHead = index == 0
                        val inset = if (isHead) 1.5.dp.toPx() else 2.5.dp.toPx()
                        drawRoundRect(
                            color = if (isHead) CyanGlow else EmeraldConnected.copy(alpha = 0.85f),
                            topLeft = Offset(pt.x * cellW + inset, pt.y * cellH + inset),
                            size = Size(cellW - inset * 2, cellH - inset * 2),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )
                    }
                }
            }

            Text(
                text = if (isBn) "D-Pad বা বাম অ্যানালগ স্টিক দিয়ে সাপ চালান" else "Steer with D-Pad or Left Analog Stick",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        if (isGameOver) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(24.dp))
                    .background(ConsoleSurface.copy(alpha = 0.94f))
                    .border(2.dp, EmeraldConnected, RoundedCornerShape(24.dp))
                    .padding(horizontal = 36.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isBn) "গেম ওভার!" else "GAME OVER",
                    style = MaterialTheme.typography.displaySmall,
                    color = CoralPulse
                )
                Text(
                    text = if (isBn) "আপনার স্কোর: $score" else "Final Score: $score",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { resetGame() },
                        modifier = Modifier.height(48.dp).testTag("restart_snake_game"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldConnected,
                            contentColor = ConsoleObsidian
                        )
                    ) {
                        Text(if (isBn) "আবার খেলুন (A)" else "Play Again (A)")
                    }
                    OutlinedButton(onClick = onExit, modifier = Modifier.height(48.dp)) {
                        Text(if (isBn) "হোমে ফিরুন" else "Back Home", color = TextPrimary)
                    }
                }
            }
        }
    }
}
