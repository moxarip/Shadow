package com.example.game.model

data class MissionItem(
    val id: String,
    val title: String,
    val description: String,
    val target: Int,
    val current: Int,
    val coinReward: Int,
    val gemReward: Int,
    val isClaimed: Boolean
)

data class AchievementItem(
    val id: String,
    val title: String,
    val description: String,
    val target: Int,
    val current: Int,
    val gemReward: Int,
    val isClaimed: Boolean
)

data class DailyRewardItem(
    val dayNumber: Int,
    val coinReward: Int,
    val gemReward: Int,
    val isMajor: Boolean = false
)

data class ShopPackage(
    val id: String,
    val title: String,
    val priceStr: String,
    val gems: Int,
    val bonusLabel: String? = null,
    val isPopular: Boolean = false,
    val isBestValue: Boolean = false
)

data class CoinPackage(
    val id: String,
    val title: String,
    val costGems: Int,
    val coins: Int,
    val bonusLabel: String? = null
)

object ShopCatalog {
    val gemPackages: List<ShopPackage> = listOf(
        ShopPackage("gem_1", "Pouch of Gems", "$0.99", 100),
        ShopPackage("gem_2", "Sack of Gems", "$1.99", 220, "+10% Bonus"),
        ShopPackage("gem_5", "Chest of Gems", "$4.99", 600, "+20% Bonus", isPopular = true),
        ShopPackage("gem_10", "Vault of Gems", "$9.99", 1300, "+30% Bonus"),
        ShopPackage("gem_20", "Hoard of Gems", "$19.99", 2800, "+40% Bonus", isBestValue = true),
        ShopPackage("gem_30", "Mountain of Gems", "$29.99", 4500, "+50% Bonus"),
        ShopPackage("gem_50", "Divine Cache", "$49.99", 8000, "+60% Bonus")
    )

    val coinPackages: List<CoinPackage> = listOf(
        CoinPackage("coin_1", "Handful of Gold", 50, 1000),
        CoinPackage("coin_2", "Coffer of Gold", 200, 5000, "+25% Bonus"),
        CoinPackage("coin_3", "Treasury of Gold", 500, 15000, "+50% Bonus")
    )

    val dailyRewards: List<DailyRewardItem> = listOf(
        DailyRewardItem(1, 200, 0),
        DailyRewardItem(2, 400, 0),
        DailyRewardItem(3, 0, 50),
        DailyRewardItem(4, 800, 0),
        DailyRewardItem(5, 0, 100),
        DailyRewardItem(6, 1500, 0),
        DailyRewardItem(7, 5000, 500, isMajor = true)
    )
}
