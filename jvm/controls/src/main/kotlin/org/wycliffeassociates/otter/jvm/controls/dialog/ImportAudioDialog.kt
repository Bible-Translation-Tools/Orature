package org.wycliffeassociates.otter.jvm.controls.dialog

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.input.DragEvent
import javafx.scene.input.TransferMode
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.jvm.controls.event.ImportVerseAudioEvent
import tornadofx.*
import java.io.File

class ImportAudioDialog : OtterDialog() {
    private val logger = LoggerFactory.getLogger(javaClass)

    val validateFileCallback = SimpleObjectProperty<(List<File>) -> Boolean>()
    private val onCloseActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val verseIndexProperty = SimpleIntegerProperty()

    private val content = VBox().apply {
        addClass("confirm-dialog", "import-project-dialog")

        hbox {
            addClass("confirm-dialog__header")
            label(messages["importAudioFile"]) {
                addClass("h3")
            }
            region { hgrow = Priority.ALWAYS }
            button {
                addClass("btn", "btn--icon", "btn--borderless")
                graphic = FontIcon(MaterialDesign.MDI_CLOSE_CIRCLE)
                tooltip(messages["close"])

                onActionProperty().bind(onCloseActionProperty)
            }
        }

        vbox {
            addClass("confirm-dialog__body")
            vgrow = Priority.ALWAYS

            textflow {
                text(messages["dragToImport"]).apply {
                    addClass("app-drawer__text")
                }
            }
            vbox {
                addClass("app-drawer__drag-drop-area")

                vgrow = Priority.ALWAYS

                label {
                    addClass("app-drawer__drag-drop-area__icon")
                    graphic = FontIcon(MaterialDesign.MDI_FILE_MULTIPLE)
                }

                label(messages["dragToImport"]) {
                    fitToParentWidth()
                    addClass("app-drawer__text--centered")
                }

                button(messages["choose_file"]) {
                    addClass(
                        "btn",
                        "btn--primary"
                    )
                    tooltip {
                        textProperty().bind(this@button.textProperty())
                    }
                    graphic = FontIcon(MaterialDesign.MDI_OPEN_IN_NEW)
                    action {
                        chooseFile(
                            FX.messages["importResourceFromZip"],
                            arrayOf(
                                FileChooser.ExtensionFilter(
                                    messages["audioFileTypes"],
                                    enumValues<AudioFileFormat>().map { "*.$it" }
                                )
                            ),
                            mode = FileChooserMode.Single,
                            owner = currentWindow
                        ).firstOrNull()?.let { importFile(it) }
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
        }
    }

    init {
        setContent(content)
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
        if (validateFileCallback.value.invoke(files)) {
            val fileToImport = files.first()
            logger.info("Drag-drop import: $fileToImport")
            importFile(fileToImport)
        }
    }

    private fun importFile(file: File) {
        if (verseIndexProperty.value > -1) {
            FX.eventbus.fire(ImportVerseAudioEvent(verseIndexProperty.value, file))
            verseIndexProperty.set(-1)
        }

        runLater {
            onCloseActionProperty.value.handle(ActionEvent())
        }

    }


    fun setOnCloseAction(op: () -> Unit) {
        onCloseActionProperty.set(EventHandler { op() })
    }
}