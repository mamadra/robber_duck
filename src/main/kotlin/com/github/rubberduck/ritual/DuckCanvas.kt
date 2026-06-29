package com.github.rubberduck.ritual

import com.github.rubberduck.core.Mood
import com.github.rubberduck.render.DuckRenderer
import com.intellij.ui.scale.JBUIScale
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JComponent
import javax.swing.Timer

class DuckCanvas(private val sizeFactor: Float = 2.6f) : JComponent() {
    var mood: Mood = Mood.IDLE
        set(value) {
            field = value
            syncAnimation()
            repaint()
        }

    private val startNanos = System.nanoTime()
    private val timer = Timer(50) { repaint() }.apply { isRepeats = true }

    init {
        isOpaque = false
        val s = DuckRenderer.size(JBUIScale.scale(sizeFactor))
        preferredSize = Dimension(s, s)
    }

    override fun addNotify() {
        super.addNotify()
        syncAnimation()
    }

    override fun removeNotify() {
        timer.stop()
        super.removeNotify()
    }

    private fun syncAnimation() {
        val shouldAnimate = isShowing && mood.animated
        if (shouldAnimate && !timer.isRunning) timer.start()
        else if (!shouldAnimate && timer.isRunning) timer.stop()
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g as Graphics2D
        val s = DuckRenderer.size(JBUIScale.scale(sizeFactor))
        val x = (width - s) / 2
        val y = (height - s) / 2
        val phase = if (mood.animated) (System.nanoTime() - startNanos) / 1_000_000_000.0 else STATIC_PHASE
        DuckRenderer.paint(g2, x, y, s, mood, phase)
    }

    private companion object {
        const val STATIC_PHASE = 0.75
    }
}
