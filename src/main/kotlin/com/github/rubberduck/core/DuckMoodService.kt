package com.github.rubberduck.core

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic
import java.util.concurrent.atomic.AtomicReference

@Service(Service.Level.PROJECT)
class DuckMoodService(private val project: Project) {
    private val current = AtomicReference(Mood.IDLE)

    val mood: Mood
        get() = current.get()

    fun setMood(mood: Mood) {
        val previous = current.getAndSet(mood)
        if (previous == mood) return
        publishOnEdt(mood)
    }

    private fun publishOnEdt(mood: Mood) {
        val app = ApplicationManager.getApplication()
        val publish = Runnable {
            if (project.isDisposed) return@Runnable
            project.messageBus.syncPublisher(TOPIC).moodChanged(mood)
        }
        if (app.isDispatchThread) publish.run() else app.invokeLater(publish, project.disposed)
    }

    fun interface DuckMoodListener {
        fun moodChanged(mood: Mood)
    }

    companion object {
        @JvmField
        val TOPIC: Topic<DuckMoodListener> =
            Topic.create("Rubber Duck mood", DuckMoodListener::class.java)

        fun getInstance(project: Project): DuckMoodService = project.service()
    }
}
