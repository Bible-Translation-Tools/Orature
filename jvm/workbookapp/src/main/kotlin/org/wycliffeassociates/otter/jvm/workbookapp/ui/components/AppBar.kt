package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.button.AppBarButton
import tornadofx.*

class AppBar : Fragment() {

    override val root = VBox()

    private val addButton = AppBarButton().apply {
        btnTextProperty.set(messages["add"])
        btnIconProperty.set(FontIcon(MaterialDesign.MDI_PLUS))
    }

    private val settingsButton = AppBarButton().apply {
        btnTextProperty.set(messages["settings"])
        btnIconProperty.set(FontIcon(MaterialDesign.MDI_SETTINGS))
    }

    private val infoButton = AppBarButton().apply {
        btnTextProperty.set(messages["info"])
        btnIconProperty.set(FontIcon(MaterialDesign.MDI_INFORMATION))
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
