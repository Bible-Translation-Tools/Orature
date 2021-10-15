package org.wycliffeassociates.otter.jvm.workbookapp.updater.install4j.ui.view

import org.wycliffeassociates.otter.jvm.workbookapp.updater.install4j.ui.viewmodel.AppUpdaterViewModel
import tornadofx.*

class UpdaterView : View() {

    val vm: AppUpdaterViewModel by inject()

    init {
        vm.applyScheduledUpdate()
    }

    override val root = borderpane {
        styleClass.add("app-drawer__section")

        center = stackpane {
            add<NoUpdatesAvailable>()
            add<UpdateWillCompleteLaterFragment>()
            add<UpdateCompleteFragment>()
            add<UpdateDownloadingFragment>()
            add<UpdateAvailableFragment>()
            add<CheckForUpdatesFragment>()
        }
    }
}
