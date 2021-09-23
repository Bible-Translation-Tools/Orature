package org.wycliffeassociates.otter.jvm.workbookapp.updater.install4j

import com.install4j.api.launcher.Variables
import com.install4j.api.update.ApplicationDisplayMode
import com.install4j.api.update.UpdateChecker
import com.install4j.api.update.UpdateDescriptor

class AppUpdater() {
    fun checkUpdate(): UpdateDescriptor  {
        val updateUrl: String = Variables.getCompilerVariable("sys.updatesUrl")
        return UpdateChecker.getUpdateDescriptor(updateUrl, ApplicationDisplayMode.GUI)
    }

    fun updateAndRestart() {
        UpdateChecker.executeScheduledUpdate(null, true, null)
    }
}
