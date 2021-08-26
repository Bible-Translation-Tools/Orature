package org.wycliffeassociates.otter.jvm.workbookapp.updater.install4j.ui.view

import org.wycliffeassociates.otter.jvm.workbookapp.updater.install4j.ui.viewmodel.AppUpdaterViewModel
import tornadofx.*

class NoUpdatesAvailable : Fragment() {

    val vm: AppUpdaterViewModel by inject()

    override val root = vbox {

        fitToParentWidth()

        visibleProperty().bind(vm.showNoUpdatesAvailable)
        managedProperty().bind(visibleProperty())

        styleClass.add("app-drawer__section")

        label(messages["versionUpToDate"]) {
            styleClass.add("app-drawer__subtitle")
        }

        label(messages["versionUpToDateLong"]) {
            styleClass.add("app-drawer__text")
        }
    }
}
