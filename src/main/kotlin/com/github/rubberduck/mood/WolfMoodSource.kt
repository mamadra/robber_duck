package com.github.rubberduck.mood

import com.github.rubberduck.core.DuckMoodService
import com.github.rubberduck.core.Mood
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.problems.WolfTheProblemSolver
import com.intellij.util.Alarm

class WolfMoodSource(
    private val project: Project,
    private val service: DuckMoodService,
    parent: Disposable,
) {
    private val pollAlarm = Alarm(Alarm.ThreadToUse.POOLED_THREAD, parent)

    init {
        scheduleNextPoll()
    }

    private fun scheduleNextPoll() {
        if (pollAlarm.isDisposed) return
        pollAlarm.addRequest({ poll() }, POLL_INTERVAL_MS)
    }

    private fun poll() {
        if (project.isDisposed) return
        try {
            if (DumbService.isDumb(project)) return

            val hasProblems = ReadAction.compute<Boolean, RuntimeException> {
                if (project.isDisposed) false
                else WolfTheProblemSolver.getInstance(project).hasProblemFilesBeneath { true }
            }

            when (service.mood) {
                Mood.IDLE -> if (hasProblems) service.setMood(Mood.PANIC)
                Mood.PANIC -> if (!hasProblems) service.setMood(Mood.IDLE)
                else -> Unit
            }
        } finally {
            scheduleNextPoll()
        }
    }

    companion object {
        private const val POLL_INTERVAL_MS = 1500
    }
}
