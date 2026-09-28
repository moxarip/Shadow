package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.engine.GameEngine

@Composable
fun CombatHUD(
    engine: GameEngine,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        // Top status row: HP/Energy on left, Wave & Coins in middle, Pause on right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player HP & Energy Bars
            Column {
                // HP Bar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "HP",
                        color = Color(0xFFFF5252),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.width(22.dp)
                    )
                    Box(
                        modifier = Modifier
                            .width(130.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(Color(0xFF2E151B))
                            .border(1.dp, Color(0xFF6E2835), RoundedCornerShape(7.dp))
                    ) {
                        val hpFraction = (engine.player.health / engine.player.maxHealth).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(hpFraction)
                                .height(14.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFFF1744), Color(0xFFFF5252), Color(0xFFFF8A80))
                                    )
                                )
                        )
                        Text(
                            text = "${engine.player.health.toInt()} / ${engine.player.maxHealth.toInt()}",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Energy Bar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "EN",
                        color = Color(0xFF00E5FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.width(22.dp)
                    )
                    Box(
                        modifier = Modifier
                            .width(110.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF0D2235))
                    ) {
                        val energyFraction = (engine.player.energy / engine.player.maxEnergy).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(energyFraction)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF00B0FF), Color(0xFF00E5FF))
                                    )
                                )
                        )
                    }
                }
            }

            // Wave & Coins indicator in middle
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xCC1F1B38))
                        .border(1.dp, Color(0xFF534882), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (engine.gameMode == com.example.game.model.GameMode.ENDLESS) {
                            "WAVE ${engine.currentWave}"
                        } else if (engine.gameMode == com.example.game.model.GameMode.TRAINING) {
                            "TRAINING"
                        } else {
                            "WAVE ${engine.currentWave} / ${engine.maxStoryWaves}"
                        },
                        color = Color(0xFFFFD54F),
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = "Coins",
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "+${engine.runCoinsEarned}",
                        color = Color(0xFFFFE082),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Enemies remaining & Pause Button
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xCC3E1929))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "⚔ ${engine.enemies.count { it.isAlive() }}",
                        color = Color(0xFFFF80AB),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onPauseClick,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0x88000000))
                        .border(1.dp, Color(0x66FFFFFF), CircleShape)
                        .testTag("pause_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Boss Epic HP Bar (Shown when boss is present)
        AnimatedVisibility(
            visible = engine.isBossActive && engine.bossReference != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            engine.bossReference?.let { boss ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "👑 ${boss.type.displayName.uppercase()}",
                            color = Color(0xFFFFD700),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "${((boss.health / boss.maxHealth) * 100).toInt()}%",
                            color = Color(0xFFFF5252),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(Color(0xFF2A0808))
                            .border(1.5.dp, Color(0xFFFFD700), RoundedCornerShape(7.dp))
                    ) {
                        val fraction = (boss.health / boss.maxHealth).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction)
                                .height(14.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFD50000), Color(0xFFFF1744), Color(0xFFFF8A80))
                                    )
                                )
                        )
                    }
                }
            }
        }

        // Wave Announcement Banner (Center floating banner)
        if (engine.waveBannerTimer > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0x00000000), Color(0xEE1E1338), Color(0xEE2A1245), Color(0x00000000))
                            )
                        )
                        .border(
                            1.dp,
                            Brush.horizontalGradient(listOf(Color(0x00000000), Color(0xFFFFD54F), Color(0x00000000))),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 32.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = engine.waveBannerText,
                        color = Color(0xFFFFEA00),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                }
            }
        }
    }
}
