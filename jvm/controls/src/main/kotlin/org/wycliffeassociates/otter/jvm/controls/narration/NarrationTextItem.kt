/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.jvm.controls.event.PauseEvent
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

enum class NarrationTextItemState {
    BEGIN_RECORDING,
    RECORD,
    RECORDING_PAUSED,
    RECORD_DISABLED,
    RECORD_ACTIVE,
    RE_RECORD,
    RE_RECORD_ACTIVE,
    RE_RECORDING_PAUSED,
    RE_RECORD_DISABLED
}

class NarrationTextItem : VBox() {
    private val logger = LoggerFactory.getLogger(NarrationTextItem::class.java)

    val indexProperty = SimpleIntegerProperty(0)

    val stateProperty = SimpleObjectProperty(NarrationTextItemState.RECORD)
    val state by stateProperty

    val hasRecordingProperty = SimpleBooleanProperty(false)
    val verseLabelProperty = SimpleStringProperty()
    val verseTextProperty = SimpleStringProperty()
    val isRecordingProperty = SimpleBooleanProperty()
    val isRecording by isRecordingProperty

    val playingVerseIndexProperty = SimpleIntegerProperty()

    val isRecordingAgainProperty = SimpleBooleanProperty()
    val isRecordingAgain by isRecordingAgainProperty
    val isSelectedProperty = SimpleBooleanProperty(false)
    val isLastVerseProperty = SimpleBooleanProperty()

    val recordButtonTextProperty = SimpleStringProperty()
    val nextChunkTextProperty = SimpleStringProperty()

    val onBeginRecordingAction = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val onPauseRecordingAction = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val onResumeRecordingAction = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val onRecordActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val onRecordAgainActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val onSaveRecordingActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val onNextVerseActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val onPlayActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val onPauseActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    val isPlayingProperty = SimpleBooleanProperty(false)
    val isPlaying by isPlayingProperty

    init {
        styleClass.setAll("narration-list__verse-item")
        hbox {
            styleClass.setAll("narration-list__verse-item")
            borderpane {
                center = stackpane {
                    button {
                        addClass("btn", "btn--secondary")
                        graphic = FontIcon(MaterialDesign.MDI_PLAY)
                        disableWhen {
                            hasRecordingProperty.not()
                        }
                        disabledProperty().onChangeAndDoNow {
                            togglePseudoClass("inactive", it!!)
                        }
                        onActionProperty().bind(onPlayActionProperty)
                        visibleProperty().bind(isPlayingProperty.not())
                    }
                    button {
                        addClass("btn", "btn--secondary")
                        graphic = FontIcon(MaterialDesign.MDI_PAUSE)
                        disableWhen {
                            hasRecordingProperty.not()
                        }
                        disabledProperty().onChangeAndDoNow {
                            togglePseudoClass("inactive", it!!)
                        }
                        onActionProperty().bind(onPauseActionProperty)
                        visibleProperty().bind(isPlayingProperty.and(playingVerseIndexProperty.eq(indexProperty)))
                    }
                }
            }
            hbox {
                addClass("narration-list__verse-block")
                label(verseLabelProperty) {
                    minWidth = Region.USE_PREF_SIZE
                    addClass("narration-list__verse-item-text", "narration-list__verse-item-text__title")
                    translateY += 2.0
                }
                label(verseTextProperty) {
                    addClass("narration-list__verse-item-text")
                    isWrapText = true
                }
            }

            stackpane {
                alignment = Pos.CENTER
                hbox {
                    // BEGIN RECORDING
                    alignment = Pos.CENTER
                    narration_button {
                        alignment = Pos.CENTER
                        prefWidth = 316.0
                        styleClass.clear()
                        addClass("btn", "btn--primary")
                        text = FX.messages["record"]
                        graphic = FontIcon(MaterialDesign.MDI_MICROPHONE)
                        onActionProperty().bind(onBeginRecordingAction)
                    }
                    visibleProperty().bind(stateProperty.isEqualTo(NarrationTextItemState.BEGIN_RECORDING))
                }
                hbox {
                    // RECORD
                    alignment = Pos.CENTER
                    narration_button {
                        alignment = Pos.CENTER
                        prefWidth = 316.0
                        styleClass.clear()
                        addClass("btn", "btn--primary")
                        text = FX.messages["record"]
                        graphic = FontIcon(MaterialDesign.MDI_MICROPHONE)
                        onActionProperty().bind(onRecordActionProperty)
                    }
                    visibleProperty().bind(stateProperty.isEqualTo(NarrationTextItemState.RECORD))
                }
                hbox {
                    // RECORD_ACTIVE
                    alignment = Pos.CENTER
                    spacing = 16.0
                    narration_button {
                        prefWidth = 150.0
                        addClass("btn", "btn--secondary")
                        addPseudoClass("active")
                        text = FX.messages["pause"]
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
                                FX.messages["save"]

                            } else {
                                FX.messages["next"]
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
                    visibleProperty().bind(stateProperty.isEqualTo(NarrationTextItemState.RECORD_ACTIVE))
                }
                hbox {
                    // RECORDING PAUSED
                    alignment = Pos.CENTER
                    spacing = 16.0
                    narration_button {
                        prefWidth = 150.0
                        addClass("btn", "btn--primary")
                        text = FX.messages["resume"]
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
                                FX.messages["save"]

                            } else {
                                FX.messages["next"]
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
                    visibleProperty().bind(stateProperty.isEqualTo(NarrationTextItemState.RECORDING_PAUSED))
                }
                hbox {
                    // RECORD_DISABLED
                    alignment = Pos.CENTER
                    narration_button {
                        alignment = Pos.CENTER
                        prefWidth = 316.0
                        styleClass.clear()
                        addClass("btn", "btn--secondary")
                        addPseudoClass("inactive")
                        text = FX.messages["record"]
                        graphic = FontIcon(MaterialDesign.MDI_MICROPHONE)
                        setDisabled(true)
                    }
                    visibleProperty().bind(stateProperty.isEqualTo(NarrationTextItemState.RECORD_DISABLED))
                }
                hbox {
                    // RE_RECORD
                    alignment = Pos.CENTER
                    narration_button {
                        alignment = Pos.CENTER
                        prefWidth = 316.0
                        styleClass.clear()
                        addClass("btn", "btn--secondary")
                        text = FX.messages["reRecord"]
                        graphic = FontIcon(MaterialDesign.MDI_MICROPHONE)
                        onActionProperty().bind(onRecordAgainActionProperty)
                    }
                    visibleProperty().bind(stateProperty.isEqualTo(NarrationTextItemState.RE_RECORD))
                }
                hbox {
                    // RE_RECORD_DISABLED
                    alignment = Pos.CENTER
                    narration_button {
                        alignment = Pos.CENTER
                        prefWidth = 316.0
                        styleClass.clear()
                        addClass("btn", "btn--secondary")
                        addPseudoClass("inactive")
                        text = FX.messages["reRecord"]
                        graphic = FontIcon(MaterialDesign.MDI_MICROPHONE)
                        onActionProperty().bind(onRecordAgainActionProperty)
                    }
                    visibleProperty().bind(stateProperty.isEqualTo(NarrationTextItemState.RE_RECORD_DISABLED))
                }
                hbox {
                    // RE_RECORD_ACTIVE
                    alignment = Pos.CENTER
                    spacing = 16.0
                    narration_button {
                        addClass("btn", "btn--secondary")
                        addPseudoClass("active")
                        text = FX.messages["pause"]
                        graphic = FontIcon(MaterialDesign.MDI_PAUSE)
                        setOnAction {
                            onActionProperty().bind(onPauseRecordingAction)
                        }
                    }
                    narration_button {
                        prefWidth = 150.0
                        addClass("btn", "btn--primary")
                        text = FX.messages["save"]
                        graphic = FontIcon(MaterialDesign.MDI_CHECKBOX_MARKED_CIRCLE)
                        onActionProperty().bind(onSaveRecordingActionProperty)
                    }
                    visibleProperty().bind(stateProperty.isEqualTo(NarrationTextItemState.RE_RECORD_ACTIVE))
                }
                hbox {
                    // RE_RECORD_ACTIVE
                    alignment = Pos.CENTER
                    spacing = 16.0
                    narration_button {
                        addClass("btn", "btn--secondary")
                        text = FX.messages["resume"]
                        graphic = FontIcon(MaterialDesign.MDI_MICROPHONE)
                        setOnAction {
                            onActionProperty().bind(onResumeRecordingAction)
                        }
                    }
                    narration_button {
                        prefWidth = 150.0
                        addClass("btn", "btn--primary")
                        text = FX.messages["save"]
                        graphic = FontIcon(MaterialDesign.MDI_CHECKBOX_MARKED_CIRCLE)
                        onActionProperty().bind(onSaveRecordingActionProperty)
                    }
                    visibleProperty().bind(stateProperty.isEqualTo(NarrationTextItemState.RE_RECORDING_PAUSED))
                }
            }

//             disableProperty().bind(isSelectedProperty.not())
        }
    }

    private fun EventTarget.narration_button(
        text: String = "",
        graphic: Node? = null,
        op: Button.() -> Unit = {}
    ): Button {
        return Button(text).attachTo(this, op) {
            it.disableWhen {
                isPlayingProperty.or(
                    isRecordingProperty.and(
                        stateProperty.isNotEqualTo(NarrationTextItemState.RECORD_ACTIVE)
                            .and(
                                stateProperty.isNotEqualTo(NarrationTextItemState.RE_RECORD_ACTIVE)
                            ).and(
                                stateProperty.isNotEqualTo(NarrationTextItemState.RE_RECORDING_PAUSED)
                            ).and(
                                stateProperty.isNotEqualTo(NarrationTextItemState.RECORDING_PAUSED)
                            )
                    )
                )
            }
            if (graphic != null) it.graphic = graphic
        }
    }
}