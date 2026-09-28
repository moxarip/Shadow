package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.game.model.Vector2
import kotlin.math.sqrt

@Composable
fun VirtualJoystick(
    modifier: Modifier = Modifier,
    onMove: (Vector2) -> Unit
) {
    val sizeDp = 130.dp
    val maxRadiusPx = 110f
    var knobOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .size(sizeDp)
            .testTag("virtual_joystick")
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val delta = offset - center
                        val dist = sqrt(delta.x * delta.x + delta.y * delta.y)
                        val clamped = if (dist > maxRadiusPx) {
                            delta * (maxRadiusPx / dist)
                        } else {
                            delta
                        }
                        knobOffset = clamped
                        onMove(Vector2(clamped.x / maxRadiusPx, clamped.y / maxRadiusPx))
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = knobOffset + dragAmount
                        val dist = sqrt(newOffset.x * newOffset.x + newOffset.y * newOffset.y)
                        val clamped = if (dist > maxRadiusPx) {
                            newOffset * (maxRadiusPx / dist)
                        } else {
                            newOffset
                        }
                        knobOffset = clamped
                        onMove(Vector2(clamped.x / maxRadiusPx, clamped.y / maxRadiusPx))
                    },
                    onDragEnd = {
                        knobOffset = Offset.Zero
                        onMove(Vector2(0f, 0f))
                    },
                    onDragCancel = {
                        knobOffset = Offset.Zero
                        onMove(Vector2(0f, 0f))
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(sizeDp)) {
            val center = Offset(size.width / 2f, size.height / 2f)

            // Outer Base Ring
            drawCircle(
                color = Color(0x33000000),
                radius = maxRadiusPx,
                center = center
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x554A148C), Color(0x221A237E)),
                    center = center,
                    radius = maxRadiusPx
                ),
                radius = maxRadiusPx,
                center = center
            )
            drawCircle(
                color = Color(0x8800E5FF),
                radius = maxRadiusPx,
                center = center,
                style = Stroke(width = 2.5f)
            )

            // Inner directional cross markers
            val markerColor = Color(0x44FFFFFF)
            drawLine(
                color = markerColor,
                start = Offset(center.x - maxRadiusPx * 0.7f, center.y),
                end = Offset(center.x + maxRadiusPx * 0.7f, center.y),
                strokeWidth = 1.5f
            )
            drawLine(
                color = markerColor,
                start = Offset(center.x, center.y - maxRadiusPx * 0.7f),
                end = Offset(center.x, center.y + maxRadiusPx * 0.7f),
                strokeWidth = 1.5f
            )

            // Thumb Knob
            val knobCenter = center + knobOffset
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF00E5FF), Color(0xFF7C4DFF)),
                    center = knobCenter,
                    radius = 36f
                ),
                radius = 34f,
                center = knobCenter
            )
            drawCircle(
                color = Color.White,
                radius = 34f,
                center = knobCenter,
                style = Stroke(width = 2.5f)
            )
            drawCircle(
                color = Color.White,
                radius = 10f,
                center = knobCenter
            )
        }
    }
}
