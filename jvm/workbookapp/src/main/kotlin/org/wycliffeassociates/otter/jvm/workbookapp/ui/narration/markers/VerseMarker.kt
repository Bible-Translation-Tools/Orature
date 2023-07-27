package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.markers

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.layout.HBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.domain.narration.VerseNode
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.menu.VerseMenu
import tornadofx.*

private const val MARKER_AREA_WIDTH = 24.0
private const val MARKER_WIDTH = 2.0

class VerseMarker : HBox() {

    val verseProperty = SimpleObjectProperty<VerseNode>()
    val verseIndexProperty = SimpleIntegerProperty()
    val labelProperty = SimpleStringProperty()
    val positionProperty = SimpleIntegerProperty()

    val isRecordingProperty = SimpleBooleanProperty()

    init {
        addClass("verse-marker")

        translateXProperty().bind(positionProperty.minus(MARKER_AREA_WIDTH / 2))

        add(stackpane {
            addClass("verse-marker__drag-area")
            minWidth = MARKER_AREA_WIDTH
            maxWidth = MARKER_AREA_WIDTH

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

            hbox {
                addClass("verse-marker__label")

                label {
                    addClass("verse-marker__label-icon")
                    graphic = FontIcon(MaterialDesign.MDI_BOOKMARK_OUTLINE)
                }

                label(labelProperty) {
                    addClass("verse-marker__label-title")
                }
            }
        })

        add(VerseMenu().apply {
            isRecordingProperty.bind(this@VerseMarker.isRecordingProperty)
            verseProperty.bind(this@VerseMarker.verseProperty)
            verseIndexProperty.bind(this@VerseMarker.verseIndexProperty)
        })
    }
}