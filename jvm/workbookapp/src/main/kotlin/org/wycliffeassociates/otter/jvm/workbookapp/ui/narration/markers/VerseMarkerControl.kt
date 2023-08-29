package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.markers

import javafx.beans.binding.BooleanBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.layout.BorderPane
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.menu.VerseMenu
import tornadofx.*


internal const val MARKER_AREA_WIDTH = 24.0
internal const val MARKER_WIDTH = 2.0

class VerseMarkerControl : BorderPane() {

    val verseProperty = SimpleObjectProperty<VerseMarker>()
    val verseIndexProperty = SimpleIntegerProperty()
    val labelProperty = SimpleStringProperty()
    val canBeMovedProperty: BooleanBinding = verseIndexProperty.greaterThan(0)
    val isRecordingProperty = SimpleBooleanProperty()

    val dragAreaProperty = SimpleObjectProperty<Node>()

    private val normalMarker = FontIcon(MaterialDesign.MDI_BOOKMARK)
    private val outlinedMarker = FontIcon(MaterialDesign.MDI_BOOKMARK_OUTLINE)
    private val markerIconProperty = SimpleObjectProperty(outlinedMarker)

    init {
        addClass("verse-marker")

        left = stackpane {
            addClass("verse-marker__drag-area")
            minWidth = MARKER_AREA_WIDTH
            maxWidth = MARKER_AREA_WIDTH

            dragAreaProperty.set(this)

            region {
                line {
                    addClass("verse-marker__line")

                    startXProperty().bind(this@region.layoutXProperty().plus(MARKER_AREA_WIDTH / 2))
                    startYProperty().bind(this@region.layoutYProperty())
                    endXProperty().bind(this@region.widthProperty().minus(MARKER_AREA_WIDTH / 2))
                    endYProperty().bind(this@region.heightProperty())
                    strokeWidth = MARKER_WIDTH
                }
            }

            label {
                addClass("verse-marker__icon")
                graphicProperty().bind(markerIconProperty)
            }

            setOnMouseEntered {
                if (canBeMovedProperty.value) {
                    cursor = Cursor.H_RESIZE
                    markerIconProperty.set(normalMarker)

                    addPseudoClass("movable")
                }
                it.consume()
            }

            setOnMouseExited {
                cursor = Cursor.DEFAULT
                markerIconProperty.set(outlinedMarker)

                removePseudoClass("movable")
                it.consume()
            }
        }

        center = label(labelProperty) {
            addClass("verse-marker__title")
            setAlignment(this, Pos.BOTTOM_LEFT)
        }

        var verseMenu = VerseMenu()
        right = verseMenu.apply {
            addClass("verse-marker__menu")
            setAlignment(this, Pos.BOTTOM_LEFT)

            isRecordingProperty.bind(this@VerseMarkerControl.isRecordingProperty)
            verseProperty.bind(this@VerseMarkerControl.verseProperty)
            verseIndexProperty.bind(this@VerseMarkerControl.verseIndexProperty)
        }
    }
}