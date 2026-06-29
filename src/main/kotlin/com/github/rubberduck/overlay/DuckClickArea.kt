package com.github.rubberduck.overlay

import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.wm.IdeGlassPaneUtil
import java.awt.Rectangle
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.SwingUtilities

/**
 * Catches clicks on the duck without adding a child to the editor. It registers a mouse
 * preprocessor on the IDE glass pane and only consumes a press that lands inside the duck's
 * inscribed ellipse; every other event passes straight through to the IDE.
 */
class DuckClickArea(
    private val editor: Editor,
    private val boundsProvider: () -> Rectangle?,
    private val onClick: () -> Unit,
) {
    fun attach(parent: Disposable) {
        val glassPane = IdeGlassPaneUtil.find(editor.contentComponent)
        glassPane.addMousePreprocessor(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                val bounds = boundsProvider() ?: return
                val content = editor.contentComponent
                if (!content.isShowing) return

                val p = SwingUtilities.convertPoint(e.component, e.point, content)
                if (!hit(bounds, p.x, p.y)) return

                onClick()
                e.consume()
            }
        }, parent)
    }

    private fun hit(b: Rectangle, x: Int, y: Int): Boolean {
        if (!b.contains(x, y)) return false
        val nx = (x - b.centerX) / (b.width / 2.0)
        val ny = (y - b.centerY) / (b.height / 2.0)
        return nx * nx + ny * ny <= 1.0
    }
}
