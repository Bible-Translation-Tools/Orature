package org.wycliffeassociates.otter.jvm.controls.narration

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*

class NarrationVerseItem : VBox() {
    val verseLabelProperty = SimpleStringProperty()
    val verseTextProperty = SimpleStringProperty()
    val isActiveProperty = SimpleBooleanProperty()
    val isLastVerseProperty = SimpleBooleanProperty()

    private val onRecordActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    private val onNextVerseActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    init {
        styleClass.setAll("narration-list__verse-item")

        hbox {
            addClass("narration-list__verse-block")
            label(verseLabelProperty) {
                addClass("narration-list__verse-item-text", "narration-list__verse-item-text__title")
                translateY -= 5.0
            }
            label(verseTextProperty).apply {
                addClass("narration-list__verse-item-text")
                isWrapText = true

                prefWidthProperty().bind(this@NarrationVerseItem.maxWidthProperty().subtract(50))
            }

        }
        separator {
            addClass("narration-list__separator")
            visibleProperty().bind(isActiveProperty)
            managedProperty().bind(visibleProperty())
        }
        hbox {
            addClass("narration-list__buttons")
            alignment = Pos.BASELINE_LEFT

            button("Begin Recording") {
                addClass("btn", "btn--primary")
                graphic = FontIcon(MaterialDesign.MDI_MICROPHONE)

                onActionProperty().bind(onRecordActionProperty)
            }
            button("Next Verse") {
                addClass("btn", "btn--secondary")
                graphic = FontIcon(MaterialDesign.MDI_ARROW_DOWN)

                onActionProperty().bind(onNextVerseActionProperty)
                disableProperty().bind(isLastVerseProperty)
            }

            visibleProperty().bind(isActiveProperty)
            managedProperty().bind(visibleProperty())
        }

        disableProperty().bind(isActiveProperty.not())
    }

    fun setOnRecord(op: () -> Unit) {
        onRecordActionProperty.set(EventHandler {
            op.invoke()
        })
    }

    fun setOnNextVerse(op: () -> Unit) {
        onNextVerseActionProperty.set(
            EventHandler {
                op.invoke()
            }
        )
    }
}