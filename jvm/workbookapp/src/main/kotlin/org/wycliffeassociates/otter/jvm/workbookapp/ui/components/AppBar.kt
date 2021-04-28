package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.scene.control.ToggleGroup
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.button.AppBarButton
import tornadofx.*

class AppBar : Fragment() {

    override val root = VBox()

    private val buttonsToggleGroup = ToggleGroup()

    private val addButton = AppBarButton().apply {
        textProperty().set(messages["add"])
        graphicProperty().set(FontIcon(MaterialDesign.MDI_PLUS))
        toggleGroup = buttonsToggleGroup
    }

    private val settingsButton = AppBarButton().apply {
        textProperty().set(messages["settings"])
        graphicProperty().set(FontIcon(MaterialDesign.MDI_SETTINGS))
        toggleGroup = buttonsToggleGroup
    }

    private val infoButton = AppBarButton().apply {
        textProperty().set(messages["info"])
        graphicProperty().set(FontIcon(MaterialDesign.MDI_INFORMATION))
        toggleGroup = buttonsToggleGroup
    }

    init {
        importStylesheet(javaClass.getResource("/css/app-bar.css").toExternalForm())

        root.apply {
            styleClass.setAll("app-bar")

            label {
                addClass("app-bar__logo")
                graphic = FontIcon(MaterialDesign.MDI_HEADSET)
            }

            region { vgrow = Priority.ALWAYS }

            add(addButton)
            add(settingsButton)
            add(infoButton)
        }
    }
}
