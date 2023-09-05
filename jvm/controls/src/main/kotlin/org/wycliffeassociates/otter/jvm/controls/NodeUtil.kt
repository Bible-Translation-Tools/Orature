package org.wycliffeassociates.otter.jvm.controls

import javafx.geometry.Orientation
import javafx.scene.Parent
import javafx.scene.control.ScrollBar
import javafx.scene.layout.StackPane
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material.Material
import tornadofx.*

/**
 * Calls this method from a scrollable component such as a ScrollPane
 * to insert the custom graphic.
 *
 * This may need to execute within a Platform.runLater() block to avoid
 * premature search for the component that not yet exists.
 */
fun Parent.customizeScrollbarSkin() {
    val scrollBars = lookupAll(".scroll-bar")
    scrollBars
        .mapNotNull { it as? ScrollBar }
        .forEach { bar ->
            val thumb = bar.lookup(".thumb")
            (thumb as? StackPane)?.let { t ->
                if (t.children.size == 0) {
                    t.add(
                        FontIcon(Material.DRAG_INDICATOR).apply {
                            addClass("thumb-icon")
                            if (bar.orientation == Orientation.HORIZONTAL) rotate = 90.0
                        }
                    )
                }
            }
        }
}