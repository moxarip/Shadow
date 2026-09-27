package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.game.model.HeroId
import com.example.game.model.HeroProgress
import com.example.game.model.HeroRegistry
import com.example.ui.components.TopCurrencyBar

@Composable
fun HeroSelectScreen(
    repository: GameRepository,
    audioManager: GameAudioManager,
    onBack: () -> Unit,
    onOpenShop: () -> Unit
) {
    val progression = repository.progression.value
    var previewHeroId by remember { mutableStateOf(progression.selectedHeroId) }

    val heroDef = HeroRegistry.getDef(previewHeroId)
    val heroProgress = progression.heroes[previewHeroId] ?: HeroProgress(previewHeroId)
    val isSelected = progression.selectedHeroId == previewHeroId
    val isUnlocked = heroProgress.isUnlocked

    BackHandler {
        onBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090812))
            .testTag("hero_select_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 28.dp, bottom = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar with Back button and Currencies
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                TopCurrencyBar(
                    progression = progression,
                    onShopClick = onOpenShop,
                    onSettingsClick = {},
                    modifier = Modifier.weight(8f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Hero Avatar Large Preview
            Box(
                modifier = Modifier
                    .size(170.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(heroDef.glowColor).copy(alpha = 0.5f),
                                Color(heroDef.primaryColor).copy(alpha = 0.2f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = 80.dp.toPx()
                        ),
                        radius = 80.dp.toPx(),
                        center = center
                    )
                    drawCircle(
                        color = Color(heroDef.primaryColor),
                        radius = 48.dp.toPx(),
                        center = center
                    )
                    drawCircle(
                        color = Color(heroDef.secondaryColor),
                        radius = 36.dp.toPx(),
                        center = center
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 48.dp.toPx(),
                        center = center,
                        style = Stroke(width = 3.dp.toPx())
                    )
                    drawCircle(
                        color = Color(heroDef.glowColor),
                        radius = 16.dp.toPx(),
                        center = center
                    )
                }

                if (!isUnlocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color(0x99000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            // Name & Element
            Text(
                text = heroDef.name,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = heroDef.title,
                color = Color(heroDef.element.hexColor),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Roster Carousel of All 10 Heroes
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(HeroRegistry.allHeroes) { hDef ->
                    val hProg = progression.heroes[hDef.id] ?: HeroProgress(hDef.id)
                    val isCurrent = hDef.id == previewHeroId
                    val isEquipped = progression.selectedHeroId == hDef.id

                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isCurrent) Color(0xFF261D42) else Color(0xFF131122))
                            .border(
                                width = if (isCurrent) 2.dp else 1.dp,
                                color = if (isCurrent) Color(hDef.glowColor) else Color(0xFF2C2742),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                previewHeroId = hDef.id
                                audioManager.playSound(GameAudioManager.SoundType.CLICK)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(32.dp)) {
                            drawCircle(color = Color(hDef.primaryColor), radius = 14.dp.toPx())
                            drawCircle(color = Color(hDef.secondaryColor), radius = 9.dp.toPx())
                        }

                        if (!hProg.isUnlocked) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0x77000000)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = Color(0xAAFFFFFF),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        if (isEquipped) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00E676)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Equipped",
                                    tint = Color.Black,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Card
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141224))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "HERO STATS",
                            color = Color(0xFFFFD54F),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Level ${heroProgress.level} / 10",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    StatRow(label = "Health (HP)", value = "${heroProgress.getHp(heroDef).toInt()}", progress = (heroProgress.getHp(heroDef) / 1000f).coerceIn(0f, 1f), color = Color(0xFFFF5252))
                    StatRow(label = "Damage", value = "${heroProgress.getDamage(heroDef).toInt()}", progress = (heroProgress.getDamage(heroDef) / 150f).coerceIn(0f, 1f), color = Color(0xFFFF9800))
                    StatRow(label = "Move Speed", value = "${heroProgress.getSpeed(heroDef).toInt()}", progress = (heroProgress.getSpeed(heroDef) / 320f).coerceIn(0f, 1f), color = Color(0xFF00E5FF))
                    StatRow(label = "Attack Range", value = if (heroDef.isRanged) "Ranged (${heroDef.attackRange.toInt()})" else "Melee (${heroDef.attackRange.toInt()})", progress = (heroDef.attackRange / 300f).coerceIn(0f, 1f), color = Color(0xFFE040FB))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Special Ability Card
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141224))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "ABILITY: ${heroDef.abilityName.uppercase()}",
                            color = Color(heroDef.glowColor),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "${heroDef.abilityCooldownSec.toInt()}s CD",
                            color = Color(0xFFB0BEC5),
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = heroDef.abilityDesc,
                        color = Color(0xFFCFD8DC),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Actions: Upgrade, Select, Unlock
            if (!isUnlocked) {
                // Unlock with Gems button
                Button(
                    onClick = {
                        val success = repository.unlockHero(heroDef.id)
                        if (success) {
                            audioManager.playSound(GameAudioManager.SoundType.LEVEL_UP)
                        } else {
                            onOpenShop()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(52.dp)
                        .testTag("unlock_hero_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Diamond, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "UNLOCK FOR ${heroDef.unlockGemsCost} GEMS",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.92f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Upgrade Button
                    val canUpgrade = heroProgress.level < 10
                    val upgradeCost = heroProgress.getUpgradeCost()
                    val hasCoins = progression.coins >= upgradeCost

                    Button(
                        onClick = {
                            val success = repository.upgradeHero(heroDef.id)
                            if (success) {
                                audioManager.playSound(GameAudioManager.SoundType.LEVEL_UP)
                                audioManager.vibrate(50, 200)
                            }
                        },
                        enabled = canUpgrade && hasCoins,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("upgrade_hero_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFB300),
                            disabledContainerColor = Color(0xFF2C2417)
                        ),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = if (hasCoins) Color(0xFF1E1400) else Color(0x66FFFFFF), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (canUpgrade) "UPGRADE (${upgradeCost})" else "MAX LEVEL",
                                color = if (hasCoins && canUpgrade) Color(0xFF1E1400) else Color(0x66FFFFFF),
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Select Button
                    Button(
                        onClick = {
                            repository.selectHero(heroDef.id)
                            audioManager.playSound(GameAudioManager.SoundType.CLICK)
                        },
                        enabled = !isSelected,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("select_hero_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E676),
                            disabledContainerColor = Color(0xFF173620)
                        ),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Text(
                            text = if (isSelected) "EQUIPPED" else "SELECT",
                            color = if (isSelected) Color(0x88FFFFFF) else Color(0xFF0A2B14),
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    progress: Float,
    color: Color
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = Color(0xFFB0BEC5), fontSize = 11.sp)
            Text(text = value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Color(0xFF26233B)
        )
    }
}
