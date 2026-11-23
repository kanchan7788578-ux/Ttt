package com.example.tv.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.tv.model.HardwarePadState
import com.example.tv.model.TvHighScores
import com.example.tv.model.TvLanguage
import com.example.tv.model.TvRoute
import com.example.tv.ui.theme.ConsoleObsidian
import com.example.tv.ui.theme.ConsoleObsidianDeep
import com.example.tv.ui.theme.ConsoleOutline
import com.example.tv.ui.theme.ConsoleSurface
import com.example.tv.ui.theme.ConsoleSurfaceElevated
import com.example.tv.ui.theme.CoralPulse
import com.example.tv.ui.theme.CyanGlow
import com.example.tv.ui.theme.EmeraldConnected
import com.example.tv.ui.theme.TextPrimary
import com.example.tv.ui.theme.TextSecondary
import com.example.tv.ui.theme.VioletNeon

@Composable
fun TvHomeScreen(
    highScores: TvHighScores,
    padState: HardwarePadState,
    onNavigate: (TvRoute) -> Unit,
    onToggleLanguage: () -> Unit
) {
    val isBn = highScores.language == TvLanguage.BENGALI

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(colors = listOf(ConsoleObsidian, ConsoleObsidianDeep))
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "TV Arcade",
                        style = MaterialTheme.typography.displaySmall,
                        color = TextPrimary
                    )
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(EmeraldConnected.copy(alpha = 0.14f))
                            .border(1.dp, EmeraldConnected.copy(alpha = 0.45f), RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = null,
                            tint = EmeraldConnected,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isBn) "অফলাইন • ইন্টারনেট ছাড়াই" else "Offline • No Internet",
                            style = MaterialTheme.typography.labelMedium,
                            color = EmeraldConnected
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = padState.activeControllerName?.let {
                        if (isBn) "সংযুক্ত গেমপ্যাড: $it" else "Gamepad Ready: $it"
                    } ?: if (isBn) {
                        "ফোনের TV Pad ব্লুটুথ দিয়ে কানেক্ট করে নিচের যেকোনো গেম খেলুন"
                    } else {
                        "Connect your phone's TV Pad via Bluetooth and pick a game below"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (padState.activeControllerName != null) CyanGlow else TextSecondary
                )
            }

            OutlinedButton(
                onClick = onToggleLanguage,
                modifier = Modifier.height(48.dp).testTag("tv_lang_toggle"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
            ) {
                Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isBn) "English" else "বাংলা")
            }
        }

        // Game Cards Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TvGameCard(
                title = if (isBn) "স্পেস স্ট্রাইকার" else "Space Striker",
                subtitle = if (isBn) "আর্কেড স্পেস শ্যুটার" else "Arcade Space Shooter",
                bestLabel = if (isBn) "সেরা স্কোর: ${highScores.spaceStrikerBest}" else "Best: ${highScores.spaceStrikerBest}",
                icon = Icons.Default.RocketLaunch,
                accent = CyanGlow,
                testTag = "card_space_striker",
                onClick = { onNavigate(TvRoute.GAME_SPACE_STRIKER) },
                modifier = Modifier.weight(1f)
            )

            TvGameCard(
                title = if (isBn) "নিওন স্নেক" else "Neon Snake",
                subtitle = if (isBn) "ক্লাসিক গ্রিড স্নেক" else "Classic Grid Arcade",
                bestLabel = if (isBn) "সেরা স্কোর: ${highScores.neonSnakeBest}" else "Best: ${highScores.neonSnakeBest}",
                icon = Icons.Default.Timeline,
                accent = EmeraldConnected,
                testTag = "card_neon_snake",
                onClick = { onNavigate(TvRoute.GAME_NEON_SNAKE) },
                modifier = Modifier.weight(1f)
            )

            TvGameCard(
                title = if (isBn) "ব্রিক ব্রেকার" else "Brick Breaker",
                subtitle = if (isBn) "প্যাডল ও বল আর্কেড" else "Paddle & Ball Arcade",
                bestLabel = if (isBn) "সেরা স্কোর: ${highScores.brickBreakerBest}" else "Best: ${highScores.brickBreakerBest}",
                icon = Icons.Default.GridView,
                accent = VioletNeon,
                testTag = "card_brick_breaker",
                onClick = { onNavigate(TvRoute.GAME_BRICK_BREAKER) },
                modifier = Modifier.weight(1f)
            )
        }

        // Gamepad Input Tester Card
        TvGameCard(
            title = if (isBn) "ব্লুটুথ গেমপ্যাড টেস্টার (TV Pad চেক করুন)" else "Bluetooth Gamepad Input Tester",
            subtitle = if (isBn) {
                "আপনার ফোনের TV Pad বাটন ও জয়স্টিক টিভিতে ঠিকমতো কাজ করছে কিনা এখানে সরাসরি দেখুন"
            } else {
                "Verify every button, D-pad direction, and analog stick signal from your TV Pad"
            },
            bestLabel = if (isBn) "খুলতে A বাটনে চাপ দিন" else "Press A to Open",
            icon = Icons.Default.SportsEsports,
            accent = CoralPulse,
            testTag = "card_input_tester",
            onClick = { onNavigate(TvRoute.INPUT_TESTER) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TvGameCard(
    title: String,
    subtitle: String,
    bestLabel: String,
    icon: ImageVector,
    accent: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isFocused) 1.03f else 1f, label = "focus_scale")
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) accent else ConsoleOutline,
        label = "focus_border"
    )

    Column(
        modifier = modifier
            .scale(scale)
            .testTag(testTag)
            .clip(RoundedCornerShape(22.dp))
            .background(if (isFocused) ConsoleSurfaceElevated else ConsoleSurface)
            .border(width = if (isFocused) 2.5.dp else 1.2.dp, color = borderColor, shape = RoundedCornerShape(22.dp))
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accent,
                modifier = Modifier.size(26.dp)
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = bestLabel,
            style = MaterialTheme.typography.labelLarge,
            color = accent
        )
    }
}
