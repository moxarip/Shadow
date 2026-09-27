package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
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
import com.example.game.model.ArenaRegistry
import com.example.game.model.GameMode
import com.example.game.model.HeroRegistry
import com.example.game.model.Vector2
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
    val heroDef = HeroRegistry.getDef(progression.selectedHeroId)
    val heroProgress = progression.heroes[progression.selectedHeroId] ?: com.example.game.model.HeroProgress(progression.selectedHeroId)
    val arenaDef = ArenaRegistry.getDef(progression.selectedArenaId)

    val engine = remember {
        GameEngine(
            heroDef = heroDef,
            heroProgress = heroProgress,
            arenaDef = arenaDef,
            gameMode = progression.selectedGameMode,
            audioManager = audioManager
        )
    }

    var joystickInput by remember { mutableStateOf(Vector2(0f, 0f)) }
    var isAttackHeld by remember { mutableStateOf(false) }
    var showPauseDialog by remember { mutableStateOf(false) }
    var battleRewardsReported by remember { mutableStateOf(false) }

    // Recomposition tick trigger
    var frameTick by remember { mutableFloatStateOf(0f) }

    BackHandler {
        showPauseDialog = true
    }

    // High performance 60FPS Game Loop
    LaunchedEffect(engine) {
        var lastTime = System.nanoTime()
        while (true) {
            withFrameNanos { now ->
                val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.04f)
                lastTime = now

                if (!engine.isPaused) {
                    engine.update(dt, joystickInput, isAttackHeld)
                }
                frameTick = dt

                // Check victory or defeat once to report rewards
                if ((engine.isVictory || engine.isGameOver) && !battleRewardsReported) {
                    battleRewardsReported = true
                    repository.addRewards(
                        earnedCoins = engine.runCoinsEarned,
                        earnedXp = engine.runXpEarned,
                        enemiesDefeated = engine.runEnemiesDefeated,
                        isVictory = engine.isVictory,
                        isBossDefeated = engine.isVictory && engine.gameMode == GameMode.STORY,
                        abilitiesUsed = engine.runAbilitiesUsed,
                        endlessWave = engine.currentWave
                    )
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090810))
            .testTag("battle_screen")
    ) {
        // Real-time Gameplay Canvas
        ArenaCanvas(
            engine = engine,
            modifier = Modifier.fillMaxSize()
        )

        // Top Status HUD
        CombatHUD(
            engine = engine,
            onPauseClick = {
                engine.isPaused = true
                showPauseDialog = true
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 28.dp)
        )

        // Bottom Controls Layer
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

            // Combat Buttons on the bottom right
            CombatControls(
                modifier = Modifier.align(Alignment.BottomEnd),
                heroDef = heroDef,
                dashCooldownRemaining = engine.player.dashCooldownTimer,
                dashCooldownMax = engine.player.dashCooldownMax,
                abilityCooldownRemaining = engine.player.abilityCooldownTimer,
                abilityCooldownMax = engine.player.abilityCooldownMax,
                onAttackPress = { pressed -> isAttackHeld = pressed },
                onDashClick = { engine.requestDash() },
                onAbilityClick = { engine.requestSpecialAbility() }
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
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B182B))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "BATTLE PAUSED",
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
                            Text("Resume Battle", fontWeight = FontWeight.Bold)
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
                            Text("Abandon Battle", fontWeight = FontWeight.Bold)
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
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF16152B))
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
                            text = "VICTORY ACHIEVED!",
                            color = Color(0xFFFFD54F),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "${arenaDef.name} Conquered",
                            color = Color(0xFFB0BEC5),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats Summary Box
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF0F0E1E))
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Gold Collected", color = Color(0xFFB0BEC5), fontSize = 13.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+${engine.runCoinsEarned}", color = Color(0xFFFFE082), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("XP Earned", color = Color(0xFFB0BEC5), fontSize = 13.sp)
                                Text("+${engine.runXpEarned} XP", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Enemies Slayed", color = Color(0xFFB0BEC5), fontSize = 13.sp)
                                Text("${engine.runEnemiesDefeated}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Time Elapsed", color = Color(0xFFB0BEC5), fontSize = 13.sp)
                                Text("${engine.runTimeSeconds.toInt()}s", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onExitBattle,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("victory_continue_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                        ) {
                            Text("CLAIM & CONTINUE", color = Color(0xFF0A2B14), fontWeight = FontWeight.Black, fontSize = 15.sp)
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
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF20111A))
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
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Wave ${engine.currentWave} - Keep fighting to grow stronger!",
                            color = Color(0xFFB0BEC5),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF130910))
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Gold Salvaged", color = Color(0xFFB0BEC5), fontSize = 13.sp)
                                Text("+${engine.runCoinsEarned}", color = Color(0xFFFFE082), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("XP Retained", color = Color(0xFFB0BEC5), fontSize = 13.sp)
                                Text("+${engine.runXpEarned} XP", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onExitBattle,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("defeat_return_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
                        ) {
                            Text("RETURN TO MENU", color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}
