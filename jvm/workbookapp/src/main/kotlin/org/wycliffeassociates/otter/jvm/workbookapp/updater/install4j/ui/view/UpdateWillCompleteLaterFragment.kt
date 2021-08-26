package org.wycliffeassociates.otter.jvm.workbookapp.updater.install4j.ui.view

import org.wycliffeassociates.otter.jvm.workbookapp.updater.install4j.ui.viewmodel.AppUpdaterViewModel
import tornadofx.*

class UpdateWillCompleteLaterFragment : Fragment() {

    val vm: AppUpdaterViewModel by inject()

    override val root = vbox {

        fitToParentWidth()

        visibleProperty().bind(vm.showUpdateScheduled)
        managedProperty().bind(visibleProperty())

        styleClass.add("app-drawer__section")

        label(messages["updateSchedule"]) {
            styleClass.add("app-drawer__subtitle")
        }

        label(messages["updateWillCompleteLater"]) {
            styleClass.add("app-drawer__text")
        }
    }
}
