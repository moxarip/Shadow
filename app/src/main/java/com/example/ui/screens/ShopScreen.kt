package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.game.model.CoinPackage
import com.example.game.model.ShopCatalog
import com.example.game.model.ShopPackage
import com.example.ui.components.TopCurrencyBar

@Composable
fun ShopScreen(
    repository: GameRepository,
    audioManager: GameAudioManager,
    onBack: () -> Unit
) {
    val progression = repository.progression.value
    var selectedTab by remember { mutableIntStateOf(0) } // 0: GEMS, 1: GOLD, 2: OFFERS
    var pendingPackage by remember { mutableStateOf<ShopPackage?>(null) }
    var purchaseSuccessPackage by remember { mutableStateOf<ShopPackage?>(null) }

    BackHandler {
        onBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0914))
            .testTag("shop_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 28.dp, bottom = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
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
                    text = "MYSTIC VAULT",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                TopCurrencyBar(
                    progression = progression,
                    onShopClick = {},
                    onSettingsClick = {},
                    modifier = Modifier.weight(6f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Shop Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF141226),
                contentColor = Color(0xFFFFD54F),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("GEMS", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("GOLD", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("OFFERS", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TAB 0: GEMS PACKAGES
            if (selectedTab == 0) {
                Column(
                    modifier = Modifier.fillMaxWidth(0.92f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ShopCatalog.gemPackages.forEach { pack ->
                        GemPackageCard(
                            pack = pack,
                            onBuyClick = {
                                pendingPackage = pack
                                audioManager.playSound(GameAudioManager.SoundType.CLICK)
                            }
                        )
                    }
                }
            }

            // TAB 1: GOLD EXCHANGE
            if (selectedTab == 1) {
                Column(
                    modifier = Modifier.fillMaxWidth(0.92f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ShopCatalog.coinPackages.forEach { coinPack ->
                        CoinExchangeCard(
                            pack = coinPack,
                            canAfford = progression.gems >= coinPack.costGems,
                            onExchangeClick = {
                                val success = repository.exchangeGemsForCoins(coinPack.costGems, coinPack.coins)
                                if (success) {
                                    audioManager.playSound(GameAudioManager.SoundType.COIN)
                                }
                            }
                        )
                    }
                }
            }

            // TAB 2: SPECIAL OFFERS
            if (selectedTab == 2) {
                Column(
                    modifier = Modifier.fillMaxWidth(0.92f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SpecialOfferCard(
                        title = "Hero Starter Bundle",
                        subtitle = "1,500 Gems + 10,000 Gold + XP Boost",
                        priceStr = "$4.99",
                        gemsAwarded = 1500,
                        coinsAwarded = 10000,
                        tag = "80% OFF",
                        onBuy = {
                            pendingPackage = ShopPackage("bundle_starter", "Hero Starter Bundle", "$4.99", 1500)
                        }
                    )

                    SpecialOfferCard(
                        title = "Dragon Conqueror Cache",
                        subtitle = "5,000 Gems (Instant Dragon Unlock!) + 25,000 Gold",
                        priceStr = "$19.99",
                        gemsAwarded = 5000,
                        coinsAwarded = 25000,
                        tag = "BEST VALUE",
                        onBuy = {
                            pendingPackage = ShopPackage("bundle_dragon", "Dragon Conqueror Cache", "$19.99", 5000)
                        }
                    )
                }
            }
        }

        // Mock Purchase Confirmation Dialog
        pendingPackage?.let { pack ->
            Dialog(onDismissRequest = { pendingPackage = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.94f)
                        .testTag("purchase_confirmation_dialog"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B192E))
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E5FF).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Diamond, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "MOCK IN-APP PURCHASE",
                            color = Color(0xFFFFD54F),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = pack.title,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "+${pack.gems} Gems for ${pack.priceStr}",
                            color = Color(0xFFB0BEC5),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Simulation Mode: No real payment will be charged. Gems will be immediately credited to your account.",
                            color = Color(0xFF78909C),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                val pkg = pack
                                pendingPackage = null
                                repository.addPurchasedGems(pkg.gems)
                                audioManager.playSound(GameAudioManager.SoundType.LEVEL_UP)
                                audioManager.vibrate(60, 240)
                                purchaseSuccessPackage = pkg
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("confirm_purchase_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                            shape = RoundedCornerShape(25.dp)
                        ) {
                            Text("SIMULATE PURCHASE", color = Color(0xFF0A2B14), fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { pendingPackage = null },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(22.dp)
                        ) {
                            Text("Cancel", color = Color(0xFFB0BEC5))
                        }
                    }
                }
            }
        }

        // Purchase Success Popup
        purchaseSuccessPackage?.let { pack ->
            Dialog(onDismissRequest = { purchaseSuccessPackage = null }) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141324))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .testTag("purchase_success_dialog"),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("PURCHASE COMPLETE!", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("+${pack.gems} Gems added to your balance!", color = Color(0xFF00E5FF), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = { purchaseSuccessPackage = null },
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
                        ) {
                            Text("AWESOME", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GemPackageCard(
    pack: ShopPackage,
    onBuyClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF151326))
            .border(
                width = if (pack.isBestValue || pack.isPopular) 1.5.dp else 1.dp,
                color = if (pack.isBestValue) Color(0xFFFFD54F) else if (pack.isPopular) Color(0xFF00E5FF) else Color(0xFF282440),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onBuyClick() }
            .padding(14.dp)
            .testTag("buy_pack_${pack.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00E5FF).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Diamond,
                        contentDescription = "Gems",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "${pack.gems} GEMS",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = pack.title,
                        color = Color(0xFF90A4AE),
                        fontSize = 11.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (pack.bonusLabel != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFFB300).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFFFFB300), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = pack.bonusLabel,
                            color = Color(0xFFFFD54F),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF7C4DFF))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = pack.priceStr,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CoinExchangeCard(
    pack: CoinPackage,
    canAfford: Boolean,
    onExchangeClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF161424))
            .border(1.dp, Color(0xFF332E52), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "+${pack.coins} Coins",
                        color = Color(0xFFFFE082),
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                    Text(text = pack.title, color = Color(0xFF90A4AE), fontSize = 11.sp)
                }
            }

            Button(
                onClick = onExchangeClick,
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E5FF),
                    disabledContainerColor = Color(0xFF1A2633)
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Diamond, contentDescription = null, tint = if (canAfford) Color(0xFF091F2C) else Color(0x66FFFFFF), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${pack.costGems}",
                        color = if (canAfford) Color(0xFF091F2C) else Color(0x66FFFFFF),
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun SpecialOfferCard(
    title: String,
    subtitle: String,
    priceStr: String,
    gemsAwarded: Int,
    coinsAwarded: Int,
    tag: String,
    onBuy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.5.dp, Color(0xFFFFD54F), RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1738))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFF3D00))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(tag, color = Color.White, fontWeight = FontWeight.Black, fontSize = 10.sp)
                }
                Text(priceStr, color = Color(0xFFFFD54F), fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
            Text(subtitle, color = Color(0xFFB0BEC5), fontSize = 12.sp)

            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onBuy,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                shape = RoundedCornerShape(23.dp)
            ) {
                Text("GET OFFER", color = Color(0xFF1E1400), fontWeight = FontWeight.Black)
            }
        }
    }
}
