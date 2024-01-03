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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.translation

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.jfoenix.controls.JFXSnackbar
import javafx.event.EventHandler
import javafx.scene.input.DragEvent
import javafx.scene.input.TransferMode
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import javafx.util.Duration
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.OratureFileFormat
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.jvm.controls.dialog.ProgressDialog
import org.wycliffeassociates.otter.jvm.controls.event.NavigateChapterEvent
import org.wycliffeassociates.otter.jvm.controls.event.ProjectImportFinishEvent
import org.wycliffeassociates.otter.jvm.controls.model.NotificationStatusType
import org.wycliffeassociates.otter.jvm.controls.model.NotificationViewData
import org.wycliffeassociates.otter.jvm.controls.popup.NotificationSnackBar
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ImportProjectViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.NOTIFICATION_DURATION_SEC
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
            minHeight = Region.USE_PREF_SIZE // avoids ellipsis
        }

        vbox {
            addClass("audio-missing__drag-drop-area")

            label {
                graphic = FontIcon(MaterialDesign.MDI_FOLDER_OUTLINE).addClass("big-icon")
            }

            textflow {
                addClass("audio-missing__text-flow", "text-centered")

                val textMessage = messages["drag_drop_or_browse_import__template"]
                val prefixText = textMessage.substringBefore('{')
                val suffixText = textMessage.substringAfter('}')

                text(prefixText) {
                    addClass("normal-text")
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
                    addClass("normal-text")
                }
            }
            textflow {
                addClass("audio-missing__text-flow", "text-centered")

                val textMessage = messages["file_extension_supported"]
                val prefixText = textMessage.substringBefore('{')
                val suffixText = textMessage.substringAfter('}')

                text(prefixText) {
                    addClass("note-text")
                }
                text("orature") {
                    addClass("h5", "bold-text")
                }
                text(suffixText) {
                    addClass("note-text")
                }
            }

            setOnDragOver {
                if (it.dragboard.hasFiles()) {
                    togglePseudoClass("drag-over", true)
                }
                onDragOverHandler().handle(it)
            }
            setOnDragExited {
                togglePseudoClass("drag-over", false)
            }
            onDragDropped = onDragDroppedHandler()
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
                textProperty().bind(viewModel.bookTitleProperty.stringBinding {
                    it?.let { bookTitle ->
                        MessageFormat.format(
                            messages["begin_narrating_book"],
                            bookTitle
                        )
                    }
                })
                tooltip {
                    textProperty().bind(this@button.textProperty())
                }
                graphic = FontIcon(MaterialDesign.MDI_ARROW_RIGHT)
                prefWidthProperty().bind(this@hbox.widthProperty().divide(2))

                action {
                    viewModel.goToNarration()
                }
            }
        }
    }

    init {
        subscribe<ProjectImportFinishEvent> { event ->
            val notification = createImportNotification(event)
            showNotification(notification)
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
            if (importProjectViewModel.isSourceAudioProject(fileToImport)) {
                logger.info("Drag-drop import: $fileToImport")
                handleImportFile(fileToImport)
            } else {
                val notSourceNotification = NotificationViewData(
                    title = messages["importFailed"],
                    message = messages["importErrorNotSourceAudio"],
                    statusType = NotificationStatusType.FAILED
                )
                showNotification(notSourceNotification)
            }
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

    private fun createImportNotification(event: ProjectImportFinishEvent): NotificationViewData {
        if (event.result == ImportResult.FAILED) {
            return NotificationViewData(
                title = messages["importFailed"],
                message = MessageFormat.format(messages["importFailedMessage"], event.filePath),
                statusType = NotificationStatusType.FAILED
            )
        }

        // source import
        val messageBody = MessageFormat.format(
            messages["importSourceSuccessfulMessage"],
            event.language
        )
        return NotificationViewData(
            title = messages["importSuccessful"],
            message = messageBody,
            statusType = NotificationStatusType.SUCCESSFUL,
        )
    }

    private fun showNotification(notification: NotificationViewData) {
        val snackBar = JFXSnackbar(root)
        val graphic = NotificationSnackBar(notification).apply {
            setOnDismiss {
                snackBar.hide()
            }
        }

        snackBar.enqueue(
            JFXSnackbar.SnackbarEvent(
                graphic,
                Duration.seconds(NOTIFICATION_DURATION_SEC)
            )
        )
    }

    private fun refresh() {
        val chapter = workbookDataStore.chapter.sort
        workbookDataStore.activeChapterProperty.set(null)
        FX.eventbus.fire(NavigateChapterEvent(chapter))
    }
}