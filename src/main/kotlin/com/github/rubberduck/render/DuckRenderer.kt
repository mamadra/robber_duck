package com.github.rubberduck.render

import com.github.rubberduck.core.Mood
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.Arc2D
import java.awt.geom.Ellipse2D
import java.awt.geom.Path2D
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

object DuckRenderer {
    const val BASE_SIZE: Int = 72

    private val BODY = Color(0xFF, 0xD5, 0x4A)
    private val BODY_SHADE = Color(0xF2, 0xB8, 0x1E)
    private val BEAK = Color(0xFF, 0x9E, 0x2C)
    private val EYE = Color(0x2B, 0x2B, 0x2B)
    private val PANIC_TINT = Color(0xFF, 0x6B, 0x5B, 90)
    private val SWEAT = Color(0x6F, 0xC6, 0xFF)
    private val BLUSH = Color(0xFF, 0x8A, 0x8A, 120)

    fun size(scale: Float): Int = (BASE_SIZE * scale).roundToInt()

    fun paint(g: Graphics2D, x: Int, y: Int, size: Int, mood: Mood, phaseSeconds: Double) {
        val g2 = g.create() as Graphics2D
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
            g2.translate(x, y)

            val s = size.toDouble()
            g2.scale(s, s)
            g2.stroke = BasicStroke((1.6f / size).coerceAtLeast(0.012f))

            when (mood) {
                Mood.IDLE -> drawDuck(g2, breath = breath(phaseSeconds), headTilt = 0.0)
                Mood.HAPPY -> drawDuck(g2, breath = breath(phaseSeconds) * 1.4, headTilt = -0.04, happy = true)
                Mood.PANIC -> drawDuck(g2, breath = breath(phaseSeconds), headTilt = 0.0, panic = true, shake = shake(phaseSeconds))
                Mood.NOD -> drawDuck(g2, breath = breath(phaseSeconds), headTilt = nodTilt(phaseSeconds))
            }
        } finally {
            g2.dispose()
        }
    }

    private fun breath(t: Double): Double = sin(t * 2.0 * PI / 3.0) * 0.5 + 0.5
    private fun shake(t: Double): Double = sin(t * 2.0 * PI * 9.0) * 0.012
    private fun nodTilt(t: Double): Double = (sin(t * 2.0 * PI * 1.6) * 0.5 + 0.5) * 0.22

    private fun drawDuck(
        g: Graphics2D,
        breath: Double,
        headTilt: Double,
        happy: Boolean = false,
        panic: Boolean = false,
        shake: Double = 0.0,
    ) {
        val grow = 0.02 * breath
        val cx = 0.5 + shake

        val bodyW = 0.62 + grow
        val bodyH = 0.46 + grow
        val bodyCy = 0.66
        fill(g, BODY, Ellipse2D.Double(cx - bodyW / 2, bodyCy - bodyH / 2, bodyW, bodyH))

        fill(g, BODY_SHADE, Arc2D.Double(
            (cx - bodyW / 2) * 1.0, (bodyCy - bodyH / 2), bodyW, bodyH, 200.0, 140.0, Arc2D.PIE,
        ))

        val tail = Path2D.Double().apply {
            moveTo(cx - bodyW / 2 + 0.02, bodyCy - 0.04)
            lineTo(cx - bodyW / 2 - 0.10, bodyCy - 0.12)
            lineTo(cx - bodyW / 2 + 0.04, bodyCy + 0.06)
            closePath()
        }
        fill(g, BODY, tail)

        val headR = 0.30
        val headCx = cx + 0.14
        val headCy = 0.38 + headTilt
        fill(g, BODY, Ellipse2D.Double(headCx - headR / 2, headCy - headR / 2, headR, headR))

        val beakY = headCy + 0.02 + headTilt * 0.4
        val beak = Path2D.Double().apply {
            moveTo(headCx + headR / 2 - 0.02, beakY - 0.05)
            lineTo(headCx + headR / 2 + 0.20, beakY)
            lineTo(headCx + headR / 2 - 0.02, beakY + 0.05)
            closePath()
        }
        fill(g, BEAK, beak)

        val eyeX = headCx + 0.06
        val eyeY = headCy - 0.05 + headTilt * 0.5
        when {
            happy -> {
                stroke(g, EYE, Arc2D.Double(eyeX - 0.04, eyeY - 0.02, 0.08, 0.06, 200.0, 140.0, Arc2D.OPEN))
                fill(g, BLUSH, Ellipse2D.Double(eyeX - 0.10, eyeY + 0.06, 0.07, 0.045))
            }
            panic -> {
                fill(g, Color.WHITE, Ellipse2D.Double(eyeX - 0.05, eyeY - 0.05, 0.10, 0.11))
                fill(g, EYE, Ellipse2D.Double(eyeX - 0.018, eyeY - 0.005, 0.036, 0.04))
                val drop = Path2D.Double().apply {
                    val dx = headCx - 0.12
                    val dy = headCy - 0.14
                    moveTo(dx, dy)
                    curveTo(dx + 0.05, dy + 0.06, dx + 0.045, dy + 0.11, dx, dy + 0.11)
                    curveTo(dx - 0.045, dy + 0.11, dx - 0.05, dy + 0.06, dx, dy)
                    closePath()
                }
                fill(g, SWEAT, drop)
            }
            else -> {
                fill(g, EYE, Ellipse2D.Double(eyeX - 0.028, eyeY - 0.028, 0.056, 0.056))
                fill(g, Color.WHITE, Ellipse2D.Double(eyeX - 0.008, eyeY - 0.018, 0.018, 0.018))
            }
        }

        if (panic) {
            fill(g, PANIC_TINT, Ellipse2D.Double(cx - bodyW / 2, bodyCy - bodyH / 2, bodyW, bodyH))
        }
    }

    private fun fill(g: Graphics2D, c: Color, shape: java.awt.Shape) {
        g.color = c
        g.fill(shape)
    }

    private fun stroke(g: Graphics2D, c: Color, shape: java.awt.Shape) {
        g.color = c
        g.draw(shape)
    }
}
