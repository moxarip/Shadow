package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.audio.GameAudioManager
import com.example.data.GameRepository
import com.example.game.model.GraphicsQuality

@Composable
fun SettingsDialog(
    repository: GameRepository,
    audioManager: GameAudioManager,
    onDismiss: () -> Unit
) {
    val progression = repository.progression.value
    var soundOn by remember { mutableStateOf(progression.soundEnabled) }
    var musicOn by remember { mutableStateOf(progression.musicEnabled) }
    var vibeOn by remember { mutableStateOf(progression.vibrationEnabled) }
    var gfxQuality by remember { mutableStateOf(progression.graphicsQuality) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .testTag("settings_dialog"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141224))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GAME SETTINGS",
                        color = Color(0xFFFFD54F),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFFB0BEC5))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sound Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1B1730))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Sound Effects", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Switch(
                        checked = soundOn,
                        onCheckedChange = { checked ->
                            soundOn = checked
                            audioManager.soundEnabled = checked
                            repository.setAudioAndHaptics(checked, musicOn, vibeOn)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00E5FF), checkedTrackColor = Color(0xFF007A8A))
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Music Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1B1730))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Battle Music", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Switch(
                        checked = musicOn,
                        onCheckedChange = { checked ->
                            musicOn = checked
                            repository.setAudioAndHaptics(soundOn, checked, vibeOn)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFFD54F), checkedTrackColor = Color(0xFF8A6D00))
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Vibration Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1B1730))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Vibration, contentDescription = null, tint = Color(0xFFFF80AB), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Haptic Feedback", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Switch(
                        checked = vibeOn,
                        onCheckedChange = { checked ->
                            vibeOn = checked
                            audioManager.vibrationEnabled = checked
                            repository.setAudioAndHaptics(soundOn, musicOn, checked)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFF80AB), checkedTrackColor = Color(0xFF881B4B))
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // GRAPHICS QUALITY SELECTOR
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1B1730))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = Color(0xFF76FF03), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Graphics Quality", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        GraphicsQuality.values().forEach { gq ->
                            val isSelected = gfxQuality == gq
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF2E2652) else Color(0xFF100D1E))
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.5.dp,
                                        color = if (isSelected) Color(0xFF76FF03) else Color(0xFF282342),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        gfxQuality = gq
                                        repository.setGraphicsQuality(gq)
                                        audioManager.playSound(GameAudioManager.SoundType.CLICK)
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = gq.name,
                                    color = if (isSelected) Color(0xFF76FF03) else Color(0xFF90A4AE),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "LOW mode optimizes particles & effects for smooth 60 FPS on all devices.",
                        color = Color(0xFF78909C),
                        fontSize = 9.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Shadow Warriors v1.0 • 2D Mobile Action",
                    color = Color(0xFF607D8B),
                    fontSize = 10.sp
                )
            }
        }
    }
}
