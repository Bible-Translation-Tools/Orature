package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.menu

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import org.wycliffeassociates.otter.jvm.controls.event.OpenInAudioPluginEvent
import org.wycliffeassociates.otter.jvm.controls.event.PlayVerseEvent
import org.wycliffeassociates.otter.jvm.controls.event.RecordAgainEvent
import tornadofx.*
import tornadofx.FX.Companion.messages

class VerseMenu : ContextMenu() {
    val verseProperty = SimpleObjectProperty<AudioMarker>()
    val verseIndexProperty = SimpleIntegerProperty()
    val isRecordingProperty = SimpleBooleanProperty()

    init {
        addClass("wa-context-menu")

        val playOpt =
            MenuItem().apply {
                graphic =
                    label(messages["play"]) {
                        graphic = FontIcon(MaterialDesign.MDI_PLAY)
                        tooltip(text)
                    }
                action {
                    FX.eventbus.fire(PlayVerseEvent(verseProperty.value))
                }
                disableWhen {
                    isRecordingProperty
                }
            }
        val recordAgainOpt =
            MenuItem().apply {
                graphic =
                    label(messages["recordAgain"]) {
                        graphic = FontIcon(MaterialDesign.MDI_MICROPHONE)
                        tooltip(text)
                    }
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
        val editVerseOpt =
            MenuItem().apply {
                graphic =
                    label(messages["openIn"]) {
                        graphic = FontIcon(MaterialDesign.MDI_OPEN_IN_NEW)
                        tooltip(text)
                    }
                action {
                    FX.eventbus.fire(OpenInAudioPluginEvent(verseIndexProperty.value))
                }
                disableWhen {
                    isRecordingProperty
                }
            }

        items.setAll(playOpt, recordAgainOpt, editVerseOpt)
    }
}
