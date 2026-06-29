package com.github.rubberduck.ritual

import com.github.rubberduck.core.DuckMoodService
import com.github.rubberduck.core.Mood
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.JBPopupListener
import com.intellij.openapi.ui.popup.LightweightWindowEvent
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextArea
import com.intellij.util.Alarm
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.JPanel
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

object TellTheDuck {
    fun show(editor: Editor, service: DuckMoodService) {
        val canvas = DuckCanvas().apply { mood = service.mood }

        val prompt = JBLabel("Tell me what's wrong. I'm listening.")
        val hint = JBLabel("(I won't answer — just say it out loud.)").apply {
            foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
        }
        val quack = JBLabel(" ").apply {
            foreground = JBColor(Color(0xC9, 0x7A, 0x14), Color(0xE0, 0xA0, 0x50))
        }

        val textArea = JBTextArea(4, 32).apply {
            lineWrap = true
            wrapStyleWord = true
            emptyText.text = "…it just doesn't work and I don't know why"
        }

        val previousMood = service.mood
        val quackAlarm = Alarm(Alarm.ThreadToUse.SWING_THREAD)

        textArea.document.addDocumentListener(object : DocumentListener {
            private fun onEdit() {
                service.setMood(Mood.NOD)
                canvas.mood = Mood.NOD
                quack.text = " "
                quackAlarm.cancelAllRequests()
                quackAlarm.addRequest({ quack.text = "Quack." }, QUACK_DELAY_MS)
            }

            override fun insertUpdate(e: DocumentEvent) = onEdit()
            override fun removeUpdate(e: DocumentEvent) = onEdit()
            override fun changedUpdate(e: DocumentEvent) = Unit
        })

        val top = JPanel(BorderLayout(JBUI.scale(8), JBUI.scale(4))).apply {
            isOpaque = false
            add(prompt, BorderLayout.NORTH)
            add(hint, BorderLayout.SOUTH)
        }

        val content = JPanel(BorderLayout(JBUI.scale(12), JBUI.scale(10))).apply {
            border = JBUI.Borders.empty(14)
            add(canvas, BorderLayout.WEST)
            add(top, BorderLayout.NORTH)
            add(com.intellij.ui.components.JBScrollPane(textArea), BorderLayout.CENTER)
            add(quack, BorderLayout.SOUTH)
        }

        val popup: JBPopup = JBPopupFactory.getInstance()
            .createComponentPopupBuilder(content, textArea)
            .setTitle("Tell the Duck")
            .setRequestFocus(true)
            .setMovable(true)
            .setResizable(false)
            .setFocusable(true)
            .createPopup()

        popup.addListener(object : JBPopupListener {
            override fun onClosed(event: LightweightWindowEvent) {
                quackAlarm.cancelAllRequests()

                if (service.mood == Mood.NOD) {
                    service.setMood(if (previousMood == Mood.NOD) Mood.IDLE else previousMood)
                }
            }
        })

        popup.showInBestPositionFor(editor)
    }

    private const val QUACK_DELAY_MS = 1300
}
