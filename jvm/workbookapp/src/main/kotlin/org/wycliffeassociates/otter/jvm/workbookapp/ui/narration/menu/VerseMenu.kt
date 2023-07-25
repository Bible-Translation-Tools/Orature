package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.menu

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import javafx.scene.control.MenuButton
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.primitives.VerseNode
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.ImportVerseEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.OpenInAudioPluginEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.PlayVerseEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.RecordAgainEvent
import tornadofx.*
import tornadofx.FX.Companion.messages

class VerseMenu : MenuButton() {
    val playVerseTextProperty = SimpleStringProperty(messages["playVerse"])
    val recordAgainTextProperty = SimpleStringProperty(messages["recordAgain"])
    val importVerseTextProperty = SimpleStringProperty(messages["importVerse"])
    val editVerseTextProperty = SimpleStringProperty(messages["openIn"])

    val verseProperty = SimpleObjectProperty<VerseNode>()
    val verseIndexProperty = SimpleIntegerProperty()
    val isRecordingProperty = SimpleBooleanProperty()

    init {
        addClass("btn", "btn--primary", "btn--borderless", "wa-menu-button")
        graphic = FontIcon(MaterialDesign.MDI_DOTS_VERTICAL)
            addClass("wa-context-menu")

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
            item(importVerseTextProperty.value) {
                graphic = FontIcon(MaterialDesign.MDI_DOWNLOAD)
                action {
                    FX.eventbus.fire(ImportVerseEvent(verseProperty.value))
                }
                disableWhen {
                    isRecordingProperty
                }
            }
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