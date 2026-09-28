package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.GameAudioManager
import com.example.data.GameRepository
import com.example.game.model.CharacterProgress
import com.example.game.model.CharacterRegistry
import com.example.game.model.WeaponRegistry
import com.example.game.model.WorldRegistry
import com.example.ui.components.TopCurrencyBar

@Composable
fun MainMenuScreen(
    repository: GameRepository,
    audioManager: GameAudioManager,
    onStartBattle: () -> Unit,
    onOpenHeroes: () -> Unit,
    onOpenWeapons: () -> Unit,
    onOpenShop: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val progression = repository.progression.value
    val characterDef = CharacterRegistry.getDef(progression.selectedCharacterId)
    val characterProgress = progression.characters[progression.selectedCharacterId] ?: CharacterProgress(progression.selectedCharacterId)
    val weaponDef = WeaponRegistry.getDef(progression.selectedWeaponType)
    val selectedWorld = WorldRegistry.getDef(progression.selectedWorldId)

    // Breathing / floating character animation
    val infiniteTransition = rememberInfiniteTransition(label = "char_idle")
    val charFloatingY by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "char_float"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080612))
            .verticalScroll(rememberScrollState())
            .padding(bottom = 28.dp)
            .testTag("main_menu_screen")
    ) {
        // TOP CURRENCY BAR
        TopCurrencyBar(
            progression = progression,
            onShopClick = onOpenShop,
            onSettingsClick = onOpenSettings
        )

        // GAME TITLE HEADER
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SHADOW WARRIORS",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                modifier = Modifier.testTag("game_title")
            )
            Text(
                text = "2D ACTION • COMBO COMBAT",
                color = Color(0xFF00E5FF),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.5.sp
            )
        }

        // HERO AVATAR WITH WEAPON SHOWCASE
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background aura
            Canvas(
                modifier = Modifier
                    .size(190.dp)
                    .offset(y = charFloatingY.dp)
            ) {
                val c = Offset(size.width / 2f, size.height / 2f + 10f)
                val s = 1.9f
                val baseCol = Color(characterDef.silhouetteColor)
                val energyCol = Color(characterDef.energyColor)

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            energyCol.copy(alpha = glowAlpha * 0.4f),
                            energyCol.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        center = c,
                        radius = 85.dp.toPx()
                    ),
                    radius = 85.dp.toPx(),
                    center = c
                )

                // Head
                drawCircle(color = baseCol, radius = 10f * s, center = Offset(c.x, c.y - 38f * s))
                drawCircle(color = energyCol, radius = 2.5f * s, center = Offset(c.x + 3.5f * s, c.y - 38f * s))

                // Torso & Core
                drawLine(baseCol, Offset(c.x, c.y - 28f * s), Offset(c.x, c.y - 8f * s), strokeWidth = 5f * s, cap = StrokeCap.Round)
                drawCircle(color = energyCol, radius = 3f * s, center = Offset(c.x, c.y - 20f * s))

                // Legs
                drawLine(baseCol, Offset(c.x, c.y - 8f * s), Offset(c.x - 10f * s, c.y + 16f * s), strokeWidth = 5f * s, cap = StrokeCap.Round)
                drawLine(baseCol, Offset(c.x, c.y - 8f * s), Offset(c.x + 10f * s, c.y + 16f * s), strokeWidth = 5f * s, cap = StrokeCap.Round)

                // Arms holding weapon
                drawLine(baseCol, Offset(c.x, c.y - 26f * s), Offset(c.x + 14f * s, c.y - 12f * s), strokeWidth = 4.5f * s, cap = StrokeCap.Round)
                drawLine(energyCol, Offset(c.x + 14f * s, c.y - 12f * s), Offset(c.x + 38f * s, c.y - 28f * s), strokeWidth = 4.5f * s, cap = StrokeCap.Round)
            }

            // Quick Cycle Arrows
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val all = CharacterRegistry.allCharacters
                        val currentIdx = all.indexOfFirst { it.id == characterDef.id }
                        val prevIdx = if (currentIdx <= 0) all.size - 1 else currentIdx - 1
                        repository.selectCharacter(all[prevIdx].id)
                        audioManager.playSound(GameAudioManager.SoundType.CLICK)
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0x9918152B))
                        .border(1.dp, Color(0x66FFFFFF), CircleShape)
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Warrior", tint = Color.White)
                }

                IconButton(
                    onClick = {
                        val all = CharacterRegistry.allCharacters
                        val currentIdx = all.indexOfFirst { it.id == characterDef.id }
                        val nextIdx = (currentIdx + 1) % all.size
                        repository.selectCharacter(all[nextIdx].id)
                        audioManager.playSound(GameAudioManager.SoundType.CLICK)
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0x9918152B))
                        .border(1.dp, Color(0x66FFFFFF), CircleShape)
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Warrior", tint = Color.White)
                }
            }
        }

        // WARRIOR BADGE CARD
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xCC16142B))
                .border(1.dp, Color(characterDef.energyColor).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                .clickable { onOpenHeroes() }
                .padding(horizontal = 18.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = characterDef.name,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(characterDef.energyColor).copy(alpha = 0.25f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = weaponDef.name.uppercase(),
                        color = Color(characterDef.energyColor),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                text = "Level ${characterProgress.level}  •  ${characterDef.role}",
                color = Color(0xFFB0BEC5),
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // WORLD SELECTION CAROUSEL
        Text(
            text = "SELECT WORLD & CAMPAIGN",
            color = Color(0xFFB0BEC5),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(WorldRegistry.allWorlds) { world ->
                val isSelected = progression.selectedWorldId == world.id
                val isUnlocked = progression.unlockedWorlds.contains(world.id)

                Box(
                    modifier = Modifier
                        .width(135.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) Color(0xFF261D42) else Color(0xFF131024))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Color(world.neonAccentColor) else Color(0xFF2A2445),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable(enabled = isUnlocked) {
                            repository.selectWorldAndStage(world.id, 1)
                            audioManager.playSound(GameAudioManager.SoundType.CLICK)
                        }
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (!isUnlocked) {
                            Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = world.name,
                            color = if (isUnlocked) (if (isSelected) Color.White else Color(0xFF90A4AE)) else Color(0x6690A4AE),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = if (isUnlocked) "4 Stages" else "Lv.${world.unlockLevelRequired}",
                            color = if (isUnlocked) Color(world.neonAccentColor) else Color(0xFFFF5252),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // STAGE SELECTION PILLS (1..4)
        Text(
            text = "${selectedWorld.name.uppercase()} • CHOOSE STAGE",
            color = Color(selectedWorld.neonAccentColor),
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (st in 1..4) {
                val isSelectedStage = progression.selectedStageNumber == st
                val isBossStage = st == 4

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelectedStage) Color(0xFF2F2456) else Color(0xFF141126))
                        .border(
                            width = if (isSelectedStage) 2.dp else 1.dp,
                            color = if (isSelectedStage) Color(if (isBossStage) 0xFFFF1744 else 0xFFFFD54F) else Color(0xFF2E274A),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            repository.selectWorldAndStage(selectedWorld.id, st)
                            audioManager.playSound(GameAudioManager.SoundType.CLICK)
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isBossStage) "BOSS" else "STAGE $st",
                            color = if (isBossStage) Color(0xFFFF5252) else if (isSelectedStage) Color.White else Color(0xFF90A4AE),
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // BIG "ENTER STAGE" ACTION BUTTON
        Button(
            onClick = {
                audioManager.playSound(GameAudioManager.SoundType.CLICK)
                audioManager.vibrate(40, 200)
                onStartBattle()
            },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(58.dp)
                .align(Alignment.CenterHorizontally)
                .shadow(16.dp, RoundedCornerShape(20.dp))
                .testTag("enter_battle_button"),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF00E5FF), Color(0xFF7C4DFF), Color(0xFFFF1744))
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (progression.selectedStageNumber == 4) "FIGHT WORLD BOSS" else "ENTER STAGE ${progression.selectedStageNumber}",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
