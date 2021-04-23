package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.button.AppBarButton
import tornadofx.*

class AppBar : Fragment() {

    override val root = VBox()

    init {
        importStylesheet(javaClass.getResource("/css/app-bar.css").toExternalForm())

        root.apply {
            styleClass.setAll("app-bar")

            label {
                addClass("app-bar__logo")
                graphic = FontIcon(MaterialDesign.MDI_HEADSET)
            }

            region { vgrow = Priority.ALWAYS }

            add(
                AppBarButton().apply {
                    btnTextProperty.set(messages["add"])
                    btnIconProperty.set(FontIcon(MaterialDesign.MDI_PLUS))
                }
            )

            add(
                AppBarButton().apply {
                    btnTextProperty.set(messages["exports"])
                    btnIconProperty.set(FontIcon(MaterialDesign.MDI_FOLDER))
                }
            )

            add(
                AppBarButton().apply {
                    btnTextProperty.set(messages["settings"])
                    btnIconProperty.set(FontIcon(MaterialDesign.MDI_SETTINGS))
                }
            )

            add(
                AppBarButton().apply {
                    addClass("app-bar__btn")
                    btnTextProperty.set(messages["info"])
                    btnIconProperty.set(FontIcon(MaterialDesign.MDI_INFORMATION))
                }
            )
        }
    }
}
