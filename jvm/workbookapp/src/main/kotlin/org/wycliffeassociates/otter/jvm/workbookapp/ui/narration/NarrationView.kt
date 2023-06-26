package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import com.jfoenix.controls.JFXSnackbar
import com.jfoenix.controls.JFXSnackbarLayout
import io.reactivex.subjects.PublishSubject
import javafx.beans.property.SimpleBooleanProperty
import javafx.util.Duration
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.ColorTheme
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.workbookapp.SnackbarHandler
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.AudioPluginViewModel
import tornadofx.*
import java.util.*

class NarrationView : View() {
    private val logger = LoggerFactory.getLogger(NarrationView::class.java)

    private val viewModel: NarrationViewViewModel by inject()
    private val audioPluginViewModel: AudioPluginViewModel by inject()

    init {
        tryImportStylesheet(resources["/css/narration.css"])
        tryImportStylesheet(resources["/css/chapter-selector.css"])

        subscribe<SnackBarEvent> {
            viewModel.snackBarMessage(it.message)
        }
    }

    override val root = stackpane {
        addClass(ColorTheme.LIGHT.styleClass)

        borderpane {
            top<NarrationHeader>()
            center<NarrationBody>()
            bottom<NarrationFooter>()
        }

        createSnackBar()
    }

    private fun createSnackBar() {
        viewModel
            .snackBarObservable
            .doOnError { e ->
                logger.error("Error in creating no plugin snackbar", e)
            }
            .subscribe { pluginErrorMessage ->
                SnackbarHandler.enqueue(
                    JFXSnackbar.SnackbarEvent(
                        JFXSnackbarLayout(
                            pluginErrorMessage,
                            messages["addApp"].uppercase(Locale.getDefault())
                        ) {
                            audioPluginViewModel.addPlugin(record = true, edit = false)
                        },
                        Duration.millis(5000.0),
                        null
                    )
                )
            }
    }
}

class NarrationViewViewModel : ViewModel() {
    val recordStartProperty = SimpleBooleanProperty()
    val recordPauseProperty = SimpleBooleanProperty()
    val recordResumeProperty = SimpleBooleanProperty()
    val isRecordingProperty = SimpleBooleanProperty()
    val isRecordingAgainProperty = SimpleBooleanProperty()

    val hasUndoProperty = SimpleBooleanProperty()
    val hasRedoProperty = SimpleBooleanProperty()
    val hasVersesProperty = SimpleBooleanProperty()

    val snackBarObservable: PublishSubject<String> = PublishSubject.create()

    fun snackBarMessage(message: String) {
        snackBarObservable.onNext(message)
    }
}

class SnackBarEvent(val message: String) : FXEvent()