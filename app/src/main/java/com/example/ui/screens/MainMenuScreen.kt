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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.GameAudioManager
import com.example.data.GameRepository
import com.example.game.model.ArenaId
import com.example.game.model.ArenaRegistry
import com.example.game.model.GameMode
import com.example.game.model.HeroId
import com.example.game.model.HeroRegistry
import com.example.ui.components.TopCurrencyBar
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MainMenuScreen(
    repository: GameRepository,
    audioManager: GameAudioManager,
    onStartBattle: () -> Unit,
    onOpenHeroes: () -> Unit,
    onOpenShop: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val progression = repository.progression.value
    val currentHeroDef = HeroRegistry.getDef(progression.selectedHeroId)
    val currentHeroProgress = progression.heroes[progression.selectedHeroId] ?: com.example.game.model.HeroProgress(progression.selectedHeroId)

    // Breathing / floating hero idle animation
    val infiniteTransition = rememberInfiniteTransition(label = "hero_idle")
    val heroFloatingY by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_float"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0914))
            .testTag("main_menu_screen")
    ) {
        // Animated background mystical ember particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val t = (System.currentTimeMillis() % 100000) / 1000f
            for (i in 0 until 24) {
                val seed = i * 43.17f
                val px = (seed * 89f) % size.width
                val py = (size.height - ((t * 40f + seed * 50f) % (size.height + 100f)))
                val r = 2.5f + (i % 3) * 1.5f
                val color = if (i % 2 == 0) Color(0xFFFF5722) else Color(0xFF7C4DFF)
                drawCircle(
                    color = color.copy(alpha = 0.25f),
                    radius = r,
                    center = Offset(px, py)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 28.dp, bottom = 70.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Currency Bar
            TopCurrencyBar(
                progression = progression,
                onShopClick = onOpenShop,
                onSettingsClick = onOpenSettings
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Game Logo Title
            Text(
                text = "SHADOW CLASH",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp
            )
            Text(
                text = "ARENA OF HEROES",
                color = Color(0xFFFFD54F),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Center Character Showcase with Idle Animation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background elemental aura circle
                Canvas(
                    modifier = Modifier
                        .size(190.dp)
                        .offset(y = heroFloatingY.dp)
                ) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(currentHeroDef.glowColor).copy(alpha = glowAlpha),
                                Color(currentHeroDef.primaryColor).copy(alpha = 0.2f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = 90.dp.toPx()
                        ),
                        radius = 90.dp.toPx(),
                        center = center
                    )

                    // Hero character body
                    drawCircle(
                        color = Color(currentHeroDef.primaryColor),
                        radius = 44.dp.toPx(),
                        center = center
                    )
                    drawCircle(
                        color = Color(currentHeroDef.secondaryColor),
                        radius = 32.dp.toPx(),
                        center = center
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 44.dp.toPx(),
                        center = center,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // Glowing rune symbol in center
                    drawCircle(
                        color = Color(currentHeroDef.glowColor),
                        radius = 14.dp.toPx(),
                        center = center
                    )
                }

                // Arrow buttons to quickly cycle selected hero
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val all = HeroRegistry.allHeroes
                            val currentIdx = all.indexOfFirst { it.id == currentHeroDef.id }
                            val prevIdx = if (currentIdx <= 0) all.size - 1 else currentIdx - 1
                            val prevId = all[prevIdx].id
                            if (progression.heroes[prevId]?.isUnlocked == true) {
                                repository.selectHero(prevId)
                                audioManager.playSound(GameAudioManager.SoundType.CLICK)
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0x661F1B38))
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Hero", tint = Color.White)
                    }

                    IconButton(
                        onClick = {
                            val all = HeroRegistry.allHeroes
                            val currentIdx = all.indexOfFirst { it.id == currentHeroDef.id }
                            val nextIdx = (currentIdx + 1) % all.size
                            val nextId = all[nextIdx].id
                            if (progression.heroes[nextId]?.isUnlocked == true) {
                                repository.selectHero(nextId)
                                audioManager.playSound(GameAudioManager.SoundType.CLICK)
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0x661F1B38))
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Hero", tint = Color.White)
                    }
                }
            }

            // Hero Info Card
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xCC18162A))
                    .border(1.dp, Color(currentHeroDef.glowColor).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .clickable { onOpenHeroes() }
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = currentHeroDef.name,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(currentHeroDef.element.hexColor).copy(alpha = 0.3f))
                            .border(1.dp, Color(currentHeroDef.element.hexColor), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = currentHeroDef.element.label.uppercase(),
                            color = Color(currentHeroDef.element.hexColor),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "Level ${currentHeroProgress.level}  •  ${currentHeroDef.title}",
                    color = Color(0xFFB0BEC5),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Game Mode Selection Cards
            Text(
                text = "SELECT GAME MODE",
                color = Color(0xFFD1C4E9),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val modes = listOf(
                    Triple(GameMode.STORY, "STORY", Icons.Default.Whatshot),
                    Triple(GameMode.ENDLESS, "ENDLESS", Icons.Default.SportsKabaddi),
                    Triple(GameMode.TRAINING, "TRAIN", Icons.Default.Shield)
                )

                modes.forEach { (mode, label, icon) ->
                    val isSelected = progression.selectedGameMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF281E48) else Color(0xFF131122))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color(0xFFFFD54F) else Color(0xFF2A2742),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                repository.selectGameMode(mode)
                                audioManager.playSound(GameAudioManager.SoundType.CLICK)
                            }
                            .padding(vertical = 10.dp)
                            .testTag("mode_${mode.name}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) Color(0xFFFFD54F) else Color(0xFF90A4AE),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else Color(0xFF90A4AE),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Arena Selection Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ArenaRegistry.allArenas.forEach { arena ->
                    val isSelected = progression.selectedArenaId == arena.id
                    val isUnlocked = progression.unlockedArenas.contains(arena.id)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0xFF1F1B3B) else Color(0xFF12101F))
                            .border(
                                width = if (isSelected) 1.5.dp else 0.5.dp,
                                color = if (isSelected) Color(arena.glowColor) else Color(0xFF26223D),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable(enabled = isUnlocked) {
                                repository.selectArena(arena.id)
                                audioManager.playSound(GameAudioManager.SoundType.CLICK)
                            }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (!isUnlocked) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = Color(0xFFFF5252),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = arena.name,
                                color = if (isUnlocked) (if (isSelected) Color.White else Color(0xFFB0BEC5)) else Color(0x66B0BEC5),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main "ENTER BATTLE" Action Button
            Button(
                onClick = {
                    audioManager.playSound(GameAudioManager.SoundType.CLICK)
                    audioManager.vibrate(40, 200)
                    onStartBattle()
                },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(58.dp)
                    .shadow(16.dp, RoundedCornerShape(29.dp))
                    .testTag("play_battle_button"),
                shape = RoundedCornerShape(29.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFFF3D00), Color(0xFFFF9100), Color(0xFFFFD600))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color(0xFF1E0800),
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ENTER BATTLE",
                            color = Color(0xFF1E0800),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}
