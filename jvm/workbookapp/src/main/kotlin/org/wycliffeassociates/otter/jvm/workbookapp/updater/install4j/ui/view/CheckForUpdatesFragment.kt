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

        visibleProperty().bind(vm.showCheckForUpdate)
        managedProperty().bind(visibleProperty())

        vbox {
            styleClass.addAll("app-drawer__version", "app-drawer__section")

            label(messages["updateFailedNoInternet"]) {
                styleClass.addAll("app-drawer__text", "app-drawer__text--error")
                visibleProperty().bind(vm.showOffline)
                managedProperty().bind(visibleProperty())
            }
        }

        add(
            JFXButton(messages["checkForUpdates"]).apply {
                styleClass.addAll("btn", "btn--secondary")
                tooltip(text)
                setOnAction {
                    vm.checkForUpdates()
                }
            }
        )
    }
}
