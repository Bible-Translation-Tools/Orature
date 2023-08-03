package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import com.github.thomasnield.rxkotlinfx.toLazyBinding
import com.jfoenix.controls.JFXSnackbar
import com.jfoenix.controls.JFXSnackbarLayout
import io.reactivex.subjects.PublishSubject
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.util.Duration
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.ColorTheme
import org.wycliffeassociates.otter.jvm.controls.dialog.PluginOpenedPage
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.workbookapp.SnackbarHandler
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginClosedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginOpenedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.AudioPluginViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.PluginCloseFinishedEvent
import tornadofx.*
import java.util.*

class NarrationPage : View() {
    private val logger = LoggerFactory.getLogger(NarrationPage::class.java)

    private val viewModel: NarrationViewModel by inject()
    private val audioPluginViewModel: AudioPluginViewModel by inject()
    private val settingsViewModel: SettingsViewModel by inject()
    private val workbookDataStore: WorkbookDataStore by inject()

    private val pluginOpenedPage: PluginOpenedPage

    init {
        tryImportStylesheet(resources["/css/narration.css"])
        tryImportStylesheet(resources["/css/chapter-selector.css"])

        pluginOpenedPage = createPluginOpenedPage()

        workspace.subscribe<PluginOpenedEvent> { pluginInfo ->
            if (!pluginInfo.isNative) {
                workspace.dock(pluginOpenedPage)
                //viewModel.openSourcePlayer()
            }
        }
        workspace.subscribe<PluginClosedEvent> {
            (workspace.dockedComponentProperty.value as? PluginOpenedPage)?.let {
                workspace.navigateBack()
            }
        }
        workspace.subscribe<PluginCloseFinishedEvent> {
            workspace.navigateBack()
        }

        workspace.subscribe<SnackBarEvent> {
            viewModel.snackBarMessage(it.message)
        }
    }

    override val root = stackpane {
        addClass(ColorTheme.LIGHT.styleClass)

        createSnackBar()

        borderpane {
            top<NarrationHeader>()
            center<AudioWorkspaceView>()
            bottom<TeleprompterView>()
        }
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

    private fun createPluginOpenedPage(): PluginOpenedPage {
        // Plugin active cover
        return find<PluginOpenedPage>().apply {
            //dialogTitleProperty.bind(viewModel.dialogTitleBinding())
            //dialogTextProperty.bind(viewModel.dialogTextBinding())
            //playerProperty.bind(viewModel.sourceAudioPlayerProperty)
            // targetAudioPlayerProperty.bind(workbookDataStore.targetAudioProperty.objectBinding { it?.player })
            //audioAvailableProperty.bind(viewModel.sourceAudioAvailableProperty)
            licenseProperty.bind(workbookDataStore.sourceLicenseProperty)
            sourceTextProperty.bind(workbookDataStore.sourceTextBinding())
            sourceContentTitleProperty.bind(workbookDataStore.activeTitleBinding())
            orientationProperty.bind(settingsViewModel.orientationProperty)
            sourceOrientationProperty.bind(settingsViewModel.sourceOrientationProperty)

            sourceSpeedRateProperty.bind(
                workbookDataStore.activeWorkbookProperty.select {
                    it.translation.sourceRate.toLazyBinding()
                }
            )

            targetSpeedRateProperty.bind(
                workbookDataStore.activeWorkbookProperty.select {
                    it.translation.targetRate.toLazyBinding()
                }
            )
            /*sourceTextZoomRateProperty.bind(
                workbookDataStore.sourceTextZoomRateProperty
            )*/
        }
    }
}

class NarrationViewModel : ViewModel() {
    val recordStartProperty = SimpleBooleanProperty()
    val recordPauseProperty = SimpleBooleanProperty()
    val recordResumeProperty = SimpleBooleanProperty()
    val isRecordingProperty = SimpleBooleanProperty()
    val isRecordingAgainProperty = SimpleBooleanProperty()

    val hasUndoProperty = SimpleBooleanProperty()
    val hasRedoProperty = SimpleBooleanProperty()
    val hasVersesProperty = SimpleBooleanProperty()

    val lastRecordedVerseProperty = SimpleIntegerProperty()

    val snackBarObservable: PublishSubject<String> = PublishSubject.create()
    fun snackBarMessage(message: String) {
        snackBarObservable.onNext(message)
    }
}

class SnackBarEvent(val message: String) : FXEvent()