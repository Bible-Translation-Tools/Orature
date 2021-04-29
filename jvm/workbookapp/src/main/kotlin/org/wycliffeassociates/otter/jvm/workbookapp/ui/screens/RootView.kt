package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginClosedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginOpenedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.OtterApp
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.AppBar
import org.wycliffeassociates.otter.jvm.workbookapp.ui.menu.view.MainMenu
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.RootViewModel
import tornadofx.*

class RootView : View() {

    private val viewModel: RootViewModel by inject()
    private val navigator: NavigationMediator by inject()

    val menu = MainMenu().apply {
        hiddenWhen(viewModel.pluginOpenedProperty)
        managedProperty().bind(visibleProperty())
    }

    init {
        // Configure the Workspace: sets up the window menu and external app open events

        // Plugins being opened should block the app from closing as this could result in a
        // loss of communication between the app and the external plugin, thus data loss
        workspace.subscribe<PluginOpenedEvent> {
            (app as OtterApp).shouldBlockWindowCloseRequest = true
            viewModel.pluginOpenedProperty.set(true)
        }
        workspace.subscribe<PluginClosedEvent> {
            (app as OtterApp).shouldBlockWindowCloseRequest = false
            viewModel.pluginOpenedProperty.set(false)
        }
        workspace.add(menu)
        workspace.header.removeFromParent()
        workspace.root.vgrow = Priority.ALWAYS
        navigator.dock<HomePage>()
    }

    override val root = stackpane {
        borderpane {
            top = menu
            left<AppBar>()
            center<AppContent>()
        }
    }
}

