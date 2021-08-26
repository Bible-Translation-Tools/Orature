package org.wycliffeassociates.otter.jvm.workbookapp.updater.install4j.ui.view

import org.wycliffeassociates.otter.jvm.workbookapp.updater.install4j.ui.viewmodel.AppUpdaterViewModel
import tornadofx.*

class UpdaterView: View() {

    val vm: AppUpdaterViewModel by inject()

    init {
        vm.applyScheduledUpdate()
    }

    override val root = stackpane {

        borderpane {
            fitToParentSize()

            styleClass.add("app-drawer__section")

            top = label(messages["update"]) {
                styleClass.add("app-drawer__title")
            }

            center = stackpane {
                add<NoUpdatesAvailable>()
                add<UpdateWillCompleteLaterFragment>()
                add<UpdateCompleteFragment>()
                add<UpdateDownloadingFragment>()
                add<UpdateAvailableFragment>()
                add<CheckForUpdatesFragment>()

                style {
                    paddingTop = 20.0
                    paddingBottom = 10.0
                }
            }
        }
    }
}
