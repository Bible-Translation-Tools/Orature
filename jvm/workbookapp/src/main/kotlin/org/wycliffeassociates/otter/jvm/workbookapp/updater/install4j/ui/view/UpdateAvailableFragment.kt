package org.wycliffeassociates.otter.jvm.workbookapp.updater.install4j.ui.view

import org.wycliffeassociates.otter.jvm.workbookapp.updater.install4j.ui.viewmodel.AppUpdaterViewModel
import tornadofx.*

class UpdateAvailableFragment : Fragment() {

    val vm: AppUpdaterViewModel by inject()

    override val root = vbox {

        fitToParentWidth()

        visibleProperty().bind(vm.showUpdateAvailable)
        managedProperty().bind(visibleProperty())

        styleClass.add("app-drawer__section")

        label(messages["updateAvailable"]).apply {
            styleClass.add("app-drawer__subtitle")
        }

        hbox {
            styleClass.add("app-drawer__section")
            label(messages["updateVersion"]).apply {
                styleClass.add("app-drawer__text")
            }
            label {
                textProperty().bind(vm.updateVersion)
                styleClass.add("app-drawer__text")
            }
        }

        hbox {
            styleClass.add("app-drawer__section")
            label(messages["updateSize"]).apply {
                styleClass.add("app-drawer__text")
            }
            label {
                styleClass.add("app-drawer__text")
                textProperty().bind(vm.updateSize)
            }
        }

        label(messages["updateUrl"]).apply {
            styleClass.add("app-drawer__subtitle")
        }

        hyperlink {
            styleClass.add("wa-text--hyperlink")
            textProperty().bind(vm.updateUrlText)
            action {
                hostServices.showDocument(textProperty().value)
            }
        }

/** currently does not work on Windows/Linux **/
//        add(
//            JFXButton(messages["updateNow"]).apply {
//                styleClass.addAll("btn", "btn--secondary")
//                tooltip(text)
//                setOnAction {
//                    vm.downloadUpdate()
//                }
//            }
//        )
    }
}
