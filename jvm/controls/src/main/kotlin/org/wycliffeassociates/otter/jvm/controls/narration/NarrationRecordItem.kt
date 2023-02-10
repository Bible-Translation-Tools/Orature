package org.wycliffeassociates.otter.jvm.controls.narration

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.image.Image
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*

class NarrationRecordItem : VBox() {
    val verseLabelProperty = SimpleStringProperty()
    val waveformProperty = SimpleObjectProperty<Image>()

    val onPlayActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val onOpenAppActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val onRecordAgainActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    init {
        styleClass.setAll("narration-record__verse-item")

        hbox {
            addClass("narration-record__verse-controls")

            label {
                addClass("narration-record__verse-text")

                graphic = FontIcon(MaterialDesign.MDI_BOOKMARK_OUTLINE)
                textProperty().bind(verseLabelProperty)
            }
            region {
                hgrow = Priority.ALWAYS
            }
            button {
                addClass("btn", "btn--primary", "btn--borderless")
                graphic = FontIcon(MaterialDesign.MDI_PLAY)

                onActionProperty().bind(onPlayActionProperty)
            }
            menubutton {
                addClass("btn", "btn--primary", "btn--borderless", "wa-menu-button")
                graphic = FontIcon(MaterialDesign.MDI_DOTS_HORIZONTAL)

                item("Open In...") {
                    graphic = FontIcon(MaterialDesign.MDI_OPEN_IN_NEW)
                    onActionProperty().bind(onOpenAppActionProperty)
                }
                item("Record Again") {
                    graphic = FontIcon(MaterialDesign.MDI_MICROPHONE)
                    onActionProperty().bind(onRecordAgainActionProperty)
                }
            }
        }

        hbox {
            alignment = Pos.CENTER
            vgrow = Priority.ALWAYS

            hbox {
                addClass("narration-record__waveform")
                imageview(waveformProperty)
            }
        }
    }
}