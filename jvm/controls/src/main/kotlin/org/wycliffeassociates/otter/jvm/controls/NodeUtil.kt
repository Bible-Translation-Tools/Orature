package org.wycliffeassociates.otter.jvm.controls

import javafx.application.Platform
import javafx.geometry.Orientation
import javafx.scene.Parent
import javafx.scene.control.ScrollBar
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material.Material
import tornadofx.*

fun Parent.customizeScrollbarSkin() {
    Platform.runLater {
        val scrollBars = lookupAll(".scroll-bar")
        scrollBars
            .mapNotNull { it as? ScrollBar }
            .forEach {
                val thumb = it.lookup(".thumb")
                thumb?.add(
                    FontIcon(Material.DRAG_INDICATOR).apply {
                        addClass("thumb-icon")
                        if (it.orientation == Orientation.HORIZONTAL) rotate = 90.0
                    }
                )
            }
    }
}