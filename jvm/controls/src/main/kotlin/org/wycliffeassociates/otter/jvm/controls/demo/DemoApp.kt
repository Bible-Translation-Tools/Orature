package org.wycliffeassociates.otter.jvm.controls.demo

import javafx.stage.Stage
import org.wycliffeassociates.otter.jvm.controls.demo.ui.screens.DemoView
import org.wycliffeassociates.otter.jvm.controls.demo.ui.screens.RootView
import tornadofx.App
import tornadofx.UIComponent

class DemoApp : App(RootView::class) {
    override fun start(stage: Stage) {
        super.start(stage)
        stage.isMaximized = true
    }

    override fun onBeforeShow(view: UIComponent) {
        workspace.dock<DemoView>()
    }
}