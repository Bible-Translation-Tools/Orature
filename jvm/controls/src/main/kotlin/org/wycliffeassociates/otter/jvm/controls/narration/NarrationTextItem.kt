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
package org.wycliffeassociates.otter.jvm.controls.narration

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Tooltip
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.domain.narration.teleprompter.NarrationStateType
import org.wycliffeassociates.otter.common.domain.narration.teleprompter.TeleprompterItemState
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*
import tornadofx.FX.Companion.messages

class NarrationTextItem : VBox() {
    private val logger = LoggerFactory.getLogger(NarrationTextItem::class.java)

    val indexProperty = SimpleIntegerProperty(0)

    val verseStateProperty = SimpleObjectProperty(TeleprompterItemState.RECORD)
    val verseState by verseStateProperty
    val narrationStateProperty = SimpleObjectProperty<NarrationStateType>()

    val isPlayEnabledProperty = SimpleBooleanProperty()

    val verseLabelProperty = SimpleStringProperty()
    val verseTextProperty = SimpleStringProperty()

    val isHighlightedProperty = SimpleBooleanProperty()

    val isSelectedProperty = SimpleBooleanProperty(false)
    val isLastVerseProperty = SimpleBooleanProperty()
    val isLastIndexProperty = SimpleBooleanProperty()

    val nextChunkTextProperty = SimpleStringProperty()
    val licenseProperty = SimpleStringProperty()

    val onBeginRecordingAction = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val onPauseRecordingAction = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val onPauseRecordAgainAction = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val onResumeRecordingAction = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val onResumeRecordingAgainAction = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val onRecordActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val onRecordAgainActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val onSaveRecordingActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val onNextVerseActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val onPlayActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val onPauseActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    init {
        hbox {
            styleClass.setAll("narration-list__verse-item")
            borderpane {
                center = stackpane {
                    button {
                        addClass("btn", "btn--secondary")
                        graphic = FontIcon(MaterialDesign.MDI_PLAY)
                        tooltip(messages["play"])


                        enableWhen {
                            isPlayEnabledProperty
                        }

                        disabledProperty().onChangeAndDoNow {
                            togglePseudoClass("inactive", it!!)
                        }
                        onActionProperty().bind(onPlayActionProperty)


                        visibleProperty().bind(
                            verseStateProperty.isNotEqualTo(TeleprompterItemState.PLAYING)
                                .and(verseStateProperty.isNotEqualTo(TeleprompterItemState.PLAYING_WHILE_RECORDING_PAUSED))
                        )
                    }
                    button {
                        addClass("btn", "btn--secondary")
                        graphic = FontIcon(MaterialDesign.MDI_PAUSE)
                        tooltip(messages["pause"])
                        enableWhen {
                            verseStateProperty.isEqualTo(TeleprompterItemState.PLAYING)
                                .or(verseStateProperty.isEqualTo(TeleprompterItemState.PLAYING_WHILE_RECORDING_PAUSED))
                        }
                        disabledProperty().onChangeAndDoNow {
                            togglePseudoClass("inactive", it!!)
                        }
                        onActionProperty().bind(onPauseActionProperty)
                        visibleProperty().bind(
                            verseStateProperty.isEqualTo(TeleprompterItemState.PLAYING)
                                .or(verseStateProperty.isEqualTo(TeleprompterItemState.PLAYING_WHILE_RECORDING_PAUSED))
                        )
                    }
                }
            }
            hbox {
                addClass("narration-list__verse-block")
                label(verseLabelProperty) {
                    minWidth = Region.USE_PREF_SIZE
                    addClass("narration-list__verse-item-text", "narration-list__verse-item-text__title")
                    isHighlightedProperty.onChangeAndDoNow {
                        togglePseudoClass("highlighted", it == true)
                    }
                    translateY += 2.0
                }
                label(verseTextProperty) {
                    addClass("narration-list__verse-item-text")
                    isHighlightedProperty.onChangeAndDoNow {
                        togglePseudoClass("highlighted", it == true)
                    }
                    isWrapText = true
                }
            }

            stackpane {
                alignment = Pos.CENTER
                hbox {
                    // BEGIN RECORDING
                    alignment = Pos.TOP_CENTER
                    paddingTop = 10.0
                    narration_button {
                        alignment = Pos.CENTER
                        prefWidth = 316.0
                        styleClass.clear()
                        addClass("btn", "btn--primary")
                        text = messages["record"]
                        graphic = FontIcon(MaterialDesign.MDI_MICROPHONE)
                        onActionProperty().bind(onBeginRecordingAction)
                    }
                    visibleProperty().bind(verseStateProperty.isEqualTo(TeleprompterItemState.BEGIN_RECORDING))
                }
                hbox {
                    // RECORD
                    alignment = Pos.TOP_CENTER
                    paddingTop = 10.0
                    narration_button {
                        alignment = Pos.CENTER
                        prefWidth = 316.0
                        styleClass.clear()
                        addClass("btn", "btn--primary")
                        text = messages["record"]
                        graphic = FontIcon(MaterialDesign.MDI_MICROPHONE)
                        onActionProperty().bind(onRecordActionProperty)
                    }
                    visibleProperty().bind(verseStateProperty.isEqualTo(TeleprompterItemState.RECORD))
                }
                hbox {
                    // RECORD_ACTIVE
                    alignment = Pos.TOP_CENTER
                    paddingTop = 10.0
                    spacing = 16.0
                    narration_button {
                        prefWidth = 150.0
                        addClass("btn", "btn--secondary")
                        addPseudoClass("active")
                        text = messages["pause"]
                        graphic = FontIcon(MaterialDesign.MDI_PAUSE)
                        onActionProperty().bind(onPauseRecordingAction)
                    }
                    narration_button {
                        prefWidth = 150.0
                        addClass("btn", "btn--secondary")

                        onActionProperty().bind(
                            objectBinding(
                                isLastVerseProperty,
                                onSaveRecordingActionProperty,
                                onNextVerseActionProperty
                            ) {
                                if (isLastVerseProperty.value) {
                                    onSaveRecordingActionProperty.value
                                } else {
                                    onNextVerseActionProperty.value
                                }
                            }
                        )

                        textProperty().bind(stringBinding(isLastVerseProperty) {
                            if (isLastVerseProperty.value) {
                                messages["save"]

                            } else {
                                messages["next"]
                            }
                        })

                        graphicProperty().bind(objectBinding(isLastVerseProperty) {
                            if (isLastVerseProperty.value) {
                                FontIcon(MaterialDesign.MDI_CHECKBOX_MARKED_CIRCLE_OUTLINE)
                            } else {
                                FontIcon(MaterialDesign.MDI_ARROW_DOWN)
                            }
                        })
                    }
                    visibleProperty().bind(verseStateProperty.isEqualTo(TeleprompterItemState.RECORD_ACTIVE))
                }
                hbox {
                    // RECORDING PAUSED
                    alignment = Pos.TOP_CENTER
                    paddingTop = 10.0
                    spacing = 16.0
                    narration_button {
                        prefWidth = 150.0
                        addClass("btn", "btn--primary")
                        text = messages["resume"]
                        graphic = FontIcon(MaterialDesign.MDI_MICROPHONE)
                        onActionProperty().bind(onResumeRecordingAction)
                    }
                    narration_button {
                        prefWidth = 150.0
                        addClass("btn", "btn--secondary")

                        onActionProperty().bind(
                            objectBinding(
                                isLastVerseProperty,
                                onSaveRecordingActionProperty,
                                onNextVerseActionProperty
                            ) {
                                if (isLastVerseProperty.value) {
                                    onSaveRecordingActionProperty.value
                                } else {
                                    onNextVerseActionProperty.value
                                }
                            }
                        )

                        textProperty().bind(stringBinding(isLastVerseProperty) {
                            if (isLastVerseProperty.value) {
                                messages["save"]

                            } else {
                                messages["next"]
                            }
                        })

                        graphicProperty().bind(objectBinding(isLastVerseProperty) {
                            if (isLastVerseProperty.value) {
                                FontIcon(MaterialDesign.MDI_CHECKBOX_MARKED_CIRCLE_OUTLINE)
                            } else {
                                FontIcon(MaterialDesign.MDI_ARROW_DOWN)
                            }
                        })
                    }
                    visibleProperty().bind(verseStateProperty.isEqualTo(TeleprompterItemState.RECORDING_PAUSED))
                }

                hbox {
                    // PLAYING WHILE RECORDING PAUSED
                    alignment = Pos.CENTER
                    spacing = 16.0
                    narration_button {
                        prefWidth = 150.0
                        addClass("btn", "btn--primary")
                        addPseudoClass("inactive")
                        text = messages["resume"]
                        graphic = FontIcon(MaterialDesign.MDI_MICROPHONE)
                        onActionProperty().bind(onResumeRecordingAction)
                    }
                    narration_button {
                        prefWidth = 150.0
                        addClass("btn", "btn--secondary")
                        addPseudoClass("inactive")

                        onActionProperty().bind(
                            objectBinding(
                                isLastVerseProperty,
                                onSaveRecordingActionProperty,
                                onNextVerseActionProperty
                            ) {
                                if (isLastVerseProperty.value) {
                                    onSaveRecordingActionProperty.value
                                } else {
                                    onNextVerseActionProperty.value
                                }
                            }
                        )

                        textProperty().bind(stringBinding(isLastVerseProperty) {
                            if (isLastVerseProperty.value) {
                                messages["save"]

                            } else {
                                messages["next"]
                            }
                        })

                        graphicProperty().bind(objectBinding(isLastVerseProperty) {
                            if (isLastVerseProperty.value) {
                                FontIcon(MaterialDesign.MDI_CHECKBOX_MARKED_CIRCLE_OUTLINE)
                            } else {
                                FontIcon(MaterialDesign.MDI_ARROW_DOWN)
                            }
                        })
                    }
                    visibleProperty().bind(verseStateProperty.isEqualTo(TeleprompterItemState.PLAYING_WHILE_RECORDING_PAUSED))
                }



                hbox {
                    // RECORD_DISABLED
                    alignment = Pos.TOP_CENTER
                    paddingTop = 10.0
                    narration_button {
                        alignment = Pos.CENTER
                        prefWidth = 316.0
                        styleClass.clear()
                        addClass("btn", "btn--secondary")
                        addPseudoClass("inactive")
                        text = messages["record"]
                        graphic = FontIcon(MaterialDesign.MDI_MICROPHONE)
                    }
                    visibleProperty().bind(verseStateProperty.isEqualTo(TeleprompterItemState.RECORD_DISABLED))
                }
                hbox {
                    // RECORD_AGAIN
                    alignment = Pos.TOP_CENTER
                    paddingTop = 10.0
                    narration_button {
                        alignment = Pos.CENTER
                        prefWidth = 316.0
                        styleClass.clear()
                        addClass("btn", "btn--secondary")
                        text = messages["reRecord"]
                        graphic = FontIcon(MaterialDesign.MDI_MICROPHONE)
                        onActionProperty().bind(onRecordAgainActionProperty)
                    }
                    visibleProperty().bind(verseStateProperty.isEqualTo(TeleprompterItemState.RECORD_AGAIN))
                }
                hbox {
                    // RECORD_AGAIN_DISABLED
                    alignment = Pos.TOP_CENTER
                    paddingTop = 10.0
                    narration_button {
                        alignment = Pos.CENTER
                        prefWidth = 316.0
                        styleClass.clear()
                        addClass("btn", "btn--secondary")
                        addPseudoClass("inactive")
                        text = messages["reRecord"]
                        graphic = FontIcon(MaterialDesign.MDI_MICROPHONE)
                        onActionProperty().bind(onRecordAgainActionProperty)
                    }
                    visibleProperty().bind(verseStateProperty.isEqualTo(TeleprompterItemState.RECORD_AGAIN_DISABLED))
                }

                hbox {
                    // PLAYING
                    alignment = Pos.CENTER
                    narration_button {
                        alignment = Pos.CENTER
                        prefWidth = 316.0
                        styleClass.clear()
                        addClass("btn", "btn--secondary")
                        addPseudoClass("inactive")
                        text = messages["reRecord"]
                        graphic = FontIcon(MaterialDesign.MDI_MICROPHONE)
                        onActionProperty().bind(onRecordAgainActionProperty)
                    }
                    visibleProperty().bind(verseStateProperty.isEqualTo(TeleprompterItemState.PLAYING))
                }


                hbox {
                    // RECORD_AGAIN_ACTIVE
                    alignment = Pos.TOP_CENTER
                    paddingTop = 10.0
                    spacing = 16.0
                    narration_button {
                        addClass("btn", "btn--secondary")
                        addPseudoClass("active")
                        text = messages["pause"]
                        graphic = FontIcon(MaterialDesign.MDI_PAUSE)
                        onActionProperty().bind(onPauseRecordAgainAction)
                    }
                    narration_button {
                        prefWidth = 150.0
                        addClass("btn", "btn--primary")
                        text = messages["save"]
                        graphic = FontIcon(MaterialDesign.MDI_CHECKBOX_MARKED_CIRCLE)
                        onActionProperty().bind(onSaveRecordingActionProperty)
                    }
                    visibleProperty().bind(verseStateProperty.isEqualTo(TeleprompterItemState.RECORD_AGAIN_ACTIVE))
                }
                hbox {
                    // RECORD_AGAIN_PAUSED
                    alignment = Pos.TOP_CENTER
                    paddingTop = 10.0
                    spacing = 16.0
                    narration_button {
                        addClass("btn", "btn--secondary")
                        text = messages["resume"]
                        graphic = FontIcon(MaterialDesign.MDI_MICROPHONE)
                        onActionProperty().bind(onResumeRecordingAgainAction)
                    }
                    narration_button {
                        prefWidth = 150.0
                        addClass("btn", "btn--primary")
                        text = messages["save"]
                        graphic = FontIcon(MaterialDesign.MDI_CHECKBOX_MARKED_CIRCLE)
                        onActionProperty().bind(onSaveRecordingActionProperty)
                    }

                    visibleProperty().bind(verseStateProperty.isEqualTo(TeleprompterItemState.RECORD_AGAIN_PAUSED))
                }
            }
        }
        hbox {
            alignment = Pos.CENTER
            vgrow = Priority.ALWAYS
            label(licenseProperty) {
                visibleWhen { isLastIndexProperty }
                managedWhen { visibleProperty() }
                addClass("narration__license-text")
                tooltip(text)
            }
        }
    }

    private fun EventTarget.narration_button(
        text: String = "",
        graphic: Node? = null,
        op: Button.() -> Unit = {}
    ): Button {
        return Button(text).attachTo(this, op) { btn ->
            val recordingStates = listOf(
                TeleprompterItemState.RECORD_ACTIVE,
                TeleprompterItemState.RECORD_AGAIN_ACTIVE,
                TeleprompterItemState.RECORD_AGAIN_PAUSED,
                TeleprompterItemState.RECORDING_PAUSED,
            )
            btn.disableWhen {
                booleanBinding(verseStateProperty, narrationStateProperty) {

                    val isPlaying = narrationStateProperty.value == NarrationStateType.PLAYING
                    val isRecording = narrationStateProperty.value == NarrationStateType.RECORDING
                    val differentItemRecording = isRecording && verseState !in recordingStates
                    when {
                        isPlaying -> true
                        differentItemRecording -> true
                        verseStateProperty.value == TeleprompterItemState.RECORD_DISABLED -> true
                        verseStateProperty.value == TeleprompterItemState.RECORD_AGAIN_DISABLED -> true
                        else -> false
                    }
                }
            }
            if (graphic != null) btn.graphic = graphic
            btn.tooltip = Tooltip().apply {
                addClass("tooltip-text")
                textProperty().bind(btn.textProperty())
            }
        }
    }
}