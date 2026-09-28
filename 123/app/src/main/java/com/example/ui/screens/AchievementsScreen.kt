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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.game.model.AchievementItem
import com.example.ui.components.TopCurrencyBar

@Composable
fun AchievementsScreen(
    repository: GameRepository,
    audioManager: GameAudioManager,
    onBack: () -> Unit,
    onOpenShop: () -> Unit
) {
    val progression = repository.progression.value
    val achievements by repository.achievements.collectAsState()

    BackHandler {
        onBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0914))
            .testTag("achievements_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 28.dp, bottom = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    text = "ACHIEVEMENTS",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                TopCurrencyBar(
                    progression = progression,
                    onShopClick = onOpenShop,
                    onSettingsClick = {},
                    modifier = Modifier.weight(6f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier.fillMaxWidth(0.92f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                achievements.forEach { ach ->
                    AchievementCard(
                        item = ach,
                        onClaim = {
                            val success = repository.claimAchievement(ach.id)
                            if (success) {
                                audioManager.playSound(GameAudioManager.SoundType.LEVEL_UP)
                                audioManager.vibrate(50, 220)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AchievementCard(
    item: AchievementItem,
    onClaim: () -> Unit
) {
    val isReady = item.current >= item.target && !item.isClaimed

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isReady) 1.5.dp else 1.dp,
                color = if (isReady) Color(0xFFFFD54F) else Color(0xFF282442),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141224))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = if (isReady || item.isClaimed) Color(0xFFFFD54F) else Color(0xFF90A4AE),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = item.title,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = item.description,
                            color = Color(0xFF90A4AE),
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Diamond, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+${item.gemReward}", color = Color(0xFFE0F7FA), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${item.current} / ${item.target}",
                        color = Color(0xFFD1C4E9),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (item.current.toFloat() / item.target).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (isReady) Color(0xFFFFD54F) else Color(0xFF7C4DFF),
                        trackColor = Color(0xFF26223D)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                if (item.isClaimed) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1F2B1F))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("CLAIMED", color = Color(0xFF81C784), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onClaim,
                        enabled = isReady,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD54F),
                            disabledContainerColor = Color(0xFF221F33)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("claim_ach_${item.id}")
                    ) {
                        Text(
                            text = "CLAIM",
                            color = if (isReady) Color(0xFF1E1400) else Color(0x66FFFFFF),
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
