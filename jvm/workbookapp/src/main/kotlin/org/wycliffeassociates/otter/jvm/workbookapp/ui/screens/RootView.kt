package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginClosedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginOpenedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.OtterApp
import org.wycliffeassociates.otter.jvm.workbookapp.ui.menu.view.MainMenu
import tornadofx.*

class RootView : View() {

    val navigator: NavigationMediator by inject()
    val pluginOpenedProperty = SimpleBooleanProperty(false)
    val menu = MainMenu().apply {
        hiddenWhen(pluginOpenedProperty)
        managedProperty().bind(visibleProperty())
    }

    init {
        // Configure the Workspace: sets up the window menu and external app open events

        // Plugins being opened should block the app from closing as this could result in a
        // loss of communication between the app and the external plugin, thus data loss
        workspace.subscribe<PluginOpenedEvent> {
            (app as OtterApp).shouldBlockWindowCloseRequest = true
            pluginOpenedProperty.set(true)
        }
        workspace.subscribe<PluginClosedEvent> {
            (app as OtterApp).shouldBlockWindowCloseRequest = false
            pluginOpenedProperty.set(false)
        }
        workspace.add(menu)
        workspace.header.removeFromParent()
        workspace.root.vgrow = Priority.ALWAYS
        navigator.dock<HomePage>()
    }

    override val root = stackpane {
        borderpane {
            top = menu
            center = vbox {
                borderpane {
                    vgrow = Priority.ALWAYS
                    top = navigator.breadCrumbsBar.apply {
                        disableWhen(pluginOpenedProperty)
                    }
                    center<Workspace>()
                }
            }
        }
    }
}
