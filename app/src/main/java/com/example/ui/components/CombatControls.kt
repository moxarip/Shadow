package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FlashOn
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
import com.example.game.model.CharacterDef

@Composable
fun CombatControls(
    modifier: Modifier = Modifier,
    characterDef: CharacterDef,
    dashCooldownRemaining: Float,
    dashCooldownMax: Float,
    abilityCooldownRemaining: Float,
    abilityCooldownMax: Float,
    onAttackPress: (Boolean) -> Unit,
    onJumpClick: () -> Unit,
    onDashClick: () -> Unit,
    onAbilityClick: () -> Unit
) {
    val abilityReady = abilityCooldownRemaining <= 0.05f
    val dashReady = dashCooldownRemaining <= 0.05f

    Box(
        modifier = modifier.size(210.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        // 1. ABILITY BUTTON (Far upper-left)
        Box(
            modifier = Modifier
                .offset(x = (-135).dp, y = (-55).dp)
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(characterDef.energyColor),
                            Color(characterDef.energyColor).copy(alpha = 0.7f),
                            Color(0xFF1B1832)
                        )
                    )
                )
                .border(
                    width = if (abilityReady) 2.dp else 1.dp,
                    color = if (abilityReady) Color(characterDef.energyColor) else Color(0x66FFFFFF),
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
                contentDescription = characterDef.abilityName,
                tint = if (abilityReady) Color.White else Color(0x77FFFFFF),
                modifier = Modifier.size(24.dp)
            )

            if (!abilityReady) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color(0xAA000000)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { (abilityCooldownRemaining / abilityCooldownMax).coerceIn(0f, 1f) },
                        modifier = Modifier.matchParentSize(),
                        color = Color(characterDef.energyColor),
                        strokeWidth = 3.dp,
                        trackColor = Color(0x33FFFFFF)
                    )
                    Text(
                        text = "${abilityCooldownRemaining.toInt() + 1}s",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 2. DASH BUTTON (Upper-left)
        Box(
            modifier = Modifier
                .offset(x = (-75).dp, y = (-105).dp)
                .size(52.dp)
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
                modifier = Modifier.size(26.dp)
            )

            if (!dashReady) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color(0xAA000000)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { (dashCooldownRemaining / dashCooldownMax).coerceIn(0f, 1f) },
                        modifier = Modifier.matchParentSize(),
                        color = Color(0xFF00E5FF),
                        strokeWidth = 3.dp,
                        trackColor = Color(0x33FFFFFF)
                    )
                }
            }
        }

        // 3. JUMP BUTTON (Upper-right)
        Box(
            modifier = Modifier
                .offset(x = (-10).dp, y = (-115).dp)
                .size(54.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF00E676), Color(0xFF004D40))
                    )
                )
                .border(2.dp, Color(0xFF69F0AE), CircleShape)
                .testTag("jump_button")
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    onJumpClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowUpward,
                contentDescription = "Jump",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        // 4. MAIN ATTACK BUTTON (Bottom-right, large)
        Box(
            modifier = Modifier
                .size(78.dp)
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
                modifier = Modifier.size(42.dp)
            )
        }
    }
}
