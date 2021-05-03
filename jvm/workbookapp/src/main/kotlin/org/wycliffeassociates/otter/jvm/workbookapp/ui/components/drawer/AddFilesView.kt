package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer

import com.jfoenix.controls.JFXButton
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*

class AddFilesView : View() {
    override val root = vbox {
        addClass("app-drawer__content")

        scrollpane {
            addClass("app-drawer__scroll-pane")

            vbox {
                isFitToWidth = true
                isFitToHeight = true

                hbox {
                    label(messages["addFiles"]).apply {
                        addClass("app-drawer__title")
                    }
                    region { hgrow = Priority.ALWAYS }
                    add(
                        JFXButton().apply {
                            addClass("app-drawer__btn--close")
                            graphic = FontIcon(MaterialDesign.MDI_CLOSE)
                            action { collapse() }
                        }
                    )
                }
            }
        }
    }

    init {
        importStylesheet(javaClass.getResource("/css/app-drawer.css").toExternalForm())
    }

    private fun collapse() {
        fire(DrawerEvent(this::class, DrawerEventAction.CLOSE))
    }
}
