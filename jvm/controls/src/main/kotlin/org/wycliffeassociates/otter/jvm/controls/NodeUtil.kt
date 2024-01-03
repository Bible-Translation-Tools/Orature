package org.wycliffeassociates.otter.jvm.controls

import javafx.beans.property.BooleanProperty
import javafx.beans.property.IntegerProperty
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
                        },
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
    onScroll: (Int) -> Unit = {},
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
        blockIncrementProperty().bind(
            maxProperty().doubleBinding {
                it?.let { maxValue ->
                    maxValue.toDouble() / 10
                } ?: SCROLL_JUMP_UNIT
            },
        )

        runLater {
            customizeScrollbarSkin()
        }
    }
}
