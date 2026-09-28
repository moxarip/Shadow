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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.audio.GameAudioManager
import com.example.data.GameRepository
import com.example.game.model.DailyRewardItem
import com.example.game.model.ShopCatalog
import com.example.ui.components.TopCurrencyBar

@Composable
fun DailyRewardsScreen(
    repository: GameRepository,
    audioManager: GameAudioManager,
    onBack: () -> Unit,
    onOpenShop: () -> Unit
) {
    val progression = repository.progression.value
    var chestRewardPopup by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    val nextDayToClaim = (progression.dailyRewardDayClaimed % 7) + 1
    // For demo / testability: player can claim daily reward if at least 10 seconds have passed or first time
    val canClaimDaily = System.currentTimeMillis() - progression.lastDailyClaimTime > 10_000L
    val canClaimChest = System.currentTimeMillis() - progression.lastFreeChestTime > 10_000L

    BackHandler {
        onBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090812))
            .testTag("rewards_screen")
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
                    text = "DAILY REWARDS",
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

            // Free Mystery Chest Box
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.5.dp, Color(0xFF00E5FF), RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141A2D))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E5FF).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("FREE MYSTERY CHEST", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                            Text("Random Gold & Gems", color = Color(0xFF80D8FF), fontSize = 11.sp)
                        }
                    }

                    Button(
                        onClick = {
                            val reward = repository.claimFreeChest()
                            chestRewardPopup = reward
                            audioManager.playSound(GameAudioManager.SoundType.COIN)
                            audioManager.vibrate(40, 200)
                        },
                        enabled = canClaimChest,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E5FF),
                            disabledContainerColor = Color(0xFF1B2836)
                        ),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.testTag("claim_free_chest_button")
                    ) {
                        Text(
                            text = if (canClaimChest) "OPEN" else "READY SOON",
                            color = if (canClaimChest) Color(0xFF091C28) else Color(0x66FFFFFF),
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "7-DAY LOGIN CALENDAR",
                color = Color(0xFFFFD54F),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 7 Days Grid
            Column(
                modifier = Modifier.fillMaxWidth(0.92f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShopCatalog.dailyRewards.forEach { rewardItem ->
                    val isClaimed = rewardItem.dayNumber <= progression.dailyRewardDayClaimed
                    val isToday = rewardItem.dayNumber == nextDayToClaim
                    val isLocked = rewardItem.dayNumber > nextDayToClaim

                    DailyDayRow(
                        item = rewardItem,
                        isClaimed = isClaimed,
                        isToday = isToday,
                        isLocked = isLocked,
                        canClaim = isToday && canClaimDaily,
                        onClaim = {
                            val success = repository.claimDailyReward()
                            if (success) {
                                audioManager.playSound(GameAudioManager.SoundType.LEVEL_UP)
                                audioManager.vibrate(50, 220)
                            }
                        }
                    )
                }
            }
        }

        // Chest Opened Popup
        chestRewardPopup?.let { (coins, gems) ->
            Dialog(onDismissRequest = { chestRewardPopup = null }) {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF14162B))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .testTag("chest_reward_dialog"),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(54.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("CHEST UNLOCKED!", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+$coins Coins", color = Color(0xFFFFE082), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Diamond, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+$gems Gems", color = Color(0xFFE0F7FA), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { chestRewardPopup = null },
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
                        ) {
                            Text("COLLECT", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyDayRow(
    item: DailyRewardItem,
    isClaimed: Boolean,
    isToday: Boolean,
    isLocked: Boolean,
    canClaim: Boolean,
    onClaim: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = if (isToday) 2.dp else 1.dp,
                color = if (isToday) Color(0xFFFFD54F) else if (item.isMajor) Color(0xFFFF1744) else Color(0xFF26223D),
                shape = RoundedCornerShape(14.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isMajor) Color(0xFF28131E) else Color(0xFF141224)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isToday) Color(0xFFFFD54F) else Color(0xFF25213D)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "D${item.dayNumber}",
                        color = if (isToday) Color.Black else Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (item.isMajor) "DAY 7 - JACKPOT!" else "Day ${item.dayNumber} Reward",
                        color = if (item.isMajor) Color(0xFFFF5252) else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (item.coinReward > 0) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("${item.coinReward} Gold", color = Color(0xFFFFE082), fontSize = 11.sp)
                        }
                        if (item.coinReward > 0 && item.gemReward > 0) {
                            Text(" + ", color = Color(0xFF90A4AE), fontSize = 11.sp)
                        }
                        if (item.gemReward > 0) {
                            Icon(Icons.Default.Diamond, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("${item.gemReward} Gems", color = Color(0xFF80D8FF), fontSize = 11.sp)
                        }
                    }
                }
            }

            when {
                isClaimed -> {
                    Icon(Icons.Default.Check, contentDescription = "Claimed", tint = Color(0xFF00E676), modifier = Modifier.size(22.dp))
                }
                isToday -> {
                    Button(
                        onClick = onClaim,
                        enabled = canClaim,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E676),
                            disabledContainerColor = Color(0xFF1D2F22)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("claim_day_${item.dayNumber}")
                    ) {
                        Text(
                            text = if (canClaim) "CLAIM" else "READY",
                            color = if (canClaim) Color(0xFF0A2B14) else Color(0x66FFFFFF),
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
                else -> {
                    Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color(0x44FFFFFF), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
