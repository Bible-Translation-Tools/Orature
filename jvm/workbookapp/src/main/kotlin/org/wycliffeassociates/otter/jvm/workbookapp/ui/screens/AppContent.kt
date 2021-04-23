package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Side
import javafx.util.Duration
import org.controlsfx.control.HiddenSidesPane
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer.DrawerEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer.DrawerEventAction
import tornadofx.*
import kotlin.reflect.KClass

class AppContent : View() {

    private val openDrawer = SimpleObjectProperty<KClass<UIComponent>>()

    override val root = HiddenSidesPane().apply {
        content = workspace.root

        triggerDistance = 0.0
        animationDelay = Duration.ZERO

        subscribe<DrawerEvent<UIComponent>> {
            pinnedSide = Side.LEFT

            when (it.action) {
                DrawerEventAction.OPEN -> {
                    if (openDrawer.value != null && openDrawer.value != it.type) {
                        // If there was an opened drawer, first close it
                        fire(DrawerEvent(openDrawer.value, DrawerEventAction.CLOSE))

                        // Wait until close animation ends then open new drawer
                        Thread {
                            Thread.sleep(animationDuration.toMillis().toLong())
                            Platform.runLater {
                                left = find(it.type).root
                                openDrawer.set(it.type)
                                show(Side.LEFT)
                            }
                        }.start()
                    } else {
                        left = find(it.type).root
                        openDrawer.set(it.type)
                        show(Side.LEFT)
                    }
                }
                DrawerEventAction.CLOSE -> {
                    hide()
                    openDrawer.set(null)
                }
            }
        }
    }
}
