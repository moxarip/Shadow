package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import com.example.game.engine.GameEngine
import com.example.game.engine.StagePlatform
import com.example.game.model.EnemyWarrior
import com.example.game.model.PlayerWarrior
import com.example.game.model.StickFigurePose
import com.example.game.model.Vector2
import com.example.game.model.WeaponType
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun ArenaCanvas(
    engine: GameEngine,
    modifier: Modifier = Modifier,
    onTouchMove: (Vector2) -> Unit = {}
) {
    val density = LocalDensity.current.density
    val maxTouchRadiusPx = 70f * density

    var touchActive by remember { mutableStateOf(false) }
    var touchStartOffset by remember { mutableStateOf(Offset.Zero) }
    var touchCurrentOffset by remember { mutableStateOf(Offset.Zero) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF06050C))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { start ->
                        touchActive = true
                        touchStartOffset = start
                        touchCurrentOffset = start
                    },
                    onDragEnd = {
                        touchActive = false
                        onTouchMove(Vector2(0f, 0f))
                    },
                    onDragCancel = {
                        touchActive = false
                        onTouchMove(Vector2(0f, 0f))
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        touchCurrentOffset += dragAmount
                        val dx = touchCurrentOffset.x - touchStartOffset.x
                        val dy = touchCurrentOffset.y - touchStartOffset.y
                        val dist = sqrt(dx * dx + dy * dy)

                        if (dist > 6f * density) {
                            val clampedDist = dist.coerceAtMost(maxTouchRadiusPx)
                            val factor = clampedDist / maxTouchRadiusPx
                            val dirX = (dx / dist) * factor
                            val dirY = (dy / dist) * factor
                            onTouchMove(Vector2(dirX, dirY))
                        } else {
                            onTouchMove(Vector2(0f, 0f))
                        }
                    }
                )
            }
    ) {
        val viewW = engine.viewportWidth
        val viewH = engine.viewportHeight
        val scale = minOf(size.width / viewW, size.height / viewH)
        val origX = (size.width - viewW * scale) / 2f + engine.screenShakeOffsetX
        val origY = (size.height - viewH * scale) / 2f + engine.screenShakeOffsetY

        val camX = engine.cameraX
        val world = engine.worldDef

        fun toScreen(worldX: Float, worldY: Float): Offset {
            return Offset(origX + (worldX - camX) * scale, origY + worldY * scale)
        }

        // 1. SKY GRADIENT (Full canvas)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(world.skyColorTop), Color(world.skyColorBottom)),
                startY = origY,
                endY = origY + viewH * scale
            ),
            topLeft = Offset(origX, origY),
            size = Size(viewW * scale, viewH * scale)
        )

        // 2. PARALLAX LAYER 1: Far silhouettes (mountains / towers / spires)
        val farParallax = camX * 0.25f
        val spireSpacing = 180f
        var sx = -spireSpacing + (-(farParallax % spireSpacing))
        while (sx < viewW + spireSpacing) {
            val h = 180f + ((sx * 13f) % 90f)
            drawRect(
                color = Color(world.farSilhouetteColor).copy(alpha = 0.55f),
                topLeft = Offset(origX + sx * scale, origY + (engine.groundY - h) * scale),
                size = Size(100f * scale, h * scale)
            )
            sx += spireSpacing
        }

        // 3. PARALLAX LAYER 2: Mid-ground arches & pillars
        val midParallax = camX * 0.55f
        val archSpacing = 260f
        var mx = -archSpacing + (-(midParallax % archSpacing))
        while (mx < viewW + archSpacing) {
            // Arch pillar
            drawLine(
                color = Color(world.neonAccentColor).copy(alpha = 0.22f),
                start = Offset(origX + mx * scale, origY + (engine.groundY - 140f) * scale),
                end = Offset(origX + mx * scale, origY + engine.groundY * scale),
                strokeWidth = 3f * scale
            )
            drawCircle(
                color = Color(world.neonAccentColor).copy(alpha = 0.35f),
                center = Offset(origX + mx * scale, origY + (engine.groundY - 140f) * scale),
                radius = 6f * scale
            )
            mx += archSpacing
        }

        // 4. PLATFORMS
        for (plat in engine.platforms) {
            val pLeft = origX + (plat.x - camX) * scale
            val pTop = origY + plat.y * scale
            val pWidth = plat.width * scale
            val pHeight = plat.height * scale

            if (pLeft + pWidth >= origX && pLeft <= origX + viewW * scale) {
                // Platform body
                drawRect(
                    color = Color(world.platformColor),
                    topLeft = Offset(pLeft, pTop),
                    size = Size(pWidth, pHeight)
                )
                // Glowing top surface
                drawLine(
                    color = Color(world.neonAccentColor),
                    start = Offset(pLeft, pTop),
                    end = Offset(pLeft + pWidth, pTop),
                    strokeWidth = 3f * scale
                )
            }
        }

        // 5. GROUND FLOOR
        val gY = origY + engine.groundY * scale
        val gHeight = (viewH - engine.groundY) * scale
        drawRect(
            color = Color(world.groundColor),
            topLeft = Offset(origX, gY),
            size = Size(viewW * scale, gHeight)
        )
        // Neon boundary line
        drawLine(
            color = Color(world.neonAccentColor),
            start = Offset(origX, gY),
            end = Offset(origX + viewW * scale, gY),
            strokeWidth = 3.5f * scale
        )

        // 6. STAGE EXIT PORTAL GATE (At end of stage)
        val portalX = engine.stageLength - 70f
        val portalScr = toScreen(portalX, engine.groundY - 60f)
        if (portalScr.x in (origX - 60f)..(origX + viewW * scale + 60f)) {
            // Portal Ring
            drawCircle(
                color = Color(world.neonAccentColor).copy(alpha = 0.25f),
                center = portalScr,
                radius = 48f * scale
            )
            drawCircle(
                color = Color(world.neonAccentColor),
                center = portalScr,
                radius = 48f * scale,
                style = Stroke(width = 3f * scale)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.7f),
                center = portalScr,
                radius = 20f * scale
            )
        }

        // 7. DRAW COINS
        for (c in engine.coins) {
            if (c.active) {
                val cPos = toScreen(c.x, c.y)
                if (cPos.x in (origX - 20f)..(origX + viewW * scale + 20f)) {
                    drawCircle(
                        color = Color(0xFFFFD700),
                        center = cPos,
                        radius = 8f * scale
                    )
                    drawCircle(
                        color = Color(0xFFFFF9C4),
                        center = cPos,
                        radius = 4f * scale
                    )
                }
            }
        }

        // 8. DRAW PROJECTILES
        for (p in engine.projectiles) {
            if (p.active) {
                val pPos = toScreen(p.x, p.y)
                if (pPos.x in (origX - 30f)..(origX + viewW * scale + 30f)) {
                    drawCircle(
                        color = Color(p.color).copy(alpha = 0.35f),
                        center = pPos,
                        radius = (p.radius * 2f) * scale
                    )
                    drawCircle(
                        color = Color(p.color),
                        center = pPos,
                        radius = p.radius * scale
                    )
                    drawCircle(
                        color = Color.White,
                        center = pPos,
                        radius = (p.radius * 0.5f) * scale
                    )
                }
            }
        }

        // 9. DRAW ENEMIES
        for (e in engine.enemies) {
            if (e.active) {
                val ePos = toScreen(e.pos.x, e.pos.y)
                if (ePos.x in (origX - 80f)..(origX + viewW * scale + 80f)) {
                    drawShadowWarrior(
                        scope = this,
                        pos = ePos,
                        pose = e.pose,
                        facingRight = e.facingRight,
                        scale = scale * (if (e.isBoss) 1.55f else 1.0f),
                        silhouetteColor = if (e.hitFlashTimer > 0f) 0xFFFFFFFF else 0xFF14131A,
                        energyColor = if (e.isBoss) e.bossDef?.color ?: 0xFFFF1744 else e.type.color,
                        weaponType = if (e.isBoss) WeaponType.DRAGON_BLADE else WeaponType.ENERGY_KATANA
                    )

                    // Enemy HP Bar
                    if (e.hp < e.maxHp && e.isAlive() && !e.isBoss) {
                        val barW = 38f * scale
                        val barH = 4f * scale
                        val hpFrac = (e.hp / e.maxHp).coerceIn(0f, 1f)
                        drawRect(
                            color = Color(0xFF261214),
                            topLeft = Offset(ePos.x - barW / 2f, ePos.y - 65f * scale),
                            size = Size(barW, barH)
                        )
                        drawRect(
                            color = Color(0xFFFF1744),
                            topLeft = Offset(ePos.x - barW / 2f, ePos.y - 65f * scale),
                            size = Size(barW * hpFrac, barH)
                        )
                    }
                }
            }
        }

        // 10. DRAW PLAYER WARRIOR
        val pScr = toScreen(engine.player.pos.x, engine.player.pos.y)
        // Dash Ghost Afterimages
        if (engine.player.isDashing) {
            val dir = if (engine.player.facingRight) -1f else 1f
            val ghostScr = Offset(pScr.x + dir * 28f * scale, pScr.y)
            drawShadowWarrior(
                scope = this,
                pos = ghostScr,
                pose = engine.player.pose,
                facingRight = engine.player.facingRight,
                scale = scale,
                silhouetteColor = 0xFF111116,
                energyColor = engine.playerSkin.glowColor,
                weaponType = engine.weaponDef.type,
                alpha = 0.35f
            )
        }

        drawShadowWarrior(
            scope = this,
            pos = pScr,
            pose = engine.player.pose,
            facingRight = engine.player.facingRight,
            scale = scale,
            silhouetteColor = if (engine.player.hitFlashTimer > 0f) 0xFFFFFFFF else engine.playerSkin.primaryColor,
            energyColor = engine.playerSkin.glowColor,
            weaponType = engine.weaponDef.type,
            alpha = if (engine.player.invulnerableTimer > 0f) 0.65f else 1.0f
        )

        // 11. COMBAT PARTICLES
        for (pt in engine.particles) {
            if (pt.active) {
                val ptPos = toScreen(pt.x, pt.y)
                val alpha = (1f - (pt.life / pt.maxLife)).coerceIn(0f, 1f)
                if (pt.isSlashTrail) {
                    // Slash arc line
                    drawLine(
                        color = Color(pt.color).copy(alpha = alpha),
                        start = ptPos,
                        end = Offset(ptPos.x + pt.vx * 0.08f * scale, ptPos.y + pt.vy * 0.08f * scale),
                        strokeWidth = 3.5f * scale,
                        cap = StrokeCap.Round
                    )
                } else {
                    drawCircle(
                        color = Color(pt.color).copy(alpha = alpha),
                        center = ptPos,
                        radius = pt.radius * scale
                    )
                }
            }
        }

        // 12. FLOATING DAMAGE NUMBERS
        val paint = Paint().apply {
            textSize = 19f * scale
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        for (dn in engine.damageNumbers) {
            if (dn.active) {
                val dnPos = toScreen(dn.x, dn.y)
                val alpha = ((1f - (dn.life / dn.maxLife)) * 255).toInt().coerceIn(0, 255)
                paint.color = (dn.color.toInt() and 0x00FFFFFF) or (alpha shl 24)
                if (dn.isCrit) {
                    paint.textSize = 23f * scale
                }
                drawContext.canvas.nativeCanvas.drawText(
                    dn.text,
                    dnPos.x,
                    dnPos.y,
                    paint
                )
            }
        }

        // 13. TOUCH MOVEMENT RETICLE
        if (touchActive) {
            drawTouchMovementIndicator(
                scope = this,
                start = touchStartOffset,
                current = touchCurrentOffset,
                maxRadius = maxTouchRadiusPx,
                glowColor = engine.playerSkin.glowColor
            )
        }
    }
}

/**
 * Procedural Stylized 2D Stickman / Shadow Warrior Renderer.
 * High-performance line and arc rendering with zero object allocations.
 */
private fun drawShadowWarrior(
    scope: DrawScope,
    pos: Offset,
    pose: StickFigurePose,
    facingRight: Boolean,
    scale: Float,
    silhouetteColor: Long,
    energyColor: Long,
    weaponType: WeaponType,
    alpha: Float = 1.0f
) {
    val dir = if (facingRight) 1f else -1f
    val baseColor = Color(silhouetteColor).copy(alpha = alpha)
    val neonColor = Color(energyColor).copy(alpha = alpha)
    val bodyWidth = 4.5f * scale

    val hip = Offset(pos.x, pos.y - 18f * scale + pose.crouchY * scale)
    val torsoLength = 22f * scale
    val torsoEnd = Offset(
        hip.x + sin(pose.torsoAngle) * torsoLength,
        hip.y - cos(pose.torsoAngle) * torsoLength
    )

    // 1. LEGS (Hip -> Knee -> Foot)
    val thighLen = 13f * scale
    val shinLen = 13f * scale

    // Left leg
    val leftKnee = Offset(
        hip.x + sin(pose.leftThighAngle * dir) * thighLen,
        hip.y + cos(pose.leftThighAngle * dir) * thighLen
    )
    val leftFoot = Offset(
        leftKnee.x + sin((pose.leftThighAngle + pose.leftShinAngle) * dir) * shinLen,
        leftKnee.y + cos((pose.leftThighAngle + pose.leftShinAngle) * dir) * shinLen
    )
    scope.drawLine(baseColor, hip, leftKnee, strokeWidth = bodyWidth, cap = StrokeCap.Round)
    scope.drawLine(baseColor, leftKnee, leftFoot, strokeWidth = bodyWidth, cap = StrokeCap.Round)

    // Right leg
    val rightKnee = Offset(
        hip.x + sin(pose.rightThighAngle * dir) * thighLen,
        hip.y + cos(pose.rightThighAngle * dir) * thighLen
    )
    val rightFoot = Offset(
        rightKnee.x + sin((pose.rightThighAngle + pose.rightShinAngle) * dir) * shinLen,
        rightKnee.y + cos((pose.rightThighAngle + pose.rightShinAngle) * dir) * shinLen
    )
    scope.drawLine(baseColor, hip, rightKnee, strokeWidth = bodyWidth, cap = StrokeCap.Round)
    scope.drawLine(baseColor, rightKnee, rightFoot, strokeWidth = bodyWidth, cap = StrokeCap.Round)

    // 2. TORSO
    scope.drawLine(baseColor, hip, torsoEnd, strokeWidth = bodyWidth * 1.15f, cap = StrokeCap.Round)
    // Energy core on chest
    scope.drawCircle(color = neonColor, radius = 2.5f * scale, center = Offset(torsoEnd.x, torsoEnd.y + 6f * scale))

    // 3. HEAD & GLOWING EYE SLIT
    val headCenter = Offset(torsoEnd.x, torsoEnd.y - 10f * scale)
    val headRadius = 9f * scale
    scope.drawCircle(color = baseColor, radius = headRadius, center = headCenter)
    // Glowing warrior eye
    val eyeOffset = Offset(headCenter.x + dir * 4.5f * scale, headCenter.y - 1f * scale)
    scope.drawCircle(color = neonColor, radius = 2.2f * scale, center = eyeOffset)

    // 4. ARMS & WEAPON
    val shoulder = Offset(torsoEnd.x, torsoEnd.y + 2f * scale)
    val upperArmLen = 11f * scale
    val forearmLen = 11f * scale

    // Left arm (Back arm)
    val leftElbow = Offset(
        shoulder.x + sin(pose.leftUpperArmAngle * dir) * upperArmLen,
        shoulder.y + cos(pose.leftUpperArmAngle * dir) * upperArmLen
    )
    val leftHand = Offset(
        leftElbow.x + sin(pose.leftForearmAngle * dir) * forearmLen,
        leftElbow.y + cos(pose.leftForearmAngle * dir) * forearmLen
    )
    scope.drawLine(baseColor, shoulder, leftElbow, strokeWidth = bodyWidth * 0.9f, cap = StrokeCap.Round)
    scope.drawLine(baseColor, leftElbow, leftHand, strokeWidth = bodyWidth * 0.9f, cap = StrokeCap.Round)

    // Right arm (Front weapon arm)
    val rightElbow = Offset(
        shoulder.x + sin(pose.rightUpperArmAngle * dir) * upperArmLen,
        shoulder.y + cos(pose.rightUpperArmAngle * dir) * upperArmLen
    )
    val rightHand = Offset(
        rightElbow.x + sin(pose.rightForearmAngle * dir) * forearmLen,
        rightElbow.y + cos(pose.rightForearmAngle * dir) * forearmLen
    )
    scope.drawLine(baseColor, shoulder, rightElbow, strokeWidth = bodyWidth * 0.9f, cap = StrokeCap.Round)
    scope.drawLine(baseColor, rightElbow, rightHand, strokeWidth = bodyWidth * 0.9f, cap = StrokeCap.Round)

    // 5. DRAW WEAPON IN HAND
    val wAngle = pose.weaponAngle * dir
    val wLen = when (weaponType) {
        WeaponType.MASSIVE_HAMMER, WeaponType.DRAGON_BLADE -> 38f * scale
        WeaponType.ENERGY_SPEAR -> 46f * scale
        WeaponType.ENERGY_BOW -> 30f * scale
        WeaponType.DUAL_PISTOLS -> 16f * scale
        else -> 32f * scale
    }
    val wTip = Offset(
        rightHand.x + cos(wAngle) * wLen * dir,
        rightHand.y + sin(wAngle) * wLen
    )

    // Weapon blade / shaft
    scope.drawLine(neonColor, rightHand, wTip, strokeWidth = bodyWidth * 0.9f, cap = StrokeCap.Round)

    // Special weapon silhouettes
    when (weaponType) {
        WeaponType.MASSIVE_HAMMER -> {
            // Hammer head
            scope.drawRect(
                color = neonColor,
                topLeft = Offset(wTip.x - 7f * scale, wTip.y - 12f * scale),
                size = Size(14f * scale, 24f * scale)
            )
        }
        WeaponType.ENERGY_BOW -> {
            // Bow arc
            scope.drawCircle(
                color = neonColor,
                center = rightHand,
                radius = 16f * scale,
                style = Stroke(width = 2.5f * scale)
            )
        }
        WeaponType.ENERGY_CANNON -> {
            // Cannon barrel
            scope.drawRect(
                color = neonColor,
                topLeft = Offset(rightHand.x, rightHand.y - 5f * scale),
                size = Size(26f * scale, 10f * scale)
            )
        }
        WeaponType.FLAME_GAUNTLETS -> {
            // Fiery fists
            scope.drawCircle(color = neonColor, radius = 7f * scale, center = rightHand)
            scope.drawCircle(color = neonColor, radius = 6f * scale, center = leftHand)
        }
        else -> {}
    }
}

private fun drawTouchMovementIndicator(
    scope: DrawScope,
    start: Offset,
    current: Offset,
    maxRadius: Float,
    glowColor: Long
) {
    val dx = current.x - start.x
    val dy = current.y - start.y
    val dist = sqrt(dx * dx + dy * dy)
    val clampedDist = dist.coerceAtMost(maxRadius)

    val knobPos = if (dist > 0.001f) {
        Offset(start.x + (dx / dist) * clampedDist, start.y + (dy / dist) * clampedDist)
    } else {
        start
    }

    val strokeWidth = 2f * (scope.size.width / 360f).coerceIn(1f, 3f)
    val thumbRadius = 16f * (scope.size.width / 360f).coerceIn(1f, 3f)

    // Outer base circle
    scope.drawCircle(
        color = Color(glowColor).copy(alpha = 0.18f),
        center = start,
        radius = maxRadius
    )
    scope.drawCircle(
        color = Color(glowColor).copy(alpha = 0.5f),
        center = start,
        radius = maxRadius,
        style = Stroke(width = strokeWidth)
    )

    // Connector line
    scope.drawLine(
        color = Color(glowColor).copy(alpha = 0.6f),
        start = start,
        end = knobPos,
        strokeWidth = strokeWidth * 1.5f,
        cap = StrokeCap.Round
    )

    // Knob thumb
    scope.drawCircle(
        color = Color.White.copy(alpha = 0.9f),
        center = knobPos,
        radius = thumbRadius
    )
    scope.drawCircle(
        color = Color(glowColor),
        center = knobPos,
        radius = thumbRadius,
        style = Stroke(width = strokeWidth * 1.5f)
    )
}
