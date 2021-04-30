package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXSnackbar
import com.jfoenix.controls.JFXSnackbarLayout
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.input.DragEvent
import javafx.scene.input.TransferMode
import javafx.scene.layout.Priority
import javafx.util.Duration
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.jvm.controls.dialog.progressdialog
import org.wycliffeassociates.otter.jvm.workbookapp.SnackbarHandler
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.AddFilesViewModel
import tornadofx.*

class AddFilesView : View() {
    private val logger = LoggerFactory.getLogger(AddFilesView::class.java)

    private val viewModel: AddFilesViewModel by inject()

    override val root = vbox {
        addClass("app-drawer__content")

        scrollpane {
            addClass("app-drawer__scroll-pane")
            fitToParentHeight()

            vbox {
                isFitToWidth = true
                isFitToHeight = true

                addClass("app-drawer-container")

                hbox {
                    label(messages["addFiles"]).apply {
                        addClass("app-drawer__title")
                    }
                    region { hgrow = Priority.ALWAYS }
                    add(
                        JFXButton().apply {
                            addClass("app-drawer__btn--close")
                            graphic = FontIcon(MaterialDesign.MDI_CLOSE)
                            action { collapse() }
                        }
                    )
                }

                vbox {
                    addClass("app-drawer__section")
                    label(messages["dragAndDrop"]).apply {
                        addClass("app-drawer__subtitle")
                    }

                    textflow {
                        text(messages["dragAndDropDescription"]).apply {
                            addClass("app-drawer__text")
                        }
                        hyperlink("audio.bibleineverylanguage.org").apply {
                            addClass("app-drawer__text--link")
                            action {
                                hostServices.showDocument("https://audio.bibleineverylanguage.org/")
                            }
                        }
                    }
                }

                vbox {
                    addClass("app-drawer__drag-drop-area")

                    vgrow = Priority.ALWAYS

                    label {
                        addClass("app-drawer__drag-drop-area__icon")
                        graphic = FontIcon(MaterialDesign.MDI_LINK_OFF)
                    }

                    label(messages["dragToImport"]) {
                        fitToParentWidth()
                        addClass("app-drawer__text--centered")
                    }

                    button(messages["browseFiles"]) {
                        addClass(
                            "btn",
                            "btn--primary"
                        )
                        graphic = FontIcon(MaterialDesign.MDI_OPEN_IN_NEW)
                        action {
                            viewModel.onChooseFile()
                        }
                    }

                    onDragOver = onDragOverHandler()
                    onDragDropped = onDragDroppedHandler()
                }
            }
        }
    }

    init {
        importStylesheet(javaClass.getResource("/css/app-drawer.css").toExternalForm())

        initImportDialog()
        createSnackBar()
    }

    private fun initImportDialog() {
        val importDialog = progressdialog {
            text = messages["importResource"]
            graphic = FontIcon("mdi-import")
        }
        viewModel.showImportDialogProperty.onChange {
            Platform.runLater { if (it) importDialog.open() else importDialog.close() }
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
                viewModel.onDropFile(it.dragboard.files)
                success = true
            }
            it.isDropCompleted = success
            it.consume()
        }
    }

    private fun createSnackBar() {
        viewModel
            .snackBarObservable
            .doOnError { e ->
                logger.error("Error in creating add files snackbar", e)
            }
            .subscribe { pluginErrorMessage ->
                SnackbarHandler.enqueue(
                    JFXSnackbar.SnackbarEvent(
                        JFXSnackbarLayout(pluginErrorMessage),
                        Duration.millis(5000.0),
                        null
                    )
                )
            }
    }

    private fun collapse() {
        fire(DrawerEvent(this::class, DrawerEventAction.CLOSE))
    }
}
