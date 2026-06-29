package com.github.rubberduck.startup

import com.github.rubberduck.overlay.DuckOverlayManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class DuckStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        val manager = DuckOverlayManager.getInstance(project)
        ApplicationManager.getApplication().invokeLater({
            if (!project.isDisposed) manager.start()
        }, project.disposed)
    }
}
