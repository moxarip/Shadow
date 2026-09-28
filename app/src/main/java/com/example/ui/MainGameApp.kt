package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.GameAudioManager
import com.example.data.GameRepository
import com.example.game.model.AppScreen
import com.example.ui.screens.AchievementsScreen
import com.example.ui.screens.BattleScreen
import com.example.ui.screens.DailyRewardsScreen
import com.example.ui.screens.HeroSelectScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.MissionsScreen
import com.example.ui.screens.SettingsDialog
import com.example.ui.screens.ShopScreen
import com.example.ui.screens.WeaponsScreen

data class NavItem(
    val screen: AppScreen,
    val label: String,
    val icon: ImageVector
)

@Composable
fun MainGameApp() {
    val context = LocalContext.current
    val repository = remember { GameRepository(context) }
    val audioManager = remember {
        GameAudioManager(context).apply {
            soundEnabled = repository.progression.value.soundEnabled
            vibrationEnabled = repository.progression.value.vibrationEnabled
        }
    }

    var currentScreen by remember { mutableStateOf(AppScreen.MAIN_MENU) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val navItems = listOf(
        NavItem(AppScreen.MAIN_MENU, "Play", Icons.Default.SportsKabaddi),
        NavItem(AppScreen.CHARACTERS, "Warriors", Icons.Default.People),
        NavItem(AppScreen.WEAPONS, "Armory", Icons.Default.Shield),
        NavItem(AppScreen.SHOP, "Shop", Icons.Default.Diamond),
        NavItem(AppScreen.MISSIONS, "Missions", Icons.Default.TrackChanges),
        NavItem(AppScreen.REWARDS, "Rewards", Icons.Default.CardGiftcard)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF090812),
        bottomBar = {
            if (currentScreen != AppScreen.BATTLE) {
                NavigationBar(
                    containerColor = Color(0xFF131124),
                    contentColor = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .navigationBarsPadding()
                        .height(60.dp)
                        .testTag("bottom_nav_bar")
                ) {
                    navItems.forEach { item ->
                        val selected = currentScreen == item.screen
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentScreen != item.screen) {
                                    currentScreen = item.screen
                                    audioManager.playSound(GameAudioManager.SoundType.CLICK)
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (selected) Color(0xFFFFD54F) else Color(0xFF8A85A6)
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    fontSize = 10.sp,
                                    fontWeight = if (selected) FontWeight.Black else FontWeight.Normal,
                                    color = if (selected) Color(0xFFFFD54F) else Color(0xFF8A85A6)
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color(0xFF281E48)
                            ),
                            modifier = Modifier.testTag("nav_tab_${item.screen.name}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.MAIN_MENU -> {
                    MainMenuScreen(
                        repository = repository,
                        audioManager = audioManager,
                        onStartBattle = { currentScreen = AppScreen.BATTLE },
                        onOpenHeroes = { currentScreen = AppScreen.CHARACTERS },
                        onOpenWeapons = { currentScreen = AppScreen.WEAPONS },
                        onOpenShop = { currentScreen = AppScreen.SHOP },
                        onOpenSettings = { showSettingsDialog = true }
                    )
                }
                AppScreen.BATTLE -> {
                    BattleScreen(
                        repository = repository,
                        audioManager = audioManager,
                        onExitBattle = { currentScreen = AppScreen.MAIN_MENU }
                    )
                }
                AppScreen.CHARACTERS, AppScreen.SKINS -> {
                    HeroSelectScreen(
                        repository = repository,
                        audioManager = audioManager,
                        onBack = { currentScreen = AppScreen.MAIN_MENU },
                        onOpenShop = { currentScreen = AppScreen.SHOP }
                    )
                }
                AppScreen.WEAPONS -> {
                    WeaponsScreen(
                        repository = repository,
                        audioManager = audioManager,
                        onBack = { currentScreen = AppScreen.MAIN_MENU }
                    )
                }
                AppScreen.SHOP -> {
                    ShopScreen(
                        repository = repository,
                        audioManager = audioManager,
                        onBack = { currentScreen = AppScreen.MAIN_MENU }
                    )
                }
                AppScreen.MISSIONS -> {
                    MissionsScreen(
                        repository = repository,
                        audioManager = audioManager,
                        onBack = { currentScreen = AppScreen.MAIN_MENU },
                        onOpenShop = { currentScreen = AppScreen.SHOP }
                    )
                }
                AppScreen.REWARDS -> {
                    DailyRewardsScreen(
                        repository = repository,
                        audioManager = audioManager,
                        onBack = { currentScreen = AppScreen.MAIN_MENU },
                        onOpenShop = { currentScreen = AppScreen.SHOP }
                    )
                }
                AppScreen.ACHIEVEMENTS -> {
                    AchievementsScreen(
                        repository = repository,
                        audioManager = audioManager,
                        onBack = { currentScreen = AppScreen.MAIN_MENU },
                        onOpenShop = { currentScreen = AppScreen.SHOP }
                    )
                }
            }

            if (showSettingsDialog) {
                SettingsDialog(
                    repository = repository,
                    audioManager = audioManager,
                    onDismiss = { showSettingsDialog = false }
                )
            }
        }
    }
}
