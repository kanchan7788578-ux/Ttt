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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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
import com.example.tv.ui.theme.CrimsonAlert
import com.example.tv.ui.theme.CyanGlow
import com.example.tv.ui.theme.EmeraldConnected
import com.example.tv.ui.theme.TextPrimary
import com.example.tv.ui.theme.TextSecondary
import com.example.tv.ui.theme.VioletNeon

private data class BrickItem(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float,
    val color: Color,
    var alive: Boolean = true
)

@Composable
fun BrickBreakerGame(
    padState: HardwarePadState,
    bestScore: Int,
    language: TvLanguage,
    onScoreRecorded: (Int) -> Unit,
    onExit: () -> Unit
) {
    BackHandler { onExit() }
    val isBn = language == TvLanguage.BENGALI
    val currentPad by rememberUpdatedState(padState)
    val latestOnScore by rememberUpdatedState(onScoreRecorded)

    var paddleX by remember { mutableStateOf(0.5f) }
    val paddleWidth = 0.18f
    var ballX by remember { mutableStateOf(0.5f) }
    var ballY by remember { mutableStateOf(0.84f) }
    var vx by remember { mutableStateOf(0.42f) }
    var vy by remember { mutableStateOf(-0.56f) }
    var isBallAttached by remember { mutableStateOf(true) }
    var score by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(3) }
    var isGameOver by remember { mutableStateOf(false) }

    val bricks = remember { mutableStateListOf<BrickItem>() }

    fun populateBricks() {
        bricks.clear()
        val rows = 4
        val cols = 9
        val colors = listOf(CoralPulse, AmberGold, EmeraldConnected, VioletNeon)
        val marginX = 0.05f
        val gap = 0.012f
        val bw = (1f - marginX * 2f - gap * (cols - 1)) / cols
        val bh = 0.048f
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                bricks.add(
                    BrickItem(
                        left = marginX + c * (bw + gap),
                        top = 0.12f + r * (bh + 0.016f),
                        width = bw,
                        height = bh,
                        color = colors[r % colors.size]
                    )
                )
            }
        }
    }

    fun resetGame() {
        paddleX = 0.5f
        ballX = 0.5f
        ballY = 0.84f
        vx = 0.42f
        vy = -0.56f
        isBallAttached = true
        score = 0
        lives = 3
        isGameOver = false
        populateBricks()
    }

    LaunchedEffect(Unit) {
        populateBricks()
    }

    LaunchedEffect(padState.buttonA, padState.buttonStart, isGameOver) {
        if (isGameOver && (padState.buttonA || padState.buttonStart)) {
            resetGame()
        } else if (isBallAttached && (padState.buttonA || padState.buttonStart)) {
            isBallAttached = false
        }
    }

    LaunchedEffect(isGameOver) {
        var lastNanos = 0L
        while (!isGameOver) {
            withFrameNanos { frameNanos ->
                if (lastNanos == 0L) {
                    lastNanos = frameNanos
                    return@withFrameNanos
                }
                val dt = ((frameNanos - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                lastNanos = frameNanos

                paddleX = (paddleX + currentPad.effectiveX * 0.85f * dt).coerceIn(paddleWidth / 2f, 1f - paddleWidth / 2f)

                if (isBallAttached) {
                    ballX = paddleX
                    ballY = 0.85f
                    return@withFrameNanos
                }

                ballX += vx * dt
                ballY += vy * dt

                if (ballX < 0.02f) {
                    ballX = 0.02f
                    vx = kotlin.math.abs(vx)
                } else if (ballX > 0.98f) {
                    ballX = 0.98f
                    vx = -kotlin.math.abs(vx)
                }
                if (ballY < 0.04f) {
                    ballY = 0.04f
                    vy = kotlin.math.abs(vy)
                }

                // Paddle bounce
                val paddleTop = 0.88f
                if (vy > 0f && ballY >= paddleTop - 0.015f && ballY <= paddleTop + 0.025f &&
                    kotlin.math.abs(ballX - paddleX) <= paddleWidth * 0.56f
                ) {
                    val hitOffset = ((ballX - paddleX) / (paddleWidth / 2f)).coerceIn(-1f, 1f)
                    vx = hitOffset * 0.62f
                    vy = -kotlin.math.abs(vy)
                    ballY = paddleTop - 0.02f
                }

                // Brick collisions
                for (i in bricks.indices) {
                    val b = bricks[i]
                    if (!b.alive) continue
                    if (ballX in (b.left - 0.01f)..(b.left + b.width + 0.01f) &&
                        ballY in (b.top - 0.01f)..(b.top + b.height + 0.01f)
                    ) {
                        bricks[i] = b.copy(alive = false)
                        vy = -vy
                        score += 50
                        break
                    }
                }

                if (bricks.none { it.alive }) {
                    populateBricks()
                    isBallAttached = true
                }

                // Missed bottom
                if (ballY > 1.02f) {
                    lives -= 1
                    if (lives <= 0) {
                        isGameOver = true
                        latestOnScore(score)
                    } else {
                        isBallAttached = true
                        ballX = paddleX
                        ballY = 0.85f
                    }
                }
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
                        color = VioletNeon
                    )
                    HudBadge(
                        label = if (isBn) "সেরা: ${maxOf(bestScore, score)}" else "BEST: ${maxOf(bestScore, score)}",
                        color = AmberGold
                    )
                    HudBadge(
                        label = if (isBn) "জীবন: ${"♥".repeat(lives.coerceAtLeast(0))}" else "LIVES: ${"♥".repeat(lives.coerceAtLeast(0))}",
                        color = CrimsonAlert
                    )
                }
                OutlinedButton(
                    onClick = onExit,
                    modifier = Modifier.height(48.dp).testTag("exit_brick_game"),
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
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    bricks.forEach { b ->
                        if (b.alive) {
                            drawRoundRect(
                                color = b.color,
                                topLeft = Offset(b.left * w, b.top * h),
                                size = Size(b.width * w, b.height * h),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )
                        }
                    }

                    // Paddle
                    val pw = paddleWidth * w
                    val ph = 14.dp.toPx()
                    drawRoundRect(
                        color = CyanGlow,
                        topLeft = Offset(paddleX * w - pw / 2f, 0.88f * h),
                        size = Size(pw, ph),
                        cornerRadius = CornerRadius(7.dp.toPx(), 7.dp.toPx())
                    )

                    // Ball
                    drawCircle(
                        color = TextPrimary,
                        radius = 9.dp.toPx(),
                        center = Offset(ballX * w, ballY * h)
                    )
                }

                if (isBallAttached && !isGameOver) {
                    Text(
                        text = if (isBn) "বল ছাড়তে বাটন A চাপুন" else "Press Button A to Launch Ball",
                        style = MaterialTheme.typography.titleMedium,
                        color = CyanGlow,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(top = 60.dp)
                    )
                }
            }

            Text(
                text = if (isBn) "বাম/ডান D-Pad বা স্টিক দিয়ে প্যাডল নড়ান" else "Move Paddle: Left/Right D-Pad or Stick",
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
                    .border(2.dp, VioletNeon, RoundedCornerShape(24.dp))
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
                        modifier = Modifier.height(48.dp).testTag("restart_brick_game"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VioletNeon,
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
