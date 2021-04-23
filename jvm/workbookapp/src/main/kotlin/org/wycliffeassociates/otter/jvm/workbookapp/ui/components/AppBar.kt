package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.button.AppBarButton
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer.AddFilesView
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer.DrawerEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer.DrawerEventAction
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer.InfoView
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer.SettingsView
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
                    onAction {
                        toggleOpen<AddFilesView>(isActiveProperty.value)
                    }
                    subscribe<DrawerEvent<UIComponent>> {
                        if (it.type == AddFilesView::class) {
                            isActiveProperty.set(it.action == DrawerEventAction.OPEN)
                        }
                    }
                }
            )

            add(
                AppBarButton().apply {
                    btnTextProperty.set(messages["exports"])
                    btnIconProperty.set(FontIcon(MaterialDesign.MDI_FOLDER))
                    isDisable = true
                }
            )

            add(
                AppBarButton().apply {
                    btnTextProperty.set(messages["settings"])
                    btnIconProperty.set(FontIcon(MaterialDesign.MDI_SETTINGS))
                    onAction {
                        toggleOpen<SettingsView>(isActiveProperty.value)
                    }
                    subscribe<DrawerEvent<UIComponent>> {
                        if (it.type == SettingsView::class) {
                            isActiveProperty.set(it.action == DrawerEventAction.OPEN)
                        }
                    }
                }
            )

            add(
                AppBarButton().apply {
                    addClass("app-bar__btn")
                    btnTextProperty.set(messages["info"])
                    btnIconProperty.set(FontIcon(MaterialDesign.MDI_INFORMATION))
                    onAction {
                        toggleOpen<InfoView>(isActiveProperty.value)
                    }
                    subscribe<DrawerEvent<UIComponent>> {
                        if (it.type == InfoView::class) {
                            isActiveProperty.set(it.action == DrawerEventAction.OPEN)
                        }
                    }
                }
            )
        }
    }

    private inline fun <reified T: UIComponent> toggleOpen(isActive: Boolean) {
        if (isActive) {
            fire(DrawerEvent(T::class, DrawerEventAction.CLOSE))
        } else {
            fire(DrawerEvent(T::class, DrawerEventAction.OPEN))
        }
    }
}
