package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import org.wycliffeassociates.otter.jvm.workbookapp.ui.OtterApp
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginClosedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginOpenedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.DeprecatedNavBar
import org.wycliffeassociates.otter.jvm.workbookapp.ui.menu.view.MainMenu
import tornadofx.*

class RootView : View() {

    val menu = MainMenu()
    val nav = DeprecatedNavBar()

    init {
        // Configure the Workspace: sets up the window menu and external app open events

        // Plugins being opened should block the app from closing as this could result in a
        // loss of communication between the app and the external plugin, thus data loss
        workspace.subscribe<PluginOpenedEvent> {
            (app as OtterApp).shouldBlockWindowCloseRequest = true
            nav.root.visibleProperty().set(false)
            nav.root.managedProperty().set(false)
            menu.visibleProperty().set(false)
            menu.managedProperty().set(false)
        }
        workspace.subscribe<PluginClosedEvent> {
            (app as OtterApp).shouldBlockWindowCloseRequest = false
            nav.root.visibleProperty().set(true)
            nav.root.managedProperty().set(true)
            menu.visibleProperty().set(true)
            menu.managedProperty().set(true)
        }
        workspace.add(menu)
        workspace.header.removeFromParent()
        workspace.dock<HomePage>()
    }

    override val root = stackpane {
        borderpane {
            top = menu
            left = nav.root
            center<Workspace>()
        }
    }
}
