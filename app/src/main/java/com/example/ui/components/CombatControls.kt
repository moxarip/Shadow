package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.HeroDef

@Composable
fun CombatControls(
    modifier: Modifier = Modifier,
    heroDef: HeroDef,
    dashCooldownRemaining: Float,
    dashCooldownMax: Float,
    abilityCooldownRemaining: Float,
    abilityCooldownMax: Float,
    onAttackPress: (Boolean) -> Unit,
    onDashClick: () -> Unit,
    onAbilityClick: () -> Unit
) {
    val abilityReady = abilityCooldownRemaining <= 0.05f
    val dashReady = dashCooldownRemaining <= 0.05f

    Box(
        modifier = modifier.size(190.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        // Special Ability Button (Top-Left of cluster)
        Box(
            modifier = Modifier
                .offset(x = (-85).dp, y = (-75).dp)
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(heroDef.glowColor),
                            Color(heroDef.primaryColor).copy(alpha = 0.85f),
                            Color(0xFF1F1D36)
                        )
                    )
                )
                .border(
                    width = if (abilityReady) 2.5.dp else 1.dp,
                    color = if (abilityReady) Color(heroDef.glowColor) else Color(0x66FFFFFF),
                    shape = CircleShape
                )
                .testTag("ability_button")
                .clickable(
                    enabled = abilityReady,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    onAbilityClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = heroDef.abilityName,
                tint = if (abilityReady) Color.White else Color(0x88FFFFFF),
                modifier = Modifier.size(28.dp)
            )

            if (!abilityReady) {
                // Cooldown overlay & countdown
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color(0x99000000)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { (abilityCooldownRemaining / abilityCooldownMax).coerceIn(0f, 1f) },
                        modifier = Modifier.matchParentSize(),
                        color = Color(heroDef.glowColor),
                        strokeWidth = 3.dp,
                        trackColor = Color(0x33FFFFFF),
                    )
                    Text(
                        text = "${abilityCooldownRemaining.toInt() + 1}s",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Dash Button (Top-Right of cluster)
        Box(
            modifier = Modifier
                .offset(x = 0.dp, y = (-90).dp)
                .size(54.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF2979FF), Color(0xFF1A237E))
                    )
                )
                .border(
                    width = if (dashReady) 2.dp else 1.dp,
                    color = if (dashReady) Color(0xFF00E5FF) else Color(0x66FFFFFF),
                    shape = CircleShape
                )
                .testTag("dash_button")
                .clickable(
                    enabled = dashReady,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    onDashClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsRun,
                contentDescription = "Dash",
                tint = if (dashReady) Color.White else Color(0x77FFFFFF),
                modifier = Modifier.size(28.dp)
            )

            if (!dashReady) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color(0x99000000)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { (dashCooldownRemaining / dashCooldownMax).coerceIn(0f, 1f) },
                        modifier = Modifier.matchParentSize(),
                        color = Color(0xFF00E5FF),
                        strokeWidth = 3.dp,
                        trackColor = Color(0x33FFFFFF),
                    )
                }
            }
        }

        // Main Primary Attack Button (Center large button)
        Box(
            modifier = Modifier
                .size(76.dp)
                .shadow(12.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFF3D00),
                            Color(0xFFD50000),
                            Color(0xFF3E0000)
                        )
                    )
                )
                .border(3.dp, Color(0xFFFFD54F), CircleShape)
                .testTag("attack_button")
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            onAttackPress(true)
                            tryAwaitRelease()
                            onAttackPress(false)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.FlashOn,
                contentDescription = "Attack",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}
