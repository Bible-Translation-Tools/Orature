package org.wycliffeassociates.otter.jvm.workbookapp.updater.install4j.ui.view

import com.jfoenix.controls.JFXButton
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.jvm.workbookapp.updater.install4j.ui.viewmodel.AppUpdaterViewModel
import org.kordamp.ikonli.javafx.FontIcon
import tornadofx.*

class UpdateCompleteFragment : Fragment() {

    private val restartNowIcon = FontIcon("mdi-power")
    private val restartLaterIcon = FontIcon("mdi-calendar-clock")

    private val vm: AppUpdaterViewModel by inject()

    override val root = vbox {
        fitToParentWidth()

        styleClass.add("app-drawer__section")

        visibleProperty().bind(vm.showUpdateCompleted)
        managedProperty().bind(visibleProperty())

        label(messages["downloadComplete"]) {
            styleClass.add("app-drawer__subtitle")
        }

        label(messages["updateNeedsRestart"]) {
            styleClass.add("app-drawer__text")
        }

        add(
            JFXButton(messages["restartLater"]).apply {
                wrapTextProperty().set(true)
                graphic = restartLaterIcon
                styleClass.addAll("btn", "btn--secondary")
                tooltip(text)
                setOnAction {
                    vm.updateLater()
                }
            }
        )
        add(
            JFXButton(messages["restartNow"]).apply {
                wrapTextProperty().set(true)
                graphic = restartNowIcon
                styleClass.addAll("btn", "btn--secondary")
                tooltip(text)
                setOnAction {
                    vm.updateAndRestart()
                }
            }
        )
    }
}
