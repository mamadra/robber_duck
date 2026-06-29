package com.github.rubberduck.mood

import com.github.rubberduck.core.DuckMoodService
import com.github.rubberduck.core.Mood
import com.intellij.openapi.Disposable
import com.intellij.openapi.compiler.CompilationStatusListener
import com.intellij.openapi.compiler.CompileContext
import com.intellij.openapi.compiler.CompilerTopics
import com.intellij.openapi.project.Project
import com.intellij.util.Alarm

class CompilationMoodSource(
    private val project: Project,
    private val service: DuckMoodService,
    parent: Disposable,
) {
    private val calmDownAlarm = Alarm(Alarm.ThreadToUse.POOLED_THREAD, parent)

    init {
        project.messageBus.connect(parent).subscribe(
            CompilerTopics.COMPILATION_STATUS,
            object : CompilationStatusListener {
                override fun compilationFinished(
                    aborted: Boolean,
                    errors: Int,
                    warnings: Int,
                    compileContext: CompileContext,
                ) {
                    if (aborted) return
                    if (errors > 0) {
                        calmDownAlarm.cancelAllRequests()
                        service.setMood(Mood.PANIC)
                    } else {
                        service.setMood(Mood.HAPPY)
                        scheduleCalmDown()
                    }
                }
            },
        )
    }

    private fun scheduleCalmDown() {
        calmDownAlarm.cancelAllRequests()
        calmDownAlarm.addRequest({
            if (!project.isDisposed && service.mood == Mood.HAPPY) {
                service.setMood(Mood.IDLE)
            }
        }, HAPPY_LINGER_MS)
    }

    companion object {
        private const val HAPPY_LINGER_MS = 2500
    }
}
