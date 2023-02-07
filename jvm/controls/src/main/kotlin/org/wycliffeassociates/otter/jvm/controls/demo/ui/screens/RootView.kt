package org.wycliffeassociates.otter.jvm.controls.demo.ui.screens

import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.common.data.ColorTheme
import org.wycliffeassociates.otter.jvm.controls.demo.ui.viewmodels.DemoViewModel
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import tornadofx.*

class RootView : View() {
    private val viewModel: DemoViewModel by inject()

    override val root = stackpane {
        prefWidth = 800.0
        prefHeight = 600.0

        borderpane {
            center<Workspace>()
        }
    }

    init {
        workspace.header.removeFromParent()
        workspace.root.vgrow = Priority.ALWAYS

        tryImportStylesheet(resources["/css/theme/light-theme.css"])
        tryImportStylesheet(resources["/css/theme/dark-theme.css"])
        tryImportStylesheet(resources["/css/control.css"])

        bindThemeClassToRoot()

        viewModel.updateTheme(ColorTheme.SYSTEM)
    }

    override fun onDock() {
        super.onDock()
        viewModel.bind()
    }

    private fun bindThemeClassToRoot() {
        viewModel.appColorMode.onChange {
            when (it) {
                ColorTheme.LIGHT -> {
                    root.addClass(ColorTheme.LIGHT.styleClass)
                    root.removeClass(ColorTheme.DARK.styleClass)
                }
                ColorTheme.DARK -> {
                    root.addClass(ColorTheme.DARK.styleClass)
                    root.removeClass(ColorTheme.LIGHT.styleClass)
                }
            }
        }
    }
}