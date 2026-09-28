package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.GameAudioManager
import com.example.data.GameRepository
import com.example.game.model.WeaponProgress
import com.example.game.model.WeaponRegistry

@Composable
fun WeaponsScreen(
    repository: GameRepository,
    audioManager: GameAudioManager,
    onBack: () -> Unit
) {
    val progression = repository.progression.value

    BackHandler {
        onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090714))
            .padding(16.dp)
            .testTag("weapons_screen")
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
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
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Text(
                text = "ARMORY & WEAPONS",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )

            // Coins
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1C192E))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("${progression.coins} 🪙", color = Color(0xFFFFE082), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(WeaponRegistry.allWeapons) { weapon ->
                val wProg = progression.weapons[weapon.type] ?: WeaponProgress(weapon.type)
                val isEquipped = progression.selectedWeaponType == weapon.type

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            width = if (isEquipped) 2.dp else 1.dp,
                            color = if (isEquipped) Color(weapon.color) else Color(0xFF231F38),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141126))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Weapon Icon & Info
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color(weapon.color).copy(alpha = 0.2f))
                                    .border(1.5.dp, Color(weapon.color), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!wProg.isUnlocked) {
                                    Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color(0xFFFF5252), modifier = Modifier.size(20.dp))
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color(weapon.color))
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(weapon.name, color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                Text(
                                    text = if (weapon.isRanged) "Ranged • ${weapon.specialEffect}" else "Melee • ${weapon.specialEffect}",
                                    color = Color(weapon.color),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Damage: ${wProg.getDamage(weapon).toInt()}  •  Speed: ${weapon.attackSpeed}x",
                                    color = Color(0xFFB0BEC5),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Equip / Upgrade Button
                        if (wProg.isUnlocked) {
                            Column(horizontalAlignment = Alignment.End) {
                                if (isEquipped) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF00E676).copy(alpha = 0.2f))
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text("EQUIPPED", color = Color(0xFF69F0AE), fontWeight = FontWeight.Black, fontSize = 11.sp)
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            repository.selectWeapon(weapon.type)
                                            audioManager.playSound(GameAudioManager.SoundType.CLICK)
                                        },
                                        modifier = Modifier.height(34.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(weapon.color))
                                    ) {
                                        Text("EQUIP", fontWeight = FontWeight.Black, fontSize = 11.sp)
                                    }
                                }

                                val upCost = wProg.getUpgradeCost()
                                if (wProg.level < 10 && progression.coins >= upCost) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Upgrade: $upCost 🪙",
                                        color = Color(0xFFFFD54F),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable {
                                            if (repository.upgradeWeapon(weapon.type)) {
                                                audioManager.playSound(GameAudioManager.SoundType.LEVEL_UP)
                                            }
                                        }
                                    )
                                }
                            }
                        } else {
                            Text("Locked", color = Color(0xFFFF5252), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
