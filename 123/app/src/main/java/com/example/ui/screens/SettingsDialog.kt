package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.audio.GameAudioManager
import com.example.data.GameRepository

@Composable
fun SettingsDialog(
    repository: GameRepository,
    audioManager: GameAudioManager,
    onDismiss: () -> Unit
) {
    val progression = repository.progression.value
    var soundOn by remember { mutableStateOf(progression.soundEnabled) }
    var vibeOn by remember { mutableStateOf(progression.vibrationEnabled) }
    var showResetConfirm by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .testTag("settings_dialog"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF17152B))
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SETTINGS",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFFB0BEC5))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sound Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Sound Effects", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Switch(
                        checked = soundOn,
                        onCheckedChange = { checked ->
                            soundOn = checked
                            repository.setSoundEnabled(checked)
                            audioManager.soundEnabled = checked
                            if (checked) audioManager.playSound(GameAudioManager.SoundType.CLICK)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00E5FF), checkedTrackColor = Color(0xFF005B7F))
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Vibration Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Vibration, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Vibration / Haptics", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Switch(
                        checked = vibeOn,
                        onCheckedChange = { checked ->
                            vibeOn = checked
                            repository.setVibrationEnabled(checked)
                            audioManager.vibrationEnabled = checked
                            if (checked) audioManager.vibrate(40, 200)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFFD54F), checkedTrackColor = Color(0xFF664D00))
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Reset Progress
                if (!showResetConfirm) {
                    OutlinedButton(
                        onClick = { showResetConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252))
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset All Progress", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Are you sure? This cannot be undone.", color = Color(0xFFFF5252), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    repository.resetAllData()
                                    showResetConfirm = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744))
                            ) {
                                Text("CONFIRM RESET")
                            }
                            OutlinedButton(onClick = { showResetConfirm = false }) {
                                Text("Cancel")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "SHADOW CLASH v1.0.0 • 2D Action Arena",
                    color = Color(0xFF78909C),
                    fontSize = 11.sp
                )
            }
        }
    }
}
