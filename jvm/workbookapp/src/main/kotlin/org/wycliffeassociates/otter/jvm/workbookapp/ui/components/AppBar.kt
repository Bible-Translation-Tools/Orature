package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.scene.control.ToggleGroup
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
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.RootViewModel
import tornadofx.*

class AppBar : Fragment() {

    private val rootViewModel: RootViewModel by inject()

    override val root = VBox()

    private val buttonsToggleGroup = ToggleGroup()

    private val addButton = AppBarButton().apply {
        textProperty().set(messages["add"])
        graphicProperty().set(FontIcon(MaterialDesign.MDI_PLUS))
        toggleGroup = buttonsToggleGroup
        selectedProperty().onChange {
            toggleOpen<AddFilesView>(it)
        }
        subscribe<DrawerEvent<UIComponent>> {
            if (it.type == AddFilesView::class) {
                selectedProperty().set(it.action == DrawerEventAction.OPEN)
            }
        }
    }

    private val settingsButton = AppBarButton().apply {
        textProperty().set(messages["settings"])
        graphicProperty().set(FontIcon(MaterialDesign.MDI_SETTINGS))
        toggleGroup = buttonsToggleGroup
        selectedProperty().onChange {
            toggleOpen<SettingsView>(it)
        }
        subscribe<DrawerEvent<UIComponent>> {
            if (it.type == SettingsView::class) {
                selectedProperty().set(it.action == DrawerEventAction.OPEN)
            }
        }
    }

    private val infoButton = AppBarButton().apply {
        textProperty().set(messages["info"])
        graphicProperty().set(FontIcon(MaterialDesign.MDI_INFORMATION))
        toggleGroup = buttonsToggleGroup
        selectedProperty().onChange {
            toggleOpen<InfoView>(it)
        }
        subscribe<DrawerEvent<UIComponent>> {
            if (it.type == InfoView::class) {
                selectedProperty().set(it.action == DrawerEventAction.OPEN)
            }
        }
    }

    init {
        importStylesheet(javaClass.getResource("/css/app-bar.css").toExternalForm())

        root.apply {
            styleClass.setAll("app-bar")

            disableProperty().bind(rootViewModel.pluginOpenedProperty)

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

    private inline fun <reified T: UIComponent> toggleOpen(isActive: Boolean) {
        when (isActive) {
            true -> fire(DrawerEvent(T::class, DrawerEventAction.OPEN))
            false -> fire(DrawerEvent(T::class, DrawerEventAction.CLOSE))
        }
    }
}
