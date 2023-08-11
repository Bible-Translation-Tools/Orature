package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Slider
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.domain.narration.VerseNode
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.domain.content.PluginActions
import org.wycliffeassociates.otter.common.domain.narration.Narration
import org.wycliffeassociates.otter.common.domain.narration.NarrationFactory
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.utils.ListenerDisposer
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNowWithDisposer
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginClosedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginOpenedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.NextVerseEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.RecordVerseEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.menu.NarrationRedoEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.menu.NarrationResetChapterEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.menu.NarrationUndoEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.AudioPluginViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*
import java.io.File
import javax.inject.Inject

class AudioWorkspaceView : View() {
    private val viewModel: AudioWorkspaceViewModel by inject()

    override val root = hbox {
        stackpane {
            scrollpane {
                hbox {
                    spacing = 10.0
                    paddingHorizontal = 10.0

                    bindChildren(viewModel.recordedVerses) { verse ->
                        val index = viewModel.recordedVerses.indexOf(verse)
                        val label = (index + 1).toString()

                        menubutton(label) {
                            item("") {
                                text = "Play"
                                action {
                                    fire(PlayVerseEvent(verse))
                                }
                                disableWhen {
                                    viewModel.isRecordingProperty
                                }
                            }
                            item("") {
                                text = "Record Again"
                                action {
                                    fire(RecordAgainEvent(index))
                                }
                                disableWhen {
                                    viewModel.isRecordingProperty
                                }
                            }
                            item("") {
                                text = "Open in..."
                                action {
                                    fire(OpenInAudioPluginEvent(index))
                                }
                                disableWhen {
                                    viewModel.isRecordingProperty
                                }
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
    var recordedVerses = observableListOf<VerseNode>()

    fun onDock() {
        isRecordingProperty.bind(narrationViewModel.isRecordingProperty)
        recordedVerses.bind(narrationViewModel.recordedVerses) { it }
    }

    fun onUndock() {
        isRecordingProperty.unbind()
    }
}