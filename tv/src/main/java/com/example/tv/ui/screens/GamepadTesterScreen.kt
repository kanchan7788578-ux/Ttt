package com.example.tv.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.tv.model.HardwarePadState
import com.example.tv.model.TvLanguage
import com.example.tv.ui.theme.AmberGold
import com.example.tv.ui.theme.ConsoleObsidian
import com.example.tv.ui.theme.ConsoleObsidianDeep
import com.example.tv.ui.theme.ConsoleOutline
import com.example.tv.ui.theme.ConsoleSurface
import com.example.tv.ui.theme.ConsoleSurfaceElevated
import com.example.tv.ui.theme.CoralPulse
import com.example.tv.ui.theme.CyanGlow
import com.example.tv.ui.theme.EmeraldConnected
import com.example.tv.ui.theme.TextMuted
import com.example.tv.ui.theme.TextPrimary
import com.example.tv.ui.theme.TextSecondary

@Composable
fun GamepadTesterScreen(
    padState: HardwarePadState,
    language: TvLanguage,
    onExit: () -> Unit
) {
    BackHandler { onExit() }
    val isBn = language == TvLanguage.BENGALI

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ConsoleObsidianDeep)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isBn) "গেমপ্যাড ইনপুট টেস্টার" else "Gamepad Input Tester",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary
                )
                Text(
                    text = padState.activeControllerName?.let {
                        if (isBn) "সক্রিয় কন্ট্রোলার: $it" else "Active Controller: $it"
                    } ?: if (isBn) {
                        "ফোনের TV Pad থেকে যেকোনো বাটন চাপুন..."
                    } else {
                        "Press any button on your connected TV Pad..."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (padState.activeControllerName != null) EmeraldConnected else TextSecondary
                )
            }
            OutlinedButton(
                onClick = onExit,
                modifier = Modifier.height(48.dp).testTag("exit_tester_btn"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
            ) {
                Text(if (isBn) "ফিরে যান" else "Back")
            }
        }

        Row(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // D-Pad & Triggers
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(ConsoleSurface)
                    .border(1.dp, ConsoleOutline, RoundedCornerShape(20.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("D-PAD & TRIGGERS", style = MaterialTheme.typography.labelLarge, color = CyanGlow)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TesterChip("L2", padState.buttonL2)
                    TesterChip("L1", padState.buttonL1)
                    TesterChip("R1", padState.buttonR1)
                    TesterChip("R2", padState.buttonR2)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TesterChip("UP", padState.up)
                    TesterChip("DOWN", padState.down)
                    TesterChip("LEFT", padState.left)
                    TesterChip("RIGHT", padState.right)
                }
                Text(
                    text = "L-Stick: (${"%.2f".format(padState.leftX)}, ${"%.2f".format(padState.leftY)})",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Text(
                    text = "R-Stick: (${"%.2f".format(padState.rightX)}, ${"%.2f".format(padState.rightY)})",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }

            // Face & System Buttons
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(ConsoleSurface)
                    .border(1.dp, ConsoleOutline, RoundedCornerShape(20.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("ACTION & SYSTEM BUTTONS", style = MaterialTheme.typography.labelLarge, color = CoralPulse)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TesterCircle("A", padState.buttonA, EmeraldConnected)
                    TesterCircle("B", padState.buttonB, CoralPulse)
                    TesterCircle("X", padState.buttonX, CyanGlow)
                    TesterCircle("Y", padState.buttonY, AmberGold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TesterChip("SELECT", padState.buttonSelect)
                    TesterChip("START", padState.buttonStart)
                }
            }
        }
    }
}

@Composable
private fun TesterChip(label: String, active: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (active) CyanGlow.copy(alpha = 0.25f) else ConsoleSurfaceElevated)
            .border(
                width = if (active) 1.5.dp else 1.dp,
                color = if (active) CyanGlow else ConsoleOutline,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (active) CyanGlow else TextMuted
        )
    }
}

@Composable
private fun TesterCircle(label: String, active: Boolean, color: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(if (active) color.copy(alpha = 0.35f) else ConsoleObsidian)
            .border(
                width = if (active) 2.5.dp else 1.5.dp,
                color = if (active) color else ConsoleOutline,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            color = if (active) TextPrimary else color
        )
    }
}
