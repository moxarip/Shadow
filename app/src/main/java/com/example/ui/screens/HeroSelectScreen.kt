package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.GameAudioManager
import com.example.data.GameRepository
import com.example.game.model.CharacterId
import com.example.game.model.CharacterProgress
import com.example.game.model.CharacterRegistry
import com.example.game.model.WeaponRegistry

@Composable
fun HeroSelectScreen(
    repository: GameRepository,
    audioManager: GameAudioManager,
    onBack: () -> Unit,
    onOpenShop: () -> Unit
) {
    val progression = repository.progression.value
    var viewingCharId by remember { mutableStateOf(progression.selectedCharacterId) }
    var showSkinsDialog by remember { mutableStateOf(false) }

    val characterDef = CharacterRegistry.getDef(viewingCharId)
    val characterProgress = progression.characters[viewingCharId] ?: CharacterProgress(viewingCharId)
    val weaponDef = WeaponRegistry.getDef(characterDef.weaponType)

    BackHandler {
        onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090714))
            .verticalScroll(rememberScrollState())
            .padding(bottom = 28.dp)
            .testTag("hero_select_screen")
    ) {
        // TOP NAVIGATION BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    audioManager.playSound(GameAudioManager.SoundType.CLICK)
                    onBack()
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E1A33))
                    .testTag("back_button")
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Text(
                text = "WARRIORS ROSTER",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )

            // Currencies Display
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1C192E))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("${progression.coins}", color = Color(0xFFFFE082), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1C192E))
                        .clickable { onOpenShop() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Diamond, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("${progression.gems}", color = Color(0xFF80D8FF), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // HORIZONTAL CHARACTER SELECTION CARDS
        Text(
            text = "SELECT FIGHTER",
            color = Color(0xFFB0BEC5),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 8.dp)
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(CharacterRegistry.allCharacters) { def ->
                val prog = progression.characters[def.id] ?: CharacterProgress(def.id)
                val isSelected = def.id == viewingCharId
                val isEquipped = def.id == progression.selectedCharacterId

                Card(
                    modifier = Modifier
                        .width(if (isSelected) 100.dp else 84.dp)
                        .height(if (isSelected) 115.dp else 100.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            width = if (isSelected) 2.5.dp else 1.dp,
                            color = if (isSelected) Color(def.energyColor) else Color(0xFF26223D),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            viewingCharId = def.id
                            audioManager.playSound(GameAudioManager.SoundType.CLICK)
                        }
                        .testTag("hero_card_${def.id.name}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF211D3B) else Color(0xFF131122)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Character Silhouette Avatar
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 46.dp else 38.dp)
                                .clip(CircleShape)
                                .background(Color(def.silhouetteColor))
                                .border(1.5.dp, Color(def.energyColor), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!prog.isUnlocked) {
                                Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                            } else {
                                // Eye glow dot
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(def.energyColor))
                                )
                            }
                        }

                        Text(
                            text = def.name,
                            color = if (isSelected) Color.White else Color(0xFF90A4AE),
                            fontSize = if (isSelected) 12.sp else 10.sp,
                            fontWeight = FontWeight.Black
                        )

                        if (isEquipped) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF00E676).copy(alpha = 0.25f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("ACTIVE", color = Color(0xFF69F0AE), fontSize = 8.sp, fontWeight = FontWeight.Black)
                            }
                        } else if (!prog.isUnlocked) {
                            Text("${def.unlockGemsCost} 💎", color = Color(0xFF00E5FF), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Text("Lv.${prog.level}", color = Color(0xFFFFD54F), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SELECTED CHARACTER HERO SHOWCASE
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Interactive 2D Shadow Warrior Canvas Preview
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(characterDef.energyColor).copy(alpha = 0.25f),
                                Color(0xFF131024),
                                Color(0xFF090714)
                            )
                        )
                    )
                    .border(2.dp, Color(characterDef.energyColor).copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(130.dp)) {
                    val c = Offset(size.width / 2f, size.height / 2f + 10f)
                    val s = 1.6f
                    val baseCol = Color(characterDef.silhouetteColor)
                    val energyCol = Color(characterDef.energyColor)

                    // Head
                    drawCircle(color = baseCol, radius = 10f * s, center = Offset(c.x, c.y - 38f * s))
                    drawCircle(color = energyCol, radius = 2.5f * s, center = Offset(c.x + 3f * s, c.y - 38f * s))

                    // Torso
                    drawLine(baseCol, Offset(c.x, c.y - 28f * s), Offset(c.x, c.y - 8f * s), strokeWidth = 5f * s, cap = StrokeCap.Round)
                    drawCircle(color = energyCol, radius = 3f * s, center = Offset(c.x, c.y - 20f * s))

                    // Legs
                    drawLine(baseCol, Offset(c.x, c.y - 8f * s), Offset(c.x - 10f * s, c.y + 16f * s), strokeWidth = 5f * s, cap = StrokeCap.Round)
                    drawLine(baseCol, Offset(c.x, c.y - 8f * s), Offset(c.x + 10f * s, c.y + 16f * s), strokeWidth = 5f * s, cap = StrokeCap.Round)

                    // Arms holding weapon
                    drawLine(baseCol, Offset(c.x, c.y - 26f * s), Offset(c.x + 14f * s, c.y - 12f * s), strokeWidth = 4.5f * s, cap = StrokeCap.Round)
                    // Glowing blade
                    drawLine(energyCol, Offset(c.x + 14f * s, c.y - 12f * s), Offset(c.x + 36f * s, c.y - 28f * s), strokeWidth = 4.5f * s, cap = StrokeCap.Round)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = characterDef.name,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "${characterDef.role}  •  Weapon: ${weaponDef.name}",
                color = Color(characterDef.energyColor),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // STATS CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141126))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("WARRIOR STATS", color = Color(0xFFFFD54F), fontSize = 12.sp, fontWeight = FontWeight.Black)
                        Text("Level ${characterProgress.level} / 20", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    StatRow(label = "Health (HP)", value = "${characterProgress.getHp(characterDef).toInt()}", progress = (characterProgress.getHp(characterDef) / 1200f).coerceIn(0f, 1f), color = Color(0xFFFF5252))
                    StatRow(label = "Attack Power", value = "${characterProgress.getAttack(characterDef).toInt()}", progress = (characterProgress.getAttack(characterDef) / 160f).coerceIn(0f, 1f), color = Color(0xFFFF9800))
                    StatRow(label = "Defense", value = "${characterProgress.getDefense(characterDef).toInt()}", progress = (characterProgress.getDefense(characterDef) / 45f).coerceIn(0f, 1f), color = Color(0xFF2979FF))
                    StatRow(label = "Move Speed", value = "${characterProgress.getSpeed(characterDef).toInt()}", progress = (characterProgress.getSpeed(characterDef) / 340f).coerceIn(0f, 1f), color = Color(0xFF00E5FF))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ABILITY CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141126))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ABILITY: ${characterDef.abilityName.uppercase()}", color = Color(characterDef.energyColor), fontSize = 13.sp, fontWeight = FontWeight.Black)
                        Text("${characterDef.abilityCooldownSec.toInt()}s CD", color = Color(0xFFB0BEC5), fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(characterDef.abilityDesc, color = Color(0xFFCFD8DC), fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ACTION BUTTONS (SELECT, UPGRADE, SKINS)
            if (characterProgress.isUnlocked) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isEquipped = viewingCharId == progression.selectedCharacterId
                    Button(
                        onClick = {
                            repository.selectCharacter(viewingCharId)
                            audioManager.playSound(GameAudioManager.SoundType.CLICK)
                        },
                        enabled = !isEquipped,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("select_hero_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isEquipped) Color(0xFF2C2844) else Color(0xFF00E676)
                        )
                    ) {
                        Text(if (isEquipped) "EQUIPPED" else "SELECT", fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }

                    val cost = characterProgress.getUpgradeCost()
                    val canUpgrade = characterProgress.level < 20 && progression.coins >= cost
                    Button(
                        onClick = {
                            if (repository.upgradeCharacter(viewingCharId)) {
                                audioManager.playSound(GameAudioManager.SoundType.LEVEL_UP)
                                audioManager.vibrate(50, 200)
                            }
                        },
                        enabled = canUpgrade,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                            .testTag("upgrade_hero_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9100))
                    ) {
                        Icon(Icons.Default.Upgrade, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (characterProgress.level >= 20) "MAX LVL" else "UPGRADE ($cost 🪙)", fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { showSkinsDialog = !showSkinsDialog },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("skins_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.Palette, contentDescription = "Skins", modifier = Modifier.size(20.dp))
                    }
                }
            } else {
                // UNLOCK BUTTON
                Button(
                    onClick = {
                        if (repository.unlockCharacter(viewingCharId)) {
                            audioManager.playSound(GameAudioManager.SoundType.LEVEL_UP)
                            audioManager.vibrate(60, 220)
                        } else {
                            onOpenShop()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("unlock_hero_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                ) {
                    Icon(Icons.Default.Diamond, contentDescription = null, tint = Color(0xFF0B1B26), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "UNLOCK FOR ${characterDef.unlockGemsCost} GEMS",
                        color = Color(0xFF0B1B26),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }

            // SKINS DRAWER SECTION
            AnimatedVisibility(visible = showSkinsDialog && characterProgress.isUnlocked) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF19152E))
                        .padding(14.dp)
                ) {
                    Text("CHARACTER SKINS", color = Color(0xFFFFD54F), fontWeight = FontWeight.Black, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        characterDef.skins.forEachIndexed { idx, skin ->
                            val isSkinUnlocked = characterProgress.unlockedSkins.contains(idx)
                            val isSkinSelected = characterProgress.selectedSkinIndex == idx

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSkinSelected) Color(0xFF2C2448) else Color(0xFF110E20))
                                    .border(
                                        width = if (isSkinSelected) 2.dp else 1.dp,
                                        color = if (isSkinSelected) Color(skin.glowColor) else Color(0xFF2E2A48),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        if (isSkinUnlocked) {
                                            repository.selectSkin(viewingCharId, idx)
                                        } else {
                                            repository.unlockSkin(viewingCharId, idx)
                                        }
                                        audioManager.playSound(GameAudioManager.SoundType.CLICK)
                                    }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(skin.primaryColor))
                                            .border(2.dp, Color(skin.glowColor), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(skin.name, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    if (isSkinSelected) {
                                        Text("EQUIPPED", color = Color(0xFF69F0AE), fontSize = 8.sp, fontWeight = FontWeight.Black)
                                    } else if (!isSkinUnlocked) {
                                        Text("${skin.unlockGemsCost} 💎", color = Color(0xFF00E5FF), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, progress: Float, color: Color) {
    Column(modifier = Modifier.padding(vertical = 3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color(0xFFB0BEC5), fontSize = 11.sp)
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Color(0xFF1D1A30)
        )
    }
}
