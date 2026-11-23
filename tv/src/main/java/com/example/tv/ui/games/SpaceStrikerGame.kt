package com.example.tv.ui.games

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
import kotlin.math.abs
import kotlin.random.Random

private data class LaserShot(var xNorm: Float, var yNorm: Float)
private data class EnemyShip(var xNorm: Float, var yNorm: Float, val speed: Float, val hp: Int = 1)
private data class StarDot(val xNorm: Float, var yNorm: Float, val speed: Float, val radius: Float)

@Composable
fun SpaceStrikerGame(
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

    var shipX by remember { mutableStateOf(0.5f) }
    var shipY by remember { mutableStateOf(0.84f) }
    var score by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(3) }
    var wave by remember { mutableIntStateOf(1) }
    var isGameOver by remember { mutableStateOf(false) }

    val shots = remember { mutableStateListOf<LaserShot>() }
    val enemies = remember { mutableStateListOf<EnemyShip>() }
    val stars = remember {
        mutableStateListOf<StarDot>().apply {
            repeat(42) {
                add(
                    StarDot(
                        xNorm = Random.nextFloat(),
                        yNorm = Random.nextFloat(),
                        speed = 0.08f + Random.nextFloat() * 0.16f,
                        radius = 1.5f + Random.nextFloat() * 2.5f
                    )
                )
            }
        }
    }

    fun resetGame() {
        shipX = 0.5f
        shipY = 0.84f
        score = 0
        lives = 3
        wave = 1
        shots.clear()
        enemies.clear()
        isGameOver = false
    }

    // Handle B button to exit or A/Start to restart on game over
    LaunchedEffect(padState.buttonB, padState.buttonA, padState.buttonStart, isGameOver) {
        if (isGameOver && (padState.buttonA || padState.buttonStart)) {
            resetGame()
        }
    }

    // 60fps arcade loop
    LaunchedEffect(isGameOver) {
        var lastNanos = 0L
        var shootCooldown = 0f
        var spawnTimer = 0f

        while (!isGameOver) {
            withFrameNanos { frameNanos ->
                if (lastNanos == 0L) {
                    lastNanos = frameNanos
                    return@withFrameNanos
                }
                val dt = ((frameNanos - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                lastNanos = frameNanos

                // Starfield scroll
                for (i in stars.indices) {
                    val s = stars[i]
                    var ny = s.yNorm + s.speed * dt
                    if (ny > 1f) ny -= 1f
                    stars[i] = s.copy(yNorm = ny)
                }

                // Move player ship using D-pad or Left Stick
                val moveSpeed = 0.72f
                shipX = (shipX + currentPad.effectiveX * moveSpeed * dt).coerceIn(0.06f, 0.94f)
                shipY = (shipY + currentPad.effectiveY * moveSpeed * dt).coerceIn(0.25f, 0.92f)

                // Auto-fire when holding A or X or R1 (or always gentle auto-fire? Let's allow A/X/R1 + periodic pulse if pressed)
                shootCooldown -= dt
                if ((currentPad.buttonA || currentPad.buttonX || currentPad.buttonR1) && shootCooldown <= 0f) {
                    shots.add(LaserShot(shipX, shipY - 0.04f))
                    shootCooldown = 0.16f
                }

                // Update lasers
                val shotIter = shots.listIterator()
                while (shotIter.hasNext()) {
                    val shot = shotIter.next()
                    shot.yNorm -= 1.15f * dt
                    if (shot.yNorm < -0.05f) {
                        shotIter.remove()
                    }
                }

                // Spawn enemies
                spawnTimer -= dt
                val spawnInterval = (1.1f - wave * 0.06f).coerceAtLeast(0.32f)
                if (spawnTimer <= 0f) {
                    enemies.add(
                        EnemyShip(
                            xNorm = 0.08f + Random.nextFloat() * 0.84f,
                            yNorm = -0.05f,
                            speed = 0.22f + wave * 0.028f + Random.nextFloat() * 0.08f
                        )
                    )
                    spawnTimer = spawnInterval
                }

                // Update enemies & collisions
                val enemyIter = enemies.listIterator()
                while (enemyIter.hasNext()) {
                    val enemy = enemyIter.next()
                    enemy.yNorm += enemy.speed * dt

                    // Check hit by laser
                    var destroyed = false
                    val hitShot = shots.firstOrNull {
                        abs(it.xNorm - enemy.xNorm) < 0.045f && abs(it.yNorm - enemy.yNorm) < 0.045f
                    }
                    if (hitShot != null) {
                        shots.remove(hitShot)
                        enemyIter.remove()
                        score += 100
                        wave = 1 + (score / 1000)
                        destroyed = true
                    }

                    if (!destroyed) {
                        // Check collision with player ship or bottom
                        val hitPlayer = abs(enemy.xNorm - shipX) < 0.05f && abs(enemy.yNorm - shipY) < 0.055f
                        if (hitPlayer || enemy.yNorm > 1.02f) {
                            enemyIter.remove()
                            lives -= 1
                            if (lives <= 0) {
                                isGameOver = true
                                latestOnScore(score)
                                break
                            }
                        }
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
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Stars
            stars.forEach { star ->
                drawCircle(
                    color = Color.White.copy(alpha = 0.45f),
                    radius = star.radius,
                    center = Offset(star.xNorm * w, star.yNorm * h)
                )
            }

            // Lasers
            shots.forEach { shot ->
                drawRoundRect(
                    color = CyanGlow,
                    topLeft = Offset(shot.xNorm * w - 4.dp.toPx(), shot.yNorm * h - 14.dp.toPx()),
                    size = Size(8.dp.toPx(), 24.dp.toPx())
                )
            }

            // Enemies
            enemies.forEach { enemy ->
                val ex = enemy.xNorm * w
                val ey = enemy.yNorm * h
                val r = 18.dp.toPx()
                val path = Path().apply {
                    moveTo(ex, ey + r)
                    lineTo(ex - r, ey - r * 0.7f)
                    lineTo(ex, ey - r * 0.25f)
                    lineTo(ex + r, ey - r * 0.7f)
                    close()
                }
                drawPath(path = path, color = CoralPulse)
            }

            // Player Ship
            val px = shipX * w
            val py = shipY * h
            val sr = 22.dp.toPx()
            val shipPath = Path().apply {
                moveTo(px, py - sr)
                lineTo(px + sr * 0.9f, py + sr * 0.8f)
                lineTo(px, py + sr * 0.35f)
                lineTo(px - sr * 0.9f, py + sr * 0.8f)
                close()
            }
            drawPath(path = shipPath, color = CyanGlow)
            drawCircle(color = AmberGold, radius = 6.dp.toPx(), center = Offset(px, py + sr * 0.55f))
        }

        // Top HUD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                HudBadge(
                    label = if (isBn) "স্কোর: $score" else "SCORE: $score",
                    color = CyanGlow
                )
                HudBadge(
                    label = if (isBn) "সেরা: ${maxOf(bestScore, score)}" else "BEST: ${maxOf(bestScore, score)}",
                    color = AmberGold
                )
                HudBadge(
                    label = if (isBn) "ওয়েভ: $wave" else "WAVE: $wave",
                    color = EmeraldConnected
                )
                HudBadge(
                    label = if (isBn) "জীবন: ${"♥".repeat(lives.coerceAtLeast(0))}" else "LIVES: ${"♥".repeat(lives.coerceAtLeast(0))}",
                    color = CrimsonAlert
                )
            }

            OutlinedButton(
                onClick = onExit,
                modifier = Modifier.height(48.dp).testTag("exit_space_game"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
            ) {
                Text(if (isBn) "বের হন (B)" else "Exit (B)")
            }
        }

        // Controls hint at bottom
        if (!isGameOver) {
            Text(
                text = if (isBn) "D-Pad / স্টিক দিয়ে নড়ান • A বাটনে ফায়ার" else "Move: D-Pad / Stick • Fire: Button A",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary.copy(alpha = 0.75f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            )
        }

        // Game Over Overlay
        if (isGameOver) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(24.dp))
                    .background(ConsoleSurface.copy(alpha = 0.94f))
                    .border(2.dp, CyanGlow, RoundedCornerShape(24.dp))
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
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { resetGame() },
                        modifier = Modifier.height(48.dp).testTag("restart_space_game"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanGlow,
                            contentColor = ConsoleObsidian
                        )
                    ) {
                        Text(if (isBn) "আবার খেলুন (A)" else "Play Again (A)")
                    }
                    OutlinedButton(
                        onClick = onExit,
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text(if (isBn) "হোমে ফিরুন" else "Back Home", color = TextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
internal fun HudBadge(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(ConsoleSurface.copy(alpha = 0.85f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = color
        )
    }
}
