package com.github.rubberduck.overlay

import com.github.rubberduck.core.DuckMoodService
import com.github.rubberduck.mood.CompilationMoodSource
import com.github.rubberduck.mood.WolfMoodSource
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer

@Service(Service.Level.PROJECT)
class DuckOverlayManager(private val project: Project) : Disposable {
    private var overlay: DuckOverlay? = null
    private var currentEditor: Editor? = null
    private var started = false

    fun start() {
        if (started) return
        started = true

        val moodService = DuckMoodService.getInstance(project)

        CompilationMoodSource(project, moodService, this)
        WolfMoodSource(project, moodService, this)

        project.messageBus.connect(this).subscribe(
            FileEditorManagerListener.FILE_EDITOR_MANAGER,
            object : FileEditorManagerListener {
                override fun selectionChanged(event: FileEditorManagerEvent) = refreshOnEdt()
            },
        )

        EditorFactory.getInstance().addEditorFactoryListener(object : EditorFactoryListener {
            override fun editorReleased(event: EditorFactoryEvent) {
                if (event.editor === currentEditor) disposeOverlay()
            }
        }, this)

        refreshOnEdt()
    }

    private fun disposeOverlay() {
        overlay?.let { Disposer.dispose(it) }
        overlay = null
        currentEditor = null
    }

    private fun refreshOnEdt() {
        val app = ApplicationManager.getApplication()
        val task = Runnable { refresh() }
        if (app.isDispatchThread) task.run() else app.invokeLater(task, project.disposed)
    }

    private fun refresh() {
        if (project.isDisposed) return
        val editor = FileEditorManager.getInstance(project).selectedTextEditor
        if (editor === currentEditor) return

        overlay?.let { Disposer.dispose(it) }
        overlay = null
        currentEditor = editor

        if (editor != null && !editor.isDisposed) {
            overlay = DuckOverlay(editor, DuckMoodService.getInstance(project), this)
        }
    }

    override fun dispose() {
        overlay = null
        currentEditor = null
    }

    companion object {
        fun getInstance(project: Project): DuckOverlayManager = project.service()
    }
}
