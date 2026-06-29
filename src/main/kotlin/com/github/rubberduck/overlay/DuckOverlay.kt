package com.github.rubberduck.overlay

import com.github.rubberduck.core.DuckMoodService
import com.github.rubberduck.render.DuckRenderer
import com.github.rubberduck.ritual.TellTheDuck
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.VisibleAreaEvent
import com.intellij.openapi.editor.event.VisibleAreaListener
import com.intellij.openapi.ui.AbstractPainter
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.IdeGlassPaneUtil
import com.intellij.ui.scale.JBUIScale
import java.awt.Component
import java.awt.Graphics2D
import java.awt.Rectangle
import javax.swing.Timer

class DuckOverlay(
    private val editor: Editor,
    private val moodService: DuckMoodService,
    parentDisposable: Disposable,
) : Disposable {
    private val phaseStartNanos = System.nanoTime()

    private var lastBounds: Rectangle? = null

    private val clickArea = DuckClickArea(editor, ::anchorBounds) { TellTheDuck.show(editor, moodService) }

    private val painter = object : AbstractPainter() {
        override fun needsRepaint(): Boolean = true
        override fun executePaint(component: Component, g: Graphics2D) {
            val b = anchorBounds() ?: return
            val mood = moodService.mood
            val phase = if (mood.animated) phaseSeconds() else STATIC_PHASE
            DuckRenderer.paint(g, b.x, b.y, b.width, mood, phase)
        }
    }

    private val animTimer = Timer(ANIM_PERIOD_MS) { lastBounds?.let { repaintRegion(it) } }

    init {
        Disposer.register(parentDisposable, this)
        animTimer.isRepeats = true

        IdeGlassPaneUtil.installPainter(editor.contentComponent, painter, this)
        clickArea.attach(this)

        editor.scrollingModel.addVisibleAreaListener(VisibleAreaListener { _: VisibleAreaEvent ->
            refresh()
        }, this)

        editor.project?.messageBus?.connect(this)?.subscribe(
            DuckMoodService.TOPIC,
            DuckMoodService.DuckMoodListener { onMoodChanged() },
        )

        refresh()
    }

    private fun spriteSize(): Int = DuckRenderer.size(JBUIScale.scale(1f))

    private fun phaseSeconds(): Double = (System.nanoTime() - phaseStartNanos) / 1_000_000_000.0

    private fun anchorBounds(): Rectangle? {
        if (editor.isDisposed) return null
        val visible = editor.scrollingModel.visibleArea
        if (visible.width <= 0 || visible.height <= 0) return null

        val size = spriteSize()
        val margin = JBUIScale.scale(MARGIN)
        if (visible.width < size + margin || visible.height < size + margin) return null

        val x = visible.x + visible.width - size - margin
        val y = visible.y + visible.height - size - margin
        return Rectangle(x, y, size, size)
    }

    private fun refresh() {
        if (editor.isDisposed) return
        val now = anchorBounds()
        if (now == null) {
            lastBounds?.let { repaintRegion(it) }
            lastBounds = null
            syncAnimation()
            return
        }
        val dirty = lastBounds?.union(now) ?: now
        lastBounds = now
        repaintRegion(dirty)
        syncAnimation()
    }

    private fun onMoodChanged() {
        syncAnimation()
        lastBounds?.let { repaintRegion(it) }
    }

    /** Run the frame timer only when there's something to animate (PANIC/NOD) and the duck is visible. */
    private fun syncAnimation() {
        val shouldAnimate = lastBounds != null && moodService.mood.animated
        if (shouldAnimate && !animTimer.isRunning) animTimer.start()
        else if (!shouldAnimate && animTimer.isRunning) animTimer.stop()
    }

    private fun repaintRegion(r: Rectangle) {
        val pad = JBUIScale.scale(4)
        editor.contentComponent.repaint(r.x - pad, r.y - pad, r.width + pad * 2, r.height + pad * 2)
    }

    override fun dispose() {
        animTimer.stop()
    }

    companion object {
        private const val ANIM_PERIOD_MS = 50
        private const val MARGIN = 16
        private const val STATIC_PHASE = 0.75
    }
}
