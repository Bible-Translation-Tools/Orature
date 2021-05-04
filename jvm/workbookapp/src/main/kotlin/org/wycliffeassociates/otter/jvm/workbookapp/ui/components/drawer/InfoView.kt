package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer

import com.jfoenix.controls.JFXButton
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.workbookapp.ui.system.AppInfo
import tornadofx.*

class InfoView : View() {
    val info = AppInfo()

    override val root = vbox {
        addClass("app-drawer__content")

        scrollpane {
            addClass("app-drawer__scroll-pane")

            vbox {
                isFitToWidth = true
                isFitToHeight = true

                addClass("app-drawer-container")

                hbox {
                    label(messages["information"]).apply {
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

                vbox {
                    addClass("app-drawer__section")
                    label(messages["aboutOtter"]).apply {
                        addClass("app-drawer__subtitle")
                    }

                    label(messages["aboutOtterDescription"]).apply {
                        fitToParentWidth()
                        addClass("app-drawer__text")
                    }
                }

                vbox {
                    addClass("app-drawer__section--filled")

                    label(messages["currentVersion"]).apply {
                        addClass("app-drawer__subtitle--small")
                    }
                    label(info.getVersion() ?: messages["na"]).apply {
                        addClass("app-drawer__text")
                    }
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
