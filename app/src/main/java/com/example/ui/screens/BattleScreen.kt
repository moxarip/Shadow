package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.audio.GameAudioManager
import com.example.data.GameRepository
import com.example.game.engine.GameEngine
import com.example.game.model.CharacterProgress
import com.example.game.model.CharacterRegistry
import com.example.game.model.Vector2
import com.example.game.model.WeaponRegistry
import com.example.game.model.WorldRegistry
import com.example.ui.components.ArenaCanvas
import com.example.ui.components.CombatControls
import com.example.ui.components.CombatHUD
import com.example.ui.components.VirtualJoystick

@Composable
fun BattleScreen(
    repository: GameRepository,
    audioManager: GameAudioManager,
    onExitBattle: () -> Unit
) {
    val progression = repository.progression.value
    val characterDef = CharacterRegistry.getDef(progression.selectedCharacterId)
    val characterProgress = progression.characters[progression.selectedCharacterId]
        ?: CharacterProgress(progression.selectedCharacterId, 1, true)
    val weaponDef = WeaponRegistry.getDef(progression.selectedWeaponType)
    val worldDef = WorldRegistry.getDef(progression.selectedWorldId)
    val stageNumber = progression.selectedStageNumber

    var restartKey by remember { mutableStateOf(0) }

    val engine = remember(restartKey, progression.selectedCharacterId, progression.selectedWeaponType, progression.selectedWorldId, stageNumber) {
        GameEngine(
            characterDef = characterDef,
            characterProgress = characterProgress,
            weaponDef = weaponDef,
            worldDef = worldDef,
            stageNumber = stageNumber,
            audioManager = audioManager
        )
    }

    var joystickInput by remember { mutableStateOf(Vector2(0f, 0f)) }
    var isAttackHeld by remember { mutableStateOf(false) }
    var showPauseDialog by remember { mutableStateOf(false) }
    var battleRewardsReported by remember { mutableStateOf(false) }

    var frameTick by remember { mutableFloatStateOf(0f) }

    BackHandler {
        showPauseDialog = true
    }

    // 60FPS Game Loop
    LaunchedEffect(engine) {
        var lastTime = System.nanoTime()
        while (true) {
            withFrameNanos { now ->
                val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.04f)
                lastTime = now

                if (!engine.isPaused) {
                    engine.update(dt, moveX = joystickInput.x, jumpPressed = false, attackHeld = isAttackHeld)
                }
                frameTick = dt

                // Award rewards on victory or loss once
                if ((engine.isVictory || engine.isGameOver) && !battleRewardsReported) {
                    battleRewardsReported = true
                    repository.addRewards(
                        earnedCoins = engine.coinsCollected,
                        earnedXp = engine.xpEarned,
                        enemiesDefeated = engine.enemiesKilled,
                        isStageVictory = engine.isVictory,
                        isBossDefeated = engine.bossDefeated
                    )
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF06050C))
            .testTag("battle_screen")
    ) {
        // 1. Real-time Side-scrolling Canvas
        ArenaCanvas(
            engine = engine,
            modifier = Modifier.fillMaxSize(),
            onTouchMove = { vec -> joystickInput = vec }
        )

        // 2. Top In-Game Status HUD
        CombatHUD(
            engine = engine,
            onPauseClick = {
                engine.isPaused = true
                showPauseDialog = true
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 26.dp)
        )

        // 3. Bottom Controls Layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            // Virtual Joystick on the bottom left
            VirtualJoystick(
                modifier = Modifier.align(Alignment.BottomStart),
                onMove = { vec -> joystickInput = vec }
            )

            // Combat Buttons on the bottom right (ATTACK, JUMP, DASH, ABILITY)
            CombatControls(
                modifier = Modifier.align(Alignment.BottomEnd),
                characterDef = characterDef,
                dashCooldownRemaining = engine.player.dashCooldownTimer,
                dashCooldownMax = engine.player.dashCooldownMax,
                abilityCooldownRemaining = engine.player.abilityCooldownTimer,
                abilityCooldownMax = characterDef.abilityCooldownSec,
                onAttackPress = { pressed -> isAttackHeld = pressed },
                onJumpClick = { engine.requestJump() },
                onDashClick = { engine.requestPlayerDash() },
                onAbilityClick = { engine.requestPlayerAbility() }
            )
        }

        // Pause Dialog
        if (showPauseDialog) {
            Dialog(onDismissRequest = {
                showPauseDialog = false
                engine.isPaused = false
            }) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF18152B))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "GAME PAUSED",
                            color = Color(0xFFFFD54F),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                showPauseDialog = false
                                engine.isPaused = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("resume_battle_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Resume", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                showPauseDialog = false
                                onExitBattle()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("exit_battle_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Abandon Stage", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Victory Dialog
        if (engine.isVictory) {
            Dialog(onDismissRequest = {}) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.92f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141228))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .testTag("victory_dialog"),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(Color(0xFFFFD700), Color(0xFFFF8F00)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = "Victory",
                                tint = Color(0xFF2E1700),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (engine.stageNumber == 4) "BOSS SLAIN!" else "STAGE CLEARED!",
                            color = Color(0xFFFFD54F),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "${worldDef.name} • Stage $stageNumber",
                            color = Color(0xFFB0BEC5),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats Summary Box
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF0C0A1A))
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Coins Harvested", color = Color(0xFFB0BEC5), fontSize = 13.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+${engine.coinsCollected}", color = Color(0xFFFFE082), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("XP Gained", color = Color(0xFFB0BEC5), fontSize = 13.sp)
                                Text("+${engine.xpEarned} XP", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Enemies Defeated", color = Color(0xFFB0BEC5), fontSize = 13.sp)
                                Text("${engine.enemiesKilled}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (stageNumber < 4) {
                                Button(
                                    onClick = {
                                        repository.selectWorldAndStage(worldDef.id, stageNumber + 1)
                                        battleRewardsReported = false
                                        restartKey++
                                    },
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .height(48.dp)
                                        .testTag("victory_next_stage_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                                ) {
                                    Text("NEXT STAGE", color = Color(0xFF0A2B14), fontWeight = FontWeight.Black, fontSize = 13.sp)
                                }
                            }
                            Button(
                                onClick = onExitBattle,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("victory_continue_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
                            ) {
                                Text("MENU", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // Defeat Dialog
        if (engine.isGameOver) {
            Dialog(onDismissRequest = {}) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.92f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF22111A))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .testTag("defeat_dialog"),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "FALLEN IN BATTLE",
                            color = Color(0xFFFF5252),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "${worldDef.name} • Stage $stageNumber",
                            color = Color(0xFFB0BEC5),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF14080F))
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Coins Salvaged", color = Color(0xFFB0BEC5), fontSize = 13.sp)
                                Text("+${engine.coinsCollected}", color = Color(0xFFFFE082), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("XP Retained", color = Color(0xFFB0BEC5), fontSize = 13.sp)
                                Text("+${engine.xpEarned} XP", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    battleRewardsReported = false
                                    restartKey++
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Retry", fontSize = 13.sp)
                            }
                            Button(
                                onClick = onExitBattle,
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(48.dp)
                                    .testTag("defeat_return_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
                            ) {
                                Text("MAIN MENU", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
