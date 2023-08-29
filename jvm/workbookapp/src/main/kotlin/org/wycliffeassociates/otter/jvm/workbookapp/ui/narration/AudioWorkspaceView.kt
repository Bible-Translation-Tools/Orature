package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import javafx.beans.property.SimpleBooleanProperty
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import tornadofx.*

class AudioWorkspaceView : View() {
    private val viewModel: AudioWorkspaceViewModel by inject()

    override val root = hbox {
        stackpane {
            scrollpane {
                vbox {
                    hbox {
                        spacing = 10.0
                        paddingHorizontal = 10.0

                        bindChildren(viewModel.recordedVerses) { verse ->
                            val index = viewModel.recordedVerses.indexOf(verse)
                            val label = verse.label

                            menubutton(label) {
                                item("") {
                                    text = "Play"
                                    action {
                                        fire(PlayVerseEvent(verse))
                                    }
                                }
                                item("") {
                                    text = "Record Again"
                                    action {
                                        fire(RecordAgainEvent(index))
                                    }
                                }
                                item("") {
                                    text = "Open in..."
                                    action {
                                        fire(OpenInAudioPluginEvent(index))
                                    }
                                }
                            }
                        }
                    }
                    hbox {
                        button("Play All") {
                            action {
                                fire(PlayChapterEvent())
                            }
                        }
                        button("Pause") {
                            action {
                                fire(PauseEvent())
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDock() {
        super.onDock()
        viewModel.onDock()
    }

    override fun onUndock() {
        super.onUndock()
        viewModel.onUndock()
    }
}

class AudioWorkspaceViewModel : ViewModel() {
    private val logger = LoggerFactory.getLogger(AudioWorkspaceViewModel::class.java)

    private val narrationViewModel: NarrationViewModel by inject()

    val isRecordingProperty = SimpleBooleanProperty()
    var recordedVerses = observableListOf<VerseMarker>()

    fun onDock() {
        isRecordingProperty.bind(narrationViewModel.isRecordingProperty)
        recordedVerses.bind(narrationViewModel.recordedVerses) { it }
    }

    fun onUndock() {
        isRecordingProperty.unbind()
    }
}