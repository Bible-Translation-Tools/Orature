package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import javafx.application.Platform
import javafx.geometry.Side
import javafx.util.Duration
import org.controlsfx.control.HiddenSidesPane
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer.DrawerEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer.DrawerEventAction
import tornadofx.*

class AppContent : View() {

    override val root = HiddenSidesPane().apply {
        content = workspace.root

        triggerDistance = 0.0
        animationDelay = Duration.ZERO

        subscribe<DrawerEvent<UIComponent>> {
            pinnedSide = Side.LEFT

            when (it.action) {
                DrawerEventAction.OPEN -> {
                    // Wait until animation (if any) ends then open a drawer
                    Thread {
                        Thread.sleep(animationDuration.toMillis().toLong())
                        Platform.runLater {
                            left = find(it.type).root
                            show(Side.LEFT)
                        }
                    }.start()
                }
                DrawerEventAction.CLOSE -> {
                    hide()
                }
            }
        }
    }
}
