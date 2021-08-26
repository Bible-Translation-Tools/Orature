package org.wycliffeassociates.otter.jvm.workbookapp.updater.install4j.ui.view

import com.jfoenix.controls.JFXButton
import org.wycliffeassociates.otter.jvm.workbookapp.ui.system.AppInfo
import org.wycliffeassociates.otter.jvm.workbookapp.updater.install4j.ui.viewmodel.AppUpdaterViewModel
import tornadofx.*

class CheckForUpdatesFragment : Fragment() {

    val vm: AppUpdaterViewModel by inject()
    val info = AppInfo()

    override val root = vbox {
        fitToParentSize()

        styleClass.add("app-drawer__section")

        visibleProperty().bind(vm.showCheckForUpdate)
        managedProperty().bind(visibleProperty())

        vbox {
            styleClass.addAll("app-drawer__version", "app-drawer__section")
            label(messages["currentVersion"]) {
                styleClass.add("app-drawer__subtitle")
            }
            label("${info.getVersion()}") {
                styleClass.add("app-drawer__text")
            }
        }

        add(
            JFXButton(messages["checkForUpdates"]).apply {
                styleClass.addAll("btn", "btn--secondary")
                setOnAction {
                    vm.checkForUpdates()
                }
            }
        )
    }
}
