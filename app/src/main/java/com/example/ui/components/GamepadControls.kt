package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppCopy
import com.example.model.DPadDirection
import com.example.model.GamepadButton
import com.example.model.GamepadInputState
import com.example.model.TvRemoteKey
import com.example.ui.theme.ButtonBlueX
import com.example.ui.theme.ButtonGreenA
import com.example.ui.theme.ButtonRedB
import com.example.ui.theme.ButtonYellowY
import com.example.ui.theme.ConsoleObsidianDeep
import com.example.ui.theme.ConsoleOutline
import com.example.ui.theme.ConsoleSurface
import com.example.ui.theme.ConsoleSurfaceElevated
import com.example.ui.theme.ConsoleSurfaceHighest
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.hypot

@Composable
fun TactileFaceButton(
    button: GamepadButton,
    accentColor: Color,
    isPressed: Boolean,
    size: Dp,
    testTag: String,
    onStateChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val latestCallback by rememberUpdatedState(onStateChange)
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.89f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "face_scale_${button.label}"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isPressed) accentColor else accentColor.copy(alpha = 0.55f),
        label = "face_border_${button.label}"
    )
    val fillBrush = if (isPressed) {
        Brush.radialGradient(
            colors = listOf(accentColor.copy(alpha = 0.45f), ConsoleSurfaceElevated)
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(ConsoleSurfaceHighest, ConsoleSurface)
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .testTag(testTag)
            .semantics { contentDescription = "Button ${button.label}" }
            .clip(CircleShape)
            .background(fillBrush)
            .border(width = if (isPressed) 2.5.dp else 1.5.dp, color = borderColor, shape = CircleShape)
            .pointerInput(button) {
                awaitPointerEventScope {
                    var down = false
                    try {
                        while (true) {
                            val event = awaitPointerEvent()
                            val anyDown = event.changes.any { it.pressed }
                            if (anyDown != down) {
                                down = anyDown
                                latestCallback(anyDown)
                            }
                        }
                    } finally {
                        if (down) latestCallback(false)
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = button.label,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = (size.value * 0.36f).sp,
                fontWeight = FontWeight.Bold
            ),
            color = if (isPressed) TextPrimary else accentColor
        )
    }
}

@Composable
fun DiamondFaceButtons(
    inputState: GamepadInputState,
    buttonSize: Dp,
    onButtonChanged: (GamepadButton, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val gap = (buttonSize.value * 0.16f).dp
    val clusterSize = buttonSize * 2.35f + gap * 2

    Box(
        modifier = modifier.size(clusterSize),
        contentAlignment = Alignment.Center
    ) {
        TactileFaceButton(
            button = GamepadButton.Y,
            accentColor = ButtonYellowY,
            isPressed = inputState.isPressed(GamepadButton.Y),
            size = buttonSize,
            testTag = "btn_y",
            onStateChange = { onButtonChanged(GamepadButton.Y, it) },
            modifier = Modifier.align(Alignment.TopCenter)
        )
        TactileFaceButton(
            button = GamepadButton.X,
            accentColor = ButtonBlueX,
            isPressed = inputState.isPressed(GamepadButton.X),
            size = buttonSize,
            testTag = "btn_x",
            onStateChange = { onButtonChanged(GamepadButton.X, it) },
            modifier = Modifier.align(Alignment.CenterStart)
        )
        TactileFaceButton(
            button = GamepadButton.B,
            accentColor = ButtonRedB,
            isPressed = inputState.isPressed(GamepadButton.B),
            size = buttonSize,
            testTag = "btn_b",
            onStateChange = { onButtonChanged(GamepadButton.B, it) },
            modifier = Modifier.align(Alignment.CenterEnd)
        )
        TactileFaceButton(
            button = GamepadButton.A,
            accentColor = ButtonGreenA,
            isPressed = inputState.isPressed(GamepadButton.A),
            size = buttonSize,
            testTag = "btn_a",
            onStateChange = { onButtonChanged(GamepadButton.A, it) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun TriggerShoulderButton(
    button: GamepadButton,
    isPressed: Boolean,
    testTag: String,
    onStateChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    isPrimaryTrigger: Boolean = false
) {
    val latestCallback by rememberUpdatedState(onStateChange)
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "trigger_scale_${button.label}"
    )
    val accent = if (isPrimaryTrigger) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
    val borderColor by animateColorAsState(
        targetValue = if (isPressed) accent else ConsoleOutline,
        label = "trigger_border_${button.label}"
    )

    Box(
        modifier = modifier
            .height(48.dp)
            .scale(scale)
            .testTag(testTag)
            .semantics { contentDescription = "Shoulder ${button.label}" }
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isPressed) accent.copy(alpha = 0.25f) else ConsoleSurfaceElevated
            )
            .border(
                width = if (isPressed) 2.dp else 1.2.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .pointerInput(button) {
                awaitPointerEventScope {
                    var down = false
                    try {
                        while (true) {
                            val event = awaitPointerEvent()
                            val anyDown = event.changes.any { it.pressed }
                            if (anyDown != down) {
                                down = anyDown
                                latestCallback(anyDown)
                            }
                        }
                    } finally {
                        if (down) latestCallback(false)
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = button.label,
            style = MaterialTheme.typography.labelLarge,
            color = if (isPressed) TextPrimary else accent
        )
    }
}

@Composable
fun CrossDPad(
    currentDirection: DPadDirection,
    size: Dp,
    onDirectionChanged: (DPadDirection) -> Unit,
    modifier: Modifier = Modifier
) {
    val latestCallback by rememberUpdatedState(onDirectionChanged)
    val glowColor = MaterialTheme.colorScheme.primary
    val surfaceColor = ConsoleSurfaceElevated
    val deepColor = ConsoleObsidianDeep
    val outlineColor = ConsoleOutline

    Box(
        modifier = modifier
            .size(size)
            .testTag("dpad_pad")
            .semantics { contentDescription = "Directional Pad" }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    var activePointer: PointerId? = null
                    try {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (activePointer == null) {
                                val down = event.changes.firstOrNull { it.changedToDown() }
                                if (down != null) {
                                    activePointer = down.id
                                    down.consume()
                                    latestCallback(resolveDPadDirection(down.position, this.size))
                                }
                            } else {
                                val tracked = event.changes.firstOrNull { it.id == activePointer }
                                if (tracked == null || !tracked.pressed) {
                                    activePointer = null
                                    latestCallback(DPadDirection.CENTER)
                                } else {
                                    tracked.consume()
                                    latestCallback(resolveDPadDirection(tracked.position, this.size))
                                }
                            }
                        }
                    } finally {
                        latestCallback(DPadDirection.CENTER)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h / 2f
            val outerRadius = minOf(w, h) * 0.48f

            // Ambient disc
            drawCircle(
                color = deepColor.copy(alpha = 0.7f),
                radius = outerRadius,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = outlineColor.copy(alpha = 0.6f),
                radius = outerRadius,
                center = Offset(cx, cy),
                style = Stroke(width = 2.dp.toPx())
            )

            val armThickness = outerRadius * 0.64f
            val armHalf = armThickness / 2f
            val armExtent = outerRadius * 0.92f
            val corner = CornerRadius(10.dp.toPx(), 10.dp.toPx())

            // Base cross
            drawRoundRect(
                color = surfaceColor,
                topLeft = Offset(cx - armHalf, cy - armExtent),
                size = Size(armThickness, armExtent * 2f),
                cornerRadius = corner
            )
            drawRoundRect(
                color = surfaceColor,
                topLeft = Offset(cx - armExtent, cy - armHalf),
                size = Size(armExtent * 2f, armThickness),
                cornerRadius = corner
            )

            val upActive = currentDirection in setOf(DPadDirection.UP, DPadDirection.UP_LEFT, DPadDirection.UP_RIGHT)
            val downActive = currentDirection in setOf(DPadDirection.DOWN, DPadDirection.DOWN_LEFT, DPadDirection.DOWN_RIGHT)
            val leftActive = currentDirection in setOf(DPadDirection.LEFT, DPadDirection.UP_LEFT, DPadDirection.DOWN_LEFT)
            val rightActive = currentDirection in setOf(DPadDirection.RIGHT, DPadDirection.UP_RIGHT, DPadDirection.DOWN_RIGHT)

            // Active arm glows
            if (upActive) {
                drawRoundRect(
                    color = glowColor.copy(alpha = 0.35f),
                    topLeft = Offset(cx - armHalf, cy - armExtent),
                    size = Size(armThickness, armExtent),
                    cornerRadius = corner
                )
            }
            if (downActive) {
                drawRoundRect(
                    color = glowColor.copy(alpha = 0.35f),
                    topLeft = Offset(cx - armHalf, cy),
                    size = Size(armThickness, armExtent),
                    cornerRadius = corner
                )
            }
            if (leftActive) {
                drawRoundRect(
                    color = glowColor.copy(alpha = 0.35f),
                    topLeft = Offset(cx - armExtent, cy - armHalf),
                    size = Size(armExtent, armThickness),
                    cornerRadius = corner
                )
            }
            if (rightActive) {
                drawRoundRect(
                    color = glowColor.copy(alpha = 0.35f),
                    topLeft = Offset(cx, cy - armHalf),
                    size = Size(armExtent, armThickness),
                    cornerRadius = corner
                )
            }

            // Directional chevrons
            val chevronDist = armExtent * 0.66f
            val chevSize = armThickness * 0.24f

            drawChevron(Offset(cx, cy - chevronDist), chevSize, 0f, if (upActive) glowColor else TextSecondary)
            drawChevron(Offset(cx + chevronDist, cy), chevSize, 90f, if (rightActive) glowColor else TextSecondary)
            drawChevron(Offset(cx, cy + chevronDist), chevSize, 180f, if (downActive) glowColor else TextSecondary)
            drawChevron(Offset(cx - chevronDist, cy), chevSize, 270f, if (leftActive) glowColor else TextSecondary)

            // Center pivot recess
            drawCircle(
                color = deepColor,
                radius = armHalf * 0.45f,
                center = Offset(cx, cy)
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawChevron(
    center: Offset,
    size: Float,
    rotationDeg: Float,
    color: Color
) {
    val path = Path()
    when (rotationDeg) {
        0f -> {
            path.moveTo(center.x, center.y - size * 0.6f)
            path.lineTo(center.x + size, center.y + size * 0.5f)
            path.lineTo(center.x - size, center.y + size * 0.5f)
        }
        90f -> {
            path.moveTo(center.x + size * 0.6f, center.y)
            path.lineTo(center.x - size * 0.5f, center.y - size)
            path.lineTo(center.x - size * 0.5f, center.y + size)
        }
        180f -> {
            path.moveTo(center.x, center.y + size * 0.6f)
            path.lineTo(center.x + size, center.y - size * 0.5f)
            path.lineTo(center.x - size, center.y - size * 0.5f)
        }
        else -> {
            path.moveTo(center.x - size * 0.6f, center.y)
            path.lineTo(center.x + size * 0.5f, center.y - size)
            path.lineTo(center.x + size * 0.5f, center.y + size)
        }
    }
    path.close()
    drawPath(path = path, color = color)
}

private fun resolveDPadDirection(pos: Offset, size: IntSize): DPadDirection {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val dx = pos.x - cx
    val dy = pos.y - cy
    val radius = minOf(size.width, size.height) / 2f
    val dist = hypot(dx, dy)
    if (dist < radius * 0.16f) return DPadDirection.CENTER

    // Angle in degrees where 0 = UP, 90 = RIGHT, 180 = DOWN, 270 = LEFT
    var deg = Math.toDegrees(atan2(dx.toDouble(), -dy.toDouble()))
    if (deg < 0) deg += 360.0

    return when {
        deg >= 337.5 || deg < 22.5 -> DPadDirection.UP
        deg < 67.5 -> DPadDirection.UP_RIGHT
        deg < 112.5 -> DPadDirection.RIGHT
        deg < 157.5 -> DPadDirection.DOWN_RIGHT
        deg < 202.5 -> DPadDirection.DOWN
        deg < 247.5 -> DPadDirection.DOWN_LEFT
        deg < 292.5 -> DPadDirection.LEFT
        else -> DPadDirection.UP_LEFT
    }
}

@Composable
fun AnalogJoystick(
    label: String,
    deadzonePercent: Int,
    size: Dp,
    testTag: String,
    onStickMoved: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val latestCallback by rememberUpdatedState(onStickMoved)
    var knobOffsetNorm by remember { mutableStateOf(Offset.Zero) }
    var isActive by remember { mutableStateOf(false) }

    val animX by animateFloatAsState(
        targetValue = knobOffsetNorm.x,
        animationSpec = spring(stiffness = if (isActive) Spring.StiffnessHigh else Spring.StiffnessMediumLow),
        label = "stick_x_$label"
    )
    val animY by animateFloatAsState(
        targetValue = knobOffsetNorm.y,
        animationSpec = spring(stiffness = if (isActive) Spring.StiffnessHigh else Spring.StiffnessMediumLow),
        label = "stick_y_$label"
    )

    val glowColor = MaterialTheme.colorScheme.primary
    val baseColor = ConsoleObsidianDeep
    val ringColor = ConsoleOutline
    val capCenterColor = ConsoleSurfaceHighest
    val capEdgeColor = ConsoleSurface

    Box(
        modifier = modifier
            .size(size)
            .testTag(testTag)
            .semantics { contentDescription = "Analog Stick $label" }
            .pointerInput(label) {
                awaitPointerEventScope {
                    var activePointer: PointerId? = null
                    try {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (activePointer == null) {
                                val down = event.changes.firstOrNull { it.changedToDown() }
                                if (down != null) {
                                    activePointer = down.id
                                    isActive = true
                                    down.consume()
                                    val norm = computeNormalizedStick(down.position, this.size)
                                    knobOffsetNorm = norm
                                    latestCallback(norm.x, norm.y)
                                }
                            } else {
                                val tracked = event.changes.firstOrNull { it.id == activePointer }
                                if (tracked == null || !tracked.pressed) {
                                    activePointer = null
                                    isActive = false
                                    knobOffsetNorm = Offset.Zero
                                    latestCallback(0f, 0f)
                                } else {
                                    tracked.consume()
                                    val norm = computeNormalizedStick(tracked.position, this.size)
                                    knobOffsetNorm = norm
                                    latestCallback(norm.x, norm.y)
                                }
                            }
                        }
                    } finally {
                        isActive = false
                        knobOffsetNorm = Offset.Zero
                        latestCallback(0f, 0f)
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val outerRadius = minOf(this.size.width, this.size.height) * 0.46f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val knobRadius = outerRadius * 0.44f
            val travelRadius = outerRadius - knobRadius * 0.65f

            // Well background
            drawCircle(color = baseColor, radius = outerRadius, center = center)
            drawCircle(
                color = if (isActive) glowColor.copy(alpha = 0.7f) else ringColor,
                radius = outerRadius,
                center = center,
                style = Stroke(width = if (isActive) 2.5.dp.toPx() else 1.5.dp.toPx())
            )

            // Crosshair guides
            drawLine(
                color = ringColor.copy(alpha = 0.45f),
                start = Offset(center.x - outerRadius * 0.7f, center.y),
                end = Offset(center.x + outerRadius * 0.7f, center.y),
                strokeWidth = 1.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = ringColor.copy(alpha = 0.45f),
                start = Offset(center.x, center.y - outerRadius * 0.7f),
                end = Offset(center.x, center.y + outerRadius * 0.7f),
                strokeWidth = 1.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Deadzone ring
            val dzFraction = (deadzonePercent / 100f).coerceIn(0.05f, 0.35f)
            drawCircle(
                color = glowColor.copy(alpha = 0.16f),
                radius = travelRadius * dzFraction,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            val knobCenter = Offset(
                x = center.x + animX * travelRadius,
                y = center.y + animY * travelRadius
            )

            // Stick shaft shadow line
            if (isActive) {
                drawLine(
                    color = glowColor.copy(alpha = 0.5f),
                    start = center,
                    end = knobCenter,
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Thumbstick cap
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(capCenterColor, capEdgeColor),
                    center = knobCenter,
                    radius = knobRadius
                ),
                radius = knobRadius,
                center = knobCenter
            )
            drawCircle(
                color = if (isActive) glowColor else ringColor,
                radius = knobRadius,
                center = knobCenter,
                style = Stroke(width = if (isActive) 2.2.dp.toPx() else 1.5.dp.toPx())
            )
            drawCircle(
                color = if (isActive) glowColor.copy(alpha = 0.25f) else Color.Transparent,
                radius = knobRadius * 0.55f,
                center = knobCenter
            )
        }

        if (!isActive) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary.copy(alpha = 0.65f)
            )
        }
    }
}

private fun computeNormalizedStick(pos: Offset, size: IntSize): Offset {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val maxRadius = minOf(size.width, size.height) * 0.36f
    if (maxRadius <= 0f) return Offset.Zero
    val dx = pos.x - cx
    val dy = pos.y - cy
    val dist = hypot(dx, dy)
    return if (dist > maxRadius) {
        Offset(dx / dist, dy / dist)
    } else {
        Offset(dx / maxRadius, dy / maxRadius)
    }
}

@Composable
fun CenterSystemCluster(
    inputState: GamepadInputState,
    onButtonChanged: (GamepadButton, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SystemPillButton(
            label = "SELECT",
            isPressed = inputState.isPressed(GamepadButton.SELECT),
            testTag = "btn_select",
            onStateChange = { onButtonChanged(GamepadButton.SELECT, it) }
        )
        SystemHomeButton(
            isPressed = inputState.isPressed(GamepadButton.HOME),
            testTag = "btn_home",
            onStateChange = { onButtonChanged(GamepadButton.HOME, it) }
        )
        SystemPillButton(
            label = "START",
            isPressed = inputState.isPressed(GamepadButton.START),
            testTag = "btn_start",
            onStateChange = { onButtonChanged(GamepadButton.START, it) }
        )
    }
}

@Composable
private fun SystemPillButton(
    label: String,
    isPressed: Boolean,
    testTag: String,
    onStateChange: (Boolean) -> Unit
) {
    val latestCallback by rememberUpdatedState(onStateChange)
    val accent = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .height(48.dp)
            .width(74.dp)
            .testTag(testTag)
            .semantics { contentDescription = "$label Button" }
            .padding(vertical = 7.dp)
            .clip(RoundedCornerShape(50))
            .background(if (isPressed) accent.copy(alpha = 0.3f) else ConsoleSurfaceElevated)
            .border(
                width = 1.dp,
                color = if (isPressed) accent else ConsoleOutline,
                shape = RoundedCornerShape(50)
            )
            .pointerInput(label) {
                awaitPointerEventScope {
                    var down = false
                    try {
                        while (true) {
                            val event = awaitPointerEvent()
                            val anyDown = event.changes.any { it.pressed }
                            if (anyDown != down) {
                                down = anyDown
                                latestCallback(anyDown)
                            }
                        }
                    } finally {
                        if (down) latestCallback(false)
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = if (isPressed) TextPrimary else TextSecondary
        )
    }
}

@Composable
private fun SystemHomeButton(
    isPressed: Boolean,
    testTag: String,
    onStateChange: (Boolean) -> Unit
) {
    val latestCallback by rememberUpdatedState(onStateChange)
    val accent = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .size(48.dp)
            .testTag(testTag)
            .semantics { contentDescription = "Gamepad Home Button" }
            .clip(CircleShape)
            .background(if (isPressed) accent.copy(alpha = 0.3f) else ConsoleSurfaceHighest)
            .border(
                width = 1.5.dp,
                color = if (isPressed) accent else ConsoleOutline,
                shape = CircleShape
            )
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    var down = false
                    try {
                        while (true) {
                            val event = awaitPointerEvent()
                            val anyDown = event.changes.any { it.pressed }
                            if (anyDown != down) {
                                down = anyDown
                                latestCallback(anyDown)
                            }
                        }
                    } finally {
                        if (down) latestCallback(false)
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.SportsEsports,
            contentDescription = null,
            tint = if (isPressed) accent else TextPrimary,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun TvRemoteActionStrip(
    copy: AppCopy,
    onKeyTap: (TvRemoteKey) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TvQuickKeyButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            label = copy.tvBackBtn,
            testTag = "tv_key_back",
            onClick = { onKeyTap(TvRemoteKey.BACK) },
            modifier = Modifier.weight(1f)
        )
        TvQuickKeyButton(
            icon = Icons.Default.Home,
            label = copy.tvHomeBtn,
            testTag = "tv_key_home",
            onClick = { onKeyTap(TvRemoteKey.HOME) },
            modifier = Modifier.weight(1f)
        )
        TvQuickKeyButton(
            icon = Icons.AutoMirrored.Filled.VolumeDown,
            label = copy.tvVolDownBtn,
            testTag = "tv_key_voldown",
            onClick = { onKeyTap(TvRemoteKey.VOLUME_DOWN) },
            modifier = Modifier.weight(1f)
        )
        TvQuickKeyButton(
            icon = Icons.AutoMirrored.Filled.VolumeUp,
            label = copy.tvVolUpBtn,
            testTag = "tv_key_volup",
            onClick = { onKeyTap(TvRemoteKey.VOLUME_UP) },
            modifier = Modifier.weight(1f)
        )
        TvQuickKeyButton(
            icon = Icons.Default.PlayArrow,
            label = copy.tvPlayPauseBtn,
            testTag = "tv_key_play",
            onClick = { onKeyTap(TvRemoteKey.PLAY_PAUSE) },
            modifier = Modifier.weight(1f)
        )
        TvQuickKeyButton(
            icon = Icons.AutoMirrored.Filled.VolumeMute,
            label = copy.tvMuteBtn,
            testTag = "tv_key_mute",
            onClick = { onKeyTap(TvRemoteKey.MUTE) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TvQuickKeyButton(
    icon: ImageVector,
    label: String,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val latestClick by rememberUpdatedState(onClick)
    var pressed by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .height(54.dp)
            .testTag(testTag)
            .semantics { contentDescription = label }
            .clip(RoundedCornerShape(14.dp))
            .background(if (pressed) CyanGlow.copy(alpha = 0.22f) else ConsoleSurfaceElevated)
            .border(
                width = 1.dp,
                color = if (pressed) CyanGlow else ConsoleOutline,
                shape = RoundedCornerShape(14.dp)
            )
            .pointerInput(label) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val anyDown = event.changes.any { it.pressed }
                        if (anyDown && !pressed) {
                            pressed = true
                            latestClick()
                        } else if (!anyDown && pressed) {
                            pressed = false
                        }
                    }
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (pressed) CyanGlow else TextPrimary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            maxLines = 1
        )
    }
}
