package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.AppCopy
import com.example.model.AppLanguage
import com.example.model.HapticStrength
import com.example.model.OrientationPreference
import com.example.ui.theme.ConsoleObsidian
import com.example.ui.theme.ConsoleOutline
import com.example.ui.theme.ConsoleSurface
import com.example.ui.theme.ConsoleSurfaceElevated
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.EmeraldConnected
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.GamepadUiState
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    copy: AppCopy,
    uiState: GamepadUiState,
    onSelectHaptic: (HapticStrength) -> Unit,
    onDeadzoneChanged: (Int) -> Unit,
    onKeepScreenOnChanged: (Boolean) -> Unit,
    onSelectOrientation: (OrientationPreference) -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    val settings = uiState.settings
    val isBn = settings.language == AppLanguage.BENGALI
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ConsoleSurface,
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = copy.settingsTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("close_settings_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = copy.closeBtn,
                        tint = TextSecondary
                    )
                }
            }

            // Live Signal Diagnostics Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ConsoleObsidian)
                    .border(1.dp, ConsoleOutline, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = copy.liveTesterTitle,
                    style = MaterialTheme.typography.titleSmall,
                    color = CyanGlow
                )
                val activeLabels = uiState.inputState.activeButtonLabels()
                Text(
                    text = if (activeLabels.isEmpty()) copy.noActiveInput else activeLabels.joinToString(" + "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (activeLabels.isEmpty()) TextMuted else EmeraldConnected
                )
                Text(
                    text = "L(${"%.2f".format(uiState.inputState.leftX)}, ${"%.2f".format(uiState.inputState.leftY)})  " +
                        "R(${"%.2f".format(uiState.inputState.rightX)}, ${"%.2f".format(uiState.inputState.rightY)})",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Text(
                    text = "${copy.packetsSentLabel} ${uiState.packetsSent}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            // Language Toggle
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = copy.languageLabel, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ChoiceChipBox(
                        label = "বাংলা",
                        selected = settings.language == AppLanguage.BENGALI,
                        testTag = "lang_bn_chip",
                        onClick = { onSelectLanguage(AppLanguage.BENGALI) },
                        modifier = Modifier.weight(1f)
                    )
                    ChoiceChipBox(
                        label = "English",
                        selected = settings.language == AppLanguage.ENGLISH,
                        testTag = "lang_en_chip",
                        onClick = { onSelectLanguage(AppLanguage.ENGLISH) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Haptic Feedback
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = copy.hapticLabel, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HapticStrength.entries.forEach { strength ->
                        ChoiceChipBox(
                            label = copy.hapticName(strength, isBn),
                            selected = settings.hapticStrength == strength,
                            testTag = "haptic_${strength.name.lowercase()}",
                            onClick = { onSelectHaptic(strength) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Analog Stick Deadzone
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = copy.deadzoneLabel, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Text(
                        text = "${settings.deadzonePercent}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = CyanGlow
                    )
                }
                Slider(
                    value = settings.deadzonePercent.toFloat(),
                    onValueChange = { onDeadzoneChanged(it.roundToInt()) },
                    valueRange = 0f..30f,
                    modifier = Modifier.testTag("deadzone_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = CyanGlow,
                        activeTrackColor = CyanGlow,
                        inactiveTrackColor = ConsoleOutline
                    )
                )
            }

            // Screen Orientation Preference
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = copy.orientationLabel, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OrientationPreference.entries.forEach { pref ->
                        ChoiceChipBox(
                            label = copy.orientationName(pref, isBn),
                            selected = settings.orientationPreference == pref,
                            testTag = "orientation_${pref.name.lowercase()}",
                            onClick = { onSelectOrientation(pref) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Keep Screen On
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ConsoleSurfaceElevated)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = copy.keepScreenOnLabel, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = copy.keepScreenOnSub, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
                Switch(
                    checked = settings.keepScreenOn,
                    onCheckedChange = onKeepScreenOnChanged,
                    modifier = Modifier.testTag("keep_screen_on_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ConsoleObsidian,
                        checkedTrackColor = CyanGlow
                    )
                )
            }
        }
    }
}

@Composable
private fun ChoiceChipBox(
    label: String,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .testTag(testTag)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) CyanGlow.copy(alpha = 0.2f) else ConsoleSurfaceElevated)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) CyanGlow else ConsoleOutline,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) CyanGlow else TextSecondary,
            maxLines = 1
        )
    }
}
