package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.translation

import com.github.thomasnield.rxkotlinfx.observeOnFx
import javafx.event.EventHandler
import javafx.scene.input.DragEvent
import javafx.scene.input.TransferMode
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.text.TextAlignment
import javafx.stage.FileChooser
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.OratureFileFormat
import org.wycliffeassociates.otter.jvm.controls.dialog.ProgressDialog
import org.wycliffeassociates.otter.jvm.controls.event.OpenChapterEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ImportProjectViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.TranslationViewModel2
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*
import java.io.File
import java.text.MessageFormat

class SourceAudioMissing : View() {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val viewModel: TranslationViewModel2 by inject()
    private val workbookDataStore: WorkbookDataStore by inject()
    private val settingsViewModel: SettingsViewModel by inject()
    private val importProjectViewModel: ImportProjectViewModel by inject()

    override val root = VBox().apply {
        addClass("audio-missing-view")
        vgrow = Priority.ALWAYS
        visibleWhen { viewModel.showAudioMissingViewProperty }
        managedWhen(visibleProperty())

        label {
            addClass("audio-missing__title")
            textProperty().bind(
                workbookDataStore.activeChapterTitleBinding().stringBinding {
                    MessageFormat.format(
                        messages["source_audio_missing_for"],
                        it
                    )
                }
            )
        }
        label(messages["source_audio_missing_description"]) {
            addClass("normal-text", "audio-missing__description")
        }

        vbox {
            addClass("audio-missing__drag-drop-area")
            onDragOver = onDragOverHandler()
            onDragDropped = onDragDroppedHandler()

            label {
                graphic = FontIcon(MaterialDesign.MDI_FOLDER_OUTLINE).addClass("big-icon")
            }

            textflow {
                addClass("audio-missing__text-flow")
                textAlignment = TextAlignment.CENTER

                val textMessage = messages["drag_drop_or_browse_import__template"]
                val prefixText = textMessage.substringBefore('{')
                val suffixText = textMessage.substringAfter('}')

                text(prefixText) {
                    addClass("normal-text", "centered")
                }
                hyperlink(messages["choose_file"]).apply {
                    addClass("wa-text--hyperlink", "audio-missing__link-text")
                    tooltip(text)
                    action {
                        chooseFile(
                            FX.messages["importResourceFromZip"],
                            arrayOf(
                                FileChooser.ExtensionFilter(
                                    messages["oratureFileTypes"],
                                    *OratureFileFormat.extensionList.map { "*.$it" }.toTypedArray()
                                )
                            ),
                            mode = FileChooserMode.Single,
                            owner = currentWindow
                        ).firstOrNull()?.let { handleImportFile(it) }
                    }
                }
                text(suffixText) {
                    addClass("normal-text", "centered")
                }
            }
        }

        stackpane {
            addClass("audio-missing__separator-area")
            separator { fitToParentWidth() }
        }

        label(messages["need_source_audio"]) {
            addClass("h3")
        }

        textflow {
            addClass("audio-missing__text-flow")

            val textMessage = messages["source_audio_download_description__template"]
            val prefixText = textMessage.substringBefore('{')
            val suffixText = textMessage.substringAfter('}')
            text(prefixText) {
                addClass("normal-text")
            }
            hyperlink("audio.bibleineverylanguage.org").apply {
                addClass("wa-text--hyperlink", "audio-missing__link-text")
                tooltip("audio.bibleineverylanguage.org/gl")
                action {
                    hostServices.showDocument("https://audio.bibleineverylanguage.org/gl")
                }
            }
            text(suffixText) {
                addClass("normal-text")
            }
        }

        hbox {
            addClass("audio-missing__actions")

            button(messages["check_online"]) {
                addClass("btn", "btn--primary")
                tooltip(text)
                graphic = FontIcon(MaterialDesign.MDI_EXPORT)
                prefWidthProperty().bind(this@hbox.widthProperty().divide(2))
                action {
                    hostServices.showDocument("https://audio.bibleineverylanguage.org/gl")
                }
            }

            button {
                addClass("btn", "btn--secondary")
                text = MessageFormat.format(
                    messages["begin_narrating_book"],
                    viewModel.bookTitleProperty.value
                )
                tooltip(text)
                graphic = FontIcon(MaterialDesign.MDI_ARROW_RIGHT)
                prefWidthProperty().bind(this@hbox.widthProperty().divide(2))

                action {
                    viewModel.goToNarration()
                }
            }
        }
    }

    private fun onDragOverHandler(): EventHandler<DragEvent> {
        return EventHandler {
            if (it.gestureSource != this && it.dragboard.hasFiles()) {
                it.acceptTransferModes(TransferMode.COPY)
            }
            it.consume()
        }
    }

    private fun onDragDroppedHandler(): EventHandler<DragEvent> {
        return EventHandler {
            var success = false
            if (it.dragboard.hasFiles()) {
                onDropFile(it.dragboard.files)
                success = true
            }
            it.isDropCompleted = success
            it.consume()
        }
    }

    private fun onDropFile(files: List<File>) {
        if (importProjectViewModel.isValidImportFile(files)) {
            val fileToImport = files.first()
            logger.info("Drag-drop import: $fileToImport")
            handleImportFile(fileToImport)
        }
    }

    private fun handleImportFile(file: File) {
        importProjectViewModel.setProjectInfo(file)

        val dialog = setupProgressDialog()

        importProjectViewModel.importProject(file)
            .observeOnFx()
            .doFinally {
                refresh()
                dialog.dialogTitleProperty.unbind()
                dialog.percentageProperty.set(0.0)
                dialog.close()
            }
            .subscribe { progressStatus ->
                progressStatus.percent?.let { percent ->
                    dialog.percentageProperty.set(percent)
                }
                if (progressStatus.titleKey != null && progressStatus.titleMessage != null) {
                    val message = MessageFormat.format(messages[progressStatus.titleKey!!], messages[progressStatus.titleMessage!!])
                    dialog.progressMessageProperty.set(message)
                } else if (progressStatus.titleKey != null) {
                    dialog.progressMessageProperty.set(messages[progressStatus.titleKey!!])
                }
            }
    }

    private fun setupProgressDialog() = find<ProgressDialog> {
        orientationProperty.set(settingsViewModel.orientationProperty.value)
        themeProperty.set(settingsViewModel.appColorMode.value)
        allowCloseProperty.set(false)
        cancelMessageProperty.set(null)
        dialogTitleProperty.bind(importProjectViewModel.importedProjectTitleProperty.stringBinding {
            it?.let {
                MessageFormat.format(
                    messages["importProjectTitle"],
                    messages["import"],
                    it
                )
            } ?: messages["importResource"]
        })

        setOnCloseAction { close() }

        open()
    }

    private fun refresh() {
        val chapter = workbookDataStore.chapter.sort
        workbookDataStore.activeChapterProperty.set(null)
        FX.eventbus.fire(OpenChapterEvent(chapter))
    }
}