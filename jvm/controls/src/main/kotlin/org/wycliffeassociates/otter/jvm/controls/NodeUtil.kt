/**
 * Copyright (C) 2020-2024 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.controls

import javafx.beans.property.BooleanProperty
import javafx.beans.property.IntegerProperty
import javafx.geometry.Orientation
import javafx.scene.Node
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

/** The distance of scroll bar increment/decrement measured in audio frames */
const val SCROLL_INCREMENT_UNIT = 10_000.0
/** The distance of scroll bar jump measured in audio frames */
const val SCROLL_JUMP_UNIT = 100_000.0

/**
 * Constructs a custom horizontal scroll bar for the audio waveform.
 *
 * @param audioPositionProperty the frame position of the current playback
 * @param totalFramesProperty the total number of frames in the audio
 * @param isPlayingProperty binding to playback status
 * @param onScroll invoked when the user interacts with the scroll bar
 */
fun createAudioScrollBar(
    audioPositionProperty: IntegerProperty,
    totalFramesProperty: IntegerProperty,
    isPlayingProperty: BooleanProperty,
    onScroll: (Int) -> Unit = {}
): ScrollBar {
    return ScrollBar().apply {
        orientation = Orientation.HORIZONTAL
        disableWhen { isPlayingProperty }

        valueProperty().onChange { value ->
            if (!isPlayingProperty.value) {
                onScroll(value.toInt())
            }
        }
        valueProperty().bindBidirectional(audioPositionProperty) // sync when audio played
        maxProperty().bind(totalFramesProperty)

        unitIncrement = SCROLL_INCREMENT_UNIT
        blockIncrementProperty().bind(maxProperty().doubleBinding {
            it?.let { maxValue ->
                maxValue.toDouble() / 10
            } ?: SCROLL_JUMP_UNIT
        })

        runLater {
            customizeScrollbarSkin()
        }
    }
}

fun Node.toggleFontLanguage(languageSlug: String?) {
    toggleClass("ethiopic-font", languageSlug == "am")
    toggleClass("lao-font", languageSlug == "lo")
}

fun Node.toggleFontForText(text: String?) {
    if (text == null) return

    val amharicPattern = Regex("[\u1200-\u137F]+") // Define a regex pattern for Amharic Unicode characters
    val laoPattern = Regex("[\u0E80-\u0EFF]+") // Define a regex pattern for Lao Unicode characters

    toggleClass("ethiopic-font", text.contains(amharicPattern))
    toggleClass("lao-font", text.contains(laoPattern))
}