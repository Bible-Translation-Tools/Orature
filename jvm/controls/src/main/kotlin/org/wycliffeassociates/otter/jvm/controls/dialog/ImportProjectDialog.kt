package org.wycliffeassociates.otter.jvm.controls.dialog

import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventHandler
import javafx.scene.input.DragEvent
import javafx.scene.input.TransferMode
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.OratureFileFormat
import org.wycliffeassociates.otter.jvm.controls.event.ProjectImportEvent
import tornadofx.*
import java.io.File

class ImportProjectDialog : OtterDialog() {
    private val logger = LoggerFactory.getLogger(javaClass)

    val validateFileCallback = SimpleObjectProperty<(List<File>) -> Boolean>()

    private val content = VBox().apply {
        addClass("confirm-dialog", "import-project-dialog")

        hbox {
            addClass("confirm-dialog__header")
            label(messages["import_projects"]) {
                addClass("h3")
            }
            region { hgrow = Priority.ALWAYS }
            button {
                addClass("btn", "btn--icon", "btn--borderless")
                graphic = FontIcon(MaterialDesign.MDI_CLOSE_CIRCLE)
                tooltip(messages["close"])

                action {
                    close()
                }
            }
        }

        vbox {
            addClass("confirm-dialog__body")
            vgrow = Priority.ALWAYS

            textflow {
                text(messages["dragAndDropDescription"]).apply {
                    addClass("app-drawer__text")
                }
                hyperlink("audio.bibleineverylanguage.org").apply {
                    addClass("wa-text--hyperlink", "app-drawer__text--link")
                    tooltip {
                        text = "audio.bibleineverylanguage.org/gl"
                    }
                    action {
                        hostServices.showDocument("https://audio.bibleineverylanguage.org/gl")
                    }
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
                                    messages["oratureFileTypes"],
                                    *OratureFileFormat.extensionList.map { "*.$it" }.toTypedArray()
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
        runLater { close() } // avoid ghost image after file dropped
        FX.eventbus.fire(ProjectImportEvent(file))
    }
}