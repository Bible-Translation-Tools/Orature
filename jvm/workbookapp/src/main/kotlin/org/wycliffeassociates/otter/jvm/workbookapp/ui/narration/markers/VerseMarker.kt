package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.markers

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.common.data.primitives.VerseNode
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.menu.VerseMenu
import tornadofx.*

class VerseMarker : HBox() {

    val verseProperty = SimpleObjectProperty<VerseNode>()
    val verseIndexProperty = SimpleIntegerProperty()
    val labelProperty = SimpleStringProperty()
    val positionProperty = SimpleIntegerProperty()

    val isRecordingProperty = SimpleBooleanProperty()

    init {
        hgrow = Priority.NEVER
        translateXProperty().bind(positionProperty)
        alignment = Pos.BOTTOM_LEFT

        add(region {
            prefWidth = 20.0
            style {
                backgroundColor += Color.RED
            }
        })

        add(label(labelProperty))

        add(VerseMenu().apply {
            isRecordingProperty.bind(this@VerseMarker.isRecordingProperty)
            verseProperty.bind(this@VerseMarker.verseProperty)
            verseIndexProperty.bind(this@VerseMarker.verseIndexProperty)
        })
    }
}