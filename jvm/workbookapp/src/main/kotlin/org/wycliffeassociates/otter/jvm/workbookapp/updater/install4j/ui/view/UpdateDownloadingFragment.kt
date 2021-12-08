package org.wycliffeassociates.otter.jvm.workbookapp.updater.install4j.ui.view

import com.jfoenix.controls.JFXProgressBar
import org.wycliffeassociates.otter.jvm.workbookapp.updater.install4j.ui.viewmodel.AppUpdaterViewModel
import tornadofx.*

class UpdateDownloadingFragment : Fragment() {

    val vm: AppUpdaterViewModel by inject()

    override val root = vbox {
        fitToParentSize()

        visibleProperty().bind(vm.showUpdateDownloading)
        managedProperty().bind(visibleProperty())

        styleClass.add("app-drawer__section")

        label(messages["updateDownloading"]).apply {
            styleClass.add("app-drawer__subtitle")
        }

        label {
            styleClass.add("app-drawer__text")
            textProperty().bind(vm.statusMessageProperty)
        }
        label {
            styleClass.add("app-drawer__text")
            textProperty().bind(vm.detailedMessageProperty)
        }

        add(
            JFXProgressBar().apply {
                prefWidth = 300.0
                progressProperty().bind(vm.percentCompleteProperty.divide(100.0))
            }
        )
    }
}
