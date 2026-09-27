package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.example.game.engine.GameEngine
import com.example.game.model.ArenaId
import com.example.game.model.EnemyEntity
import com.example.game.model.EnemyType
import com.example.game.model.GroundZoneType
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArenaCanvas(
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val scaleX = size.width / engine.arenaWidth
        val scaleY = size.height / engine.arenaHeight
        val scale = minOf(scaleX, scaleY)

        val offsetX = (size.width - engine.arenaWidth * scale) / 2f + engine.screenShakeOffsetX
        val offsetY = (size.height - engine.arenaHeight * scale) / 2f + engine.screenShakeOffsetY

        fun toScreen(x: Float, y: Float): Offset {
            return Offset(offsetX + x * scale, offsetY + y * scale)
        }

        // Draw Arena Floor & Background
        drawArenaFloor(engine, scale, offsetX, offsetY)

        // Draw Arena Obstacles
        drawArenaObstacles(engine, scale, offsetX, offsetY)

        // Draw Ground Zones (Abilities, Poison, Warnings)
        drawGroundZones(engine, scale, offsetX, offsetY)

        // Draw Floating Coins & Gems
        drawCoins(engine, scale, offsetX, offsetY)

        // Draw Projectiles
        drawProjectiles(engine, scale, offsetX, offsetY)

        // Draw Enemies
        drawEnemies(engine, scale, offsetX, offsetY)

        // Draw Player Hero
        drawPlayerHero(engine, scale, offsetX, offsetY)

        // Draw Particles
        drawParticles(engine, scale, offsetX, offsetY)

        // Draw Floating Damage Numbers
        drawDamageNumbers(engine, scale, offsetX, offsetY)
    }
}

private fun DrawScope.drawArenaFloor(engine: GameEngine, scale: Float, offX: Float, offY: Float) {
    val floorColor = Color(engine.arenaDef.floorColor)
    val gridColor = Color(engine.arenaDef.gridColor)
    val wallColor = Color(engine.arenaDef.wallColor)
    val glowColor = Color(engine.arenaDef.glowColor)

    // Clear background
    drawRect(color = Color(0xFF07060B))

    // Arena Floor
    val arenaRectSize = Size(engine.arenaWidth * scale, engine.arenaHeight * scale)
    drawRect(
        color = floorColor,
        topLeft = Offset(offX, offY),
        size = arenaRectSize
    )

    // Arena Grid pattern
    val gridSize = 50f * scale
    var gx = offX
    while (gx < offX + arenaRectSize.width) {
        drawLine(
            color = gridColor.copy(alpha = 0.35f),
            start = Offset(gx, offY),
            end = Offset(gx, offY + arenaRectSize.height),
            strokeWidth = 1.2f
        )
        gx += gridSize
    }
    var gy = offY
    while (gy < offY + arenaRectSize.height) {
        drawLine(
            color = gridColor.copy(alpha = 0.35f),
            start = Offset(offX, gy),
            end = Offset(offX + arenaRectSize.width, gy),
            strokeWidth = 1.2f
        )
        gy += gridSize
    }

    // Outer boundary walls with glowing rim
    drawRect(
        color = wallColor.copy(alpha = 0.4f),
        topLeft = Offset(offX + 15f * scale, offY + 15f * scale),
        size = Size((engine.arenaWidth - 30f) * scale, (engine.arenaHeight - 30f) * scale),
        style = Stroke(width = 8f * scale)
    )
    drawRect(
        color = glowColor.copy(alpha = 0.7f),
        topLeft = Offset(offX + 20f * scale, offY + 20f * scale),
        size = Size((engine.arenaWidth - 40f) * scale, (engine.arenaHeight - 40f) * scale),
        style = Stroke(width = 2.5f * scale)
    )

    // Decorative corner brackets
    val cornerLen = 35f * scale
    val corners = listOf(
        Pair(Offset(offX + 20f * scale, offY + 20f * scale), Offset(1f, 1f)),
        Pair(Offset(offX + (engine.arenaWidth - 20f) * scale, offY + 20f * scale), Offset(-1f, 1f)),
        Pair(Offset(offX + 20f * scale, offY + (engine.arenaHeight - 20f) * scale), Offset(1f, -1f)),
        Pair(Offset(offX + (engine.arenaWidth - 20f) * scale, offY + (engine.arenaHeight - 20f) * scale), Offset(-1f, -1f))
    )
    corners.forEach { (pos, dir) ->
        drawLine(
            color = glowColor,
            start = pos,
            end = Offset(pos.x + dir.x * cornerLen, pos.y),
            strokeWidth = 4f * scale
        )
        drawLine(
            color = glowColor,
            start = pos,
            end = Offset(pos.x, pos.y + dir.y * cornerLen),
            strokeWidth = 4f * scale
        )
    }
}

private fun DrawScope.drawArenaObstacles(engine: GameEngine, scale: Float, offX: Float, offY: Float) {
    engine.arenaDef.obstacles.forEach { obs ->
        val center = Offset(offX + obs.x * scale, offY + obs.y * scale)
        val r = obs.radius * scale

        // Drop shadow
        drawCircle(
            color = Color(0x66000000),
            radius = r * 1.15f,
            center = Offset(center.x + 3f * scale, center.y + 5f * scale)
        )

        when (obs.type) {
            "tree" -> {
                // Mystical Tree: dark trunk + glowing canopy
                drawCircle(
                    color = Color(0xFF2C1935),
                    radius = r,
                    center = center
                )
                drawCircle(
                    color = Color(0xFF4A148C),
                    radius = r * 0.85f,
                    center = center
                )
                drawCircle(
                    color = Color(0xFF7C4DFF).copy(alpha = 0.8f),
                    radius = r * 0.45f,
                    center = center
                )
            }
            "pillar" -> {
                // Ancient Temple Pillar: stone pillar with runes
                drawCircle(
                    color = Color(0xFF3E2723),
                    radius = r,
                    center = center
                )
                drawCircle(
                    color = Color(0xFF5D4037),
                    radius = r * 0.85f,
                    center = center
                )
                drawCircle(
                    color = Color(0xFFFFD54F),
                    radius = r * 0.35f,
                    center = center
                )
            }
            "holocube" -> {
                // Cyber Hologram Cube
                drawRoundRect(
                    color = Color(0x5500E5FF),
                    topLeft = Offset(center.x - r * 0.8f, center.y - r * 0.8f),
                    size = Size(r * 1.6f, r * 1.6f),
                    cornerRadius = CornerRadius(6f * scale, 6f * scale)
                )
                drawRoundRect(
                    color = Color(0xFF00E5FF),
                    topLeft = Offset(center.x - r * 0.8f, center.y - r * 0.8f),
                    size = Size(r * 1.6f, r * 1.6f),
                    cornerRadius = CornerRadius(6f * scale, 6f * scale),
                    style = Stroke(width = 2.5f * scale)
                )
            }
            else -> {
                // Generic boulder
                drawCircle(
                    color = Color(0xFF263238),
                    radius = r,
                    center = center
                )
                drawCircle(
                    color = Color(0xFF37474F),
                    radius = r * 0.8f,
                    center = center
                )
            }
        }
    }
}

private fun DrawScope.drawGroundZones(engine: GameEngine, scale: Float, offX: Float, offY: Float) {
    engine.groundZones.forEach { zone ->
        val center = Offset(offX + zone.x * scale, offY + zone.y * scale)
        val r = zone.radius * scale
        val alphaFraction = (zone.duration / zone.maxDuration).coerceIn(0f, 1f)

        when (zone.type) {
            GroundZoneType.HEALING_AURA -> {
                drawCircle(
                    color = Color(0x4400E676),
                    radius = r,
                    center = center
                )
                drawCircle(
                    color = Color(0xFF00E676).copy(alpha = alphaFraction * 0.8f),
                    radius = r,
                    center = center,
                    style = Stroke(width = 2.5f * scale)
                )
            }
            GroundZoneType.POISON_CLOUD -> {
                drawCircle(
                    color = Color(0x5576FF03).copy(alpha = alphaFraction * 0.6f),
                    radius = r,
                    center = center
                )
                drawCircle(
                    color = Color(0xFF76FF03).copy(alpha = alphaFraction * 0.7f),
                    radius = r,
                    center = center,
                    style = Stroke(width = 2f * scale)
                )
            }
            GroundZoneType.BLIZZARD_FROST -> {
                drawCircle(
                    color = Color(0x4400E5FF).copy(alpha = alphaFraction * 0.6f),
                    radius = r,
                    center = center
                )
                drawCircle(
                    color = Color(0xFF80D8FF).copy(alpha = alphaFraction),
                    radius = r,
                    center = center,
                    style = Stroke(width = 3f * scale)
                )
            }
            GroundZoneType.GROUND_SMASH_CRATER -> {
                drawCircle(
                    color = Color(0x66FF6D00).copy(alpha = alphaFraction * 0.7f),
                    radius = r,
                    center = center
                )
                drawCircle(
                    color = Color(0xFFFFD54F).copy(alpha = alphaFraction),
                    radius = r,
                    center = center,
                    style = Stroke(width = 4f * scale)
                )
            }
            GroundZoneType.BOSS_WARNING_CIRCLE -> {
                // Warning circle filling up
                val progress = 1f - alphaFraction
                drawCircle(
                    color = Color(zone.color).copy(alpha = 0.25f),
                    radius = r,
                    center = center
                )
                drawCircle(
                    color = Color(zone.color).copy(alpha = 0.5f),
                    radius = r * progress,
                    center = center
                )
                drawCircle(
                    color = Color(zone.color),
                    radius = r,
                    center = center,
                    style = Stroke(width = 3f * scale)
                )
            }
            else -> {}
        }
    }
}

private fun DrawScope.drawCoins(engine: GameEngine, scale: Float, offX: Float, offY: Float) {
    engine.floatingCoins.forEach { coin ->
        val center = Offset(offX + coin.pos.x * scale, offY + coin.pos.y * scale)
        val r = 8f * scale

        if (coin.isGem) {
            // Gem (diamond shape)
            val p = Path().apply {
                moveTo(center.x, center.y - r * 1.3f)
                lineTo(center.x + r * 1.1f, center.y)
                lineTo(center.x, center.y + r * 1.3f)
                lineTo(center.x - r * 1.1f, center.y)
                close()
            }
            drawPath(p, Color(0xFF00E5FF))
            drawPath(p, Color.White, style = Stroke(width = 1.5f * scale))
        } else {
            // Gold coin
            drawCircle(color = Color(0xFFFFB300), radius = r, center = center)
            drawCircle(color = Color(0xFFFFEA00), radius = r * 0.7f, center = center)
            drawCircle(color = Color(0xFFFF8F00), radius = r, center = center, style = Stroke(width = 1.2f * scale))
        }
    }
}

private fun DrawScope.drawProjectiles(engine: GameEngine, scale: Float, offX: Float, offY: Float) {
    engine.projectiles.forEach { proj ->
        val center = Offset(offX + proj.pos.x * scale, offY + proj.pos.y * scale)
        val r = proj.radius * scale
        val color = Color(proj.color)

        // Outer glow
        drawCircle(
            color = color.copy(alpha = 0.45f),
            radius = r * 1.8f,
            center = center
        )
        // Core
        drawCircle(
            color = color,
            radius = r,
            center = center
        )
        drawCircle(
            color = Color.White,
            radius = r * 0.45f,
            center = center
        )
    }
}

private fun DrawScope.drawEnemies(engine: GameEngine, scale: Float, offX: Float, offY: Float) {
    engine.enemies.forEach { enemy ->
        if (!enemy.isAlive()) return@forEach

        val center = Offset(offX + enemy.pos.x * scale, offY + enemy.pos.y * scale)
        val r = enemy.radius * scale

        // Drop shadow
        drawCircle(
            color = Color(0x66000000),
            radius = r * 1.1f,
            center = Offset(center.x, center.y + 4f * scale)
        )

        // Hit flash or frozen color
        val baseColor = when {
            enemy.hitFlashTimer > 0f -> Color.White
            enemy.frozenTimer > 0f -> Color(0xFF80D8FF)
            enemy.isBoss -> Color(0xFFFF1744)
            enemy.type == EnemyType.ELITE -> Color(0xFFAB47BC)
            enemy.type == EnemyType.TANK -> Color(0xFF78909C)
            enemy.type == EnemyType.FAST -> Color(0xFFFF7043)
            enemy.type == EnemyType.RANGED -> Color(0xFF7E57C2)
            else -> Color(0xFF8E24AA) // Basic
        }

        // Enemy body
        drawCircle(
            color = baseColor,
            radius = r,
            center = center
        )
        drawCircle(
            color = if (enemy.isBoss) Color(0xFFFFD700) else Color(0x88000000),
            radius = r,
            center = center,
            style = Stroke(width = if (enemy.isBoss) 3.5f * scale else 2f * scale)
        )

        // Eyes pointing in facing angle
        val eyeDist = r * 0.5f
        val eyeR = r * 0.22f
        val eyeCenter = Offset(
            center.x + cos(enemy.facingAngle) * eyeDist,
            center.y + sin(enemy.facingAngle) * eyeDist
        )
        drawCircle(
            color = if (enemy.isBoss) Color(0xFFFFEA00) else Color(0xFFFF1744),
            radius = eyeR,
            center = eyeCenter
        )

        // Health bar above enemy
        val barWidth = r * 2.2f
        val barHeight = 4f * scale
        val barTop = center.y - r - 9f * scale
        val hpFrac = (enemy.health / enemy.maxHealth).coerceIn(0f, 1f)

        drawRect(
            color = Color(0x88000000),
            topLeft = Offset(center.x - barWidth / 2f, barTop),
            size = Size(barWidth, barHeight)
        )
        drawRect(
            color = if (enemy.isBoss) Color(0xFFFF1744) else Color(0xFF00E676),
            topLeft = Offset(center.x - barWidth / 2f, barTop),
            size = Size(barWidth * hpFrac, barHeight)
        )
    }
}

private fun DrawScope.drawPlayerHero(engine: GameEngine, scale: Float, offX: Float, offY: Float) {
    val player = engine.player
    val center = Offset(offX + player.pos.x * scale, offY + player.pos.y * scale)
    val r = player.radius * scale
    val hero = engine.heroDef

    // Drop shadow
    drawCircle(
        color = Color(0x66000000),
        radius = r * 1.15f,
        center = Offset(center.x, center.y + 5f * scale)
    )

    // Dash / Invulnerable Ghosting effect
    if (player.isDashing || player.invulnerableTimer > 0f) {
        drawCircle(
            color = Color(hero.glowColor).copy(alpha = 0.35f),
            radius = r * 1.55f,
            center = center
        )
    }

    // Aura ring
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(hero.glowColor).copy(alpha = 0.6f), Color.Transparent),
            center = center,
            radius = r * 1.8f
        ),
        radius = r * 1.8f,
        center = center
    )

    // Hero Core Body
    val bodyColor = if (player.damageFlashTimer > 0f) Color.White else Color(hero.primaryColor)
    drawCircle(
        color = bodyColor,
        radius = r,
        center = center
    )
    drawCircle(
        color = Color(hero.secondaryColor),
        radius = r * 0.7f,
        center = center
    )
    drawCircle(
        color = Color.White,
        radius = r,
        center = center,
        style = Stroke(width = 2.5f * scale)
    )

    // Weapon / Facing Direction Indicator
    val fwdX = cos(player.facingAngle)
    val fwdY = sin(player.facingAngle)
    val weaponStart = Offset(center.x + fwdX * (r * 0.7f), center.y + fwdY * (r * 0.7f))
    val weaponEnd = Offset(center.x + fwdX * (r * 1.6f), center.y + fwdY * (r * 1.6f))

    drawLine(
        color = Color(hero.glowColor),
        start = weaponStart,
        end = weaponEnd,
        strokeWidth = 5f * scale
    )
    drawCircle(
        color = Color.White,
        radius = 3.5f * scale,
        center = weaponEnd
    )

    // Attack Swing Slash Arc
    if (player.attackSwingTimer > 0f) {
        val arcRadius = r * 2.2f
        val arcPath = Path().apply {
            val startAngle = player.facingAngle - 0.75f
            val endAngle = player.facingAngle + 0.75f
            moveTo(center.x + cos(startAngle) * arcRadius, center.y + sin(startAngle) * arcRadius)
            for (step in 1..8) {
                val a = startAngle + (endAngle - startAngle) * (step / 8f)
                lineTo(center.x + cos(a) * arcRadius, center.y + sin(a) * arcRadius)
            }
        }
        drawPath(
            path = arcPath,
            color = Color(hero.glowColor),
            style = Stroke(width = 6f * scale)
        )
    }
}

private fun DrawScope.drawParticles(engine: GameEngine, scale: Float, offX: Float, offY: Float) {
    engine.particles.forEach { p ->
        val center = Offset(offX + p.x * scale, offY + p.y * scale)
        val r = p.radius * scale * (0.6f + p.alpha * 0.4f)
        val color = Color(p.color).copy(alpha = p.alpha)

        if (p.isSpark) {
            drawLine(
                color = color,
                start = center,
                end = Offset(center.x + p.vx * 0.05f * scale, center.y + p.vy * 0.05f * scale),
                strokeWidth = 2.5f * scale
            )
        } else {
            drawCircle(
                color = color,
                radius = r,
                center = center
            )
        }
    }
}

private fun DrawScope.drawDamageNumbers(engine: GameEngine, scale: Float, offX: Float, offY: Float) {
    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        isFakeBoldText = true
    }

    engine.damageNumbers.forEach { d ->
        paint.color = Color(d.color).copy(alpha = d.alpha).toArgb()
        paint.textSize = if (d.isCritical) 20f * scale else 15f * scale
        val scrX = offX + d.x * scale
        val scrY = offY + d.y * scale

        drawContext.canvas.nativeCanvas.drawText(
            d.text,
            scrX,
            scrY,
            paint
        )
    }
}
