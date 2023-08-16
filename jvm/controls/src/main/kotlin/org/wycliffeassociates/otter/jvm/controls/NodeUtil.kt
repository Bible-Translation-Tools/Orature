package org.wycliffeassociates.otter.jvm.controls

import javafx.geometry.Orientation
import javafx.scene.Parent
import javafx.scene.control.ScrollBar
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material.Material
import tornadofx.*

/**
 * Calls this method from a scrollable component such as a ScrollPane
 * to insert the custom graphic.
 */
fun Parent.customizeScrollbarSkin() {
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