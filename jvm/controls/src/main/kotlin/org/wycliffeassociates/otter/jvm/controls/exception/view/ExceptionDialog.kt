package org.wycliffeassociates.otter.jvm.controls.exception.view

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXCheckBox
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.layout.VBox
import javafx.stage.StageStyle
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.controls.exception.viewmodel.ExceptionDialogViewModel
import tornadofx.*


class ExceptionDialog : View() {
    override val root: VBox by fxml()
    private val exceptionDialogViewModel: ExceptionDialogViewModel by inject()

    private val defaultStyleClass = "dialog"

    private val showMoreButton: JFXButton by fxid()
    private val stacktraceScrollPane: ScrollPane by fxid()
    private val stacktraceText: Label by fxid()
    private val sendReportCheckbox: JFXCheckBox by fxid()
    private val closeButton: JFXButton by fxid()

    init {
        importStylesheet("/css/exception-dialog.css")
        root.addClass(defaultStyleClass)

        stacktraceText.bind(exceptionDialogViewModel.stackTrace)

        showMoreButton.apply {
            textProperty().bind(
                    Bindings.`when`(exceptionDialogViewModel.showMore)
                        .then("Show less")
                        .otherwise("Show more")
            )

            graphicProperty().bind(
                    Bindings.`when`(exceptionDialogViewModel.showMore)
                            .then(FontIcon("gmi-expand-less").apply {
                                styleClass.add("show_more_icon")
                            })
                            .otherwise(FontIcon("gmi-expand-more").apply {
                                styleClass.add("show_more_icon")
                            })
            )
            action {
                showMore()
            }
        }

        stacktraceScrollPane.apply {
            visibleWhen { exceptionDialogViewModel.showMore }
            managedWhen { exceptionDialogViewModel.showMore }
        }

        sendReportCheckbox.apply {
            selectedProperty().bindBidirectional(exceptionDialogViewModel.sendReport)
        }

        closeButton.apply {
            action {
                closeDialog()
            }
        }
    }

    private fun showMore() {
        exceptionDialogViewModel.toggleShowMore()
        currentStage?.sizeToScene()
    }

    fun openDialog(stackTrace: String) {
        openModal(stageStyle = StageStyle.UNDECORATED, escapeClosesWindow = false)
        exceptionDialogViewModel.stackTrace.value = stackTrace
    }

    fun closeDialog() {
        sendReport()
        close()
        Platform.exit()
    }

    private fun sendReport() {
        exceptionDialogViewModel.sendReport()
    }
}
