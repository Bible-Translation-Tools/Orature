package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.menu

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import javafx.scene.control.MenuButton
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.jvm.controls.event.OpenInAudioPluginEvent
import org.wycliffeassociates.otter.jvm.controls.event.PlayVerseEvent
import org.wycliffeassociates.otter.jvm.controls.event.RecordAgainEvent
import tornadofx.*
import tornadofx.FX.Companion.messages

class VerseMenu : MenuButton() {
    val playVerseTextProperty = SimpleStringProperty(messages["play"])
    val recordAgainTextProperty = SimpleStringProperty(messages["recordAgain"])
    val importVerseTextProperty = SimpleStringProperty(messages["importVerse"])
    val editVerseTextProperty = SimpleStringProperty(messages["openIn"])

    val verseProperty = SimpleObjectProperty<VerseMarker>()
    val verseIndexProperty = SimpleIntegerProperty()
    val isRecordingProperty = SimpleBooleanProperty()

    init {
        addClass("btn", "btn--secondary", "btn--borderless", "wa-menu-button", "wa-context-menu")
        graphic = FontIcon(MaterialDesign.MDI_DOTS_VERTICAL)

        item(playVerseTextProperty.value) {
            graphic = FontIcon(MaterialDesign.MDI_PLAY)
            action {
                FX.eventbus.fire(PlayVerseEvent(verseProperty.value))
            }
            disableWhen {
                isRecordingProperty
            }
        }
        item(recordAgainTextProperty.value) {
            graphic = FontIcon(MaterialDesign.MDI_MICROPHONE)
            action {
                FX.eventbus.fire(RecordAgainEvent(verseIndexProperty.value))
            }
            disableWhen {
                isRecordingProperty
            }
        }
//        item(importVerseTextProperty.value) {
//            graphic = FontIcon(MaterialDesign.MDI_DOWNLOAD)
//            action {
//                FX.eventbus.fire(ImportVerseEvent(verseProperty.value))
//            }
//            disableWhen {
//                isRecordingProperty
//            }
//        }
        item(editVerseTextProperty.value) {
            graphic = FontIcon(MaterialDesign.MDI_OPEN_IN_NEW)
            action {
                FX.eventbus.fire(OpenInAudioPluginEvent(verseIndexProperty.value))
            }
            disableWhen {
                isRecordingProperty
            }
        }
    }
}

fun EventTarget.verseMenu(op: VerseMenu.() -> Unit = {}) = VerseMenu().attachTo(this, op)