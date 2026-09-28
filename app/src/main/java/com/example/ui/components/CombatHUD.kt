package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
        // TOP BAR: HP on left, Stage in center, Pause on right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // TOP-LEFT: Player HP & Energy
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = engine.characterDef.name,
                        color = Color(engine.characterDef.energyColor),
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    // HP Bar
                    Box(
                        modifier = Modifier
                            .width(130.dp)
                            .height(13.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF261215))
                            .border(1.dp, Color(0xFF6E2835), RoundedCornerShape(6.dp))
                    ) {
                        val hpFrac = (engine.player.hp / engine.player.maxHp).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(hpFrac)
                                .height(13.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFFF1744), Color(0xFFFF5252), Color(0xFFFF8A80))
                                    )
                                )
                        )
                        Text(
                            text = "${engine.player.hp.toInt()} / ${engine.player.maxHp.toInt()}",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Energy Bar
                Box(
                    modifier = Modifier
                        .padding(start = 45.dp)
                        .width(95.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF0D2235))
                ) {
                    val energyFrac = (engine.player.energy / engine.player.maxEnergy).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(energyFrac)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF00B0FF), Color(0xFF00E5FF))
                                )
                            )
                    )
                }
            }

            // TOP-CENTER: Stage Info & Coins
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xDD1B1432))
                        .border(1.dp, Color(engine.worldDef.neonAccentColor), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (engine.stageNumber == 4) "STAGE 4 • BOSS FIGHT" else "${engine.worldDef.name.uppercase()} • STAGE ${engine.stageNumber}",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = "Coins",
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "+${engine.coinsCollected}",
                        color = Color(0xFFFFE082),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            // TOP-RIGHT: Pause Button
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
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // COMBO COUNTER DISPLAY (Shows when combo > 1)
        AnimatedVisibility(
            visible = engine.player.comboDisplayCount > 1,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xCC180A22))
                        .border(1.5.dp, Color(0xFFFFD54F), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "COMBO x${engine.player.comboDisplayCount}",
                        color = Color(0xFFFFEA00),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // BOSS EPIC HP BAR (Shown when World Boss is active)
        AnimatedVisibility(
            visible = engine.isBossFightTriggered && engine.activeBoss != null && engine.activeBoss?.isAlive() == true,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            engine.activeBoss?.let { boss ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(0.92f),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👑 ${engine.worldDef.bossDef.name}",
                            color = Color(engine.worldDef.bossDef.color),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "${((boss.hp / boss.maxHp) * 100).toInt()}%",
                            color = Color(0xFFFF5252),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .height(13.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF2E0D12))
                            .border(1.5.dp, Color(engine.worldDef.bossDef.color), RoundedCornerShape(6.dp))
                    ) {
                        val bRatio = (boss.hp / boss.maxHp).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(bRatio)
                                .height(13.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(engine.worldDef.bossDef.color),
                                            Color(0xFFFF1744),
                                            Color(0xFFFF8A80)
                                        )
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}
