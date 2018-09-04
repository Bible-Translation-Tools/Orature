package org.wycliffeassociates.otter.jvm.app.ui.home.View

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import org.wycliffeassociates.otter.jvm.app.ui.home.ViewModel.HomeViewModel
import tornadofx.*

class HomeView : View() {
    private val viewModel: HomeViewModel by inject()
    val projects = viewModel.projects

    override val root = borderpane {
        center {
            datagrid(viewModel.items)
        }
        bottom {
            hbox {
                alignment = Pos.BOTTOM_RIGHT
                padding = insets(15)
                this += button("", MaterialIconView(MaterialIcon.ADD)) {
                }
            }
        }
    }
}
