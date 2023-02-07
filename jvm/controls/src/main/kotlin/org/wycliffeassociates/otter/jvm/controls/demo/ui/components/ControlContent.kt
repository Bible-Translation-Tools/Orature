package org.wycliffeassociates.otter.jvm.controls.demo.ui.components

import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.jvm.controls.demo.ui.viewmodels.DemoViewModel
import tornadofx.*

class ControlContent : Workspace() {
    private val viewModel: DemoViewModel by inject()

    init {
        header.removeFromParent()
        root.vgrow = Priority.ALWAYS

        root.apply {
            addClass("demo__content")

            viewModel.shownFragment.onChange {
                it?.let {
                    dock(it)
                }
            }
        }
    }
}