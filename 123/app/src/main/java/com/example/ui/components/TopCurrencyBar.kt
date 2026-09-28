package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerProgression

@Composable
fun TopCurrencyBar(
    progression: PlayerProgression,
    onShopClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Player Level & XP
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1B192E))
                .border(1.dp, Color(0xFF383259), RoundedCornerShape(20.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF7C4DFF), Color(0xFF651FFF)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${progression.level}",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = "LVL ${progression.level}",
                    color = Color(0xFFD1C4E9),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                LinearProgressIndicator(
                    progress = { (progression.xp.toFloat() / progression.xpToNextLevel).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .width(45.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color(0xFF00E5FF),
                    trackColor = Color(0xFF2A2640)
                )
            }
        }

        // Coins Chip
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF1E1A14))
                .border(1.dp, Color(0xFFFFB300), RoundedCornerShape(18.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MonetizationOn,
                contentDescription = "Coins",
                tint = Color(0xFFFFD54F),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${progression.coins}",
                color = Color(0xFFFFE082),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        // Gems Chip (Tappable to go to shop)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF131F2E))
                .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(18.dp))
                .clickable { onShopClick() }
                .padding(start = 8.dp, top = 4.dp, bottom = 4.dp, end = 4.dp)
                .testTag("gems_chip")
        ) {
            Icon(
                imageVector = Icons.Default.Diamond,
                contentDescription = "Gems",
                tint = Color(0xFF00E5FF),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${progression.gems}",
                color = Color(0xFFE0F7FA),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00E5FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Buy Gems",
                    tint = Color(0xFF0A192F),
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Settings Button
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .size(34.dp)
                .testTag("settings_button")
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color(0xFFB0BEC5),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
