package org.wycliffeassociates.otter.jvm.controls.exception.view

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXCheckBox
import javafx.beans.binding.Bindings
import javafx.beans.property.BooleanProperty
import javafx.beans.property.StringProperty
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.layout.StackPane
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.controls.exception.viewmodel.ExceptionDialogViewModel


private const val DEFAULT_STYLE_CLASS = "dialog--exception"

class ExceptionDialog : StackPane() {

    private val viewModel = ExceptionDialogViewModel()

    @FXML
    private lateinit var titleLabel: Label

    @FXML
    private lateinit var headerLabel: Label

    @FXML
    private lateinit var showMoreButton: JFXButton

    @FXML
    private lateinit var stacktraceScrollPane: ScrollPane

    @FXML
    private lateinit var stacktraceText: Label

    @FXML
    private lateinit var sendReportCheckbox: JFXCheckBox

    @FXML
    private lateinit var closeButton: JFXButton

    fun titleTextProperty(): StringProperty {
        return viewModel.titleTextProperty
    }

    fun headerTextProperty(): StringProperty {
        return viewModel.headerTextProperty
    }

    fun showMoreTextProperty(): StringProperty {
        return viewModel.showMoreTextProperty
    }

    fun showLessTextProperty(): StringProperty {
        return viewModel.showLessTextProperty
    }

    fun sendReportTextProperty(): StringProperty {
        return viewModel.sendReportTextProperty
    }

    fun sendReportProperty(): BooleanProperty {
        return viewModel.sendReportProperty
    }

    fun stackTraceProperty(): StringProperty {
        return viewModel.stackTraceProperty
    }

    fun closeTextProperty(): StringProperty {
        return viewModel.closeTextProperty
    }

    fun onCloseAction(op: () -> Unit) {
        viewModel.onCloseAction.set(EventHandler { op.invoke() })
    }

    init {
        loadFXML()
        styleClass.add(DEFAULT_STYLE_CLASS)

        bindText()
        bindAction()

        stacktraceScrollPane.apply {
            visibleProperty().bind(viewModel.showMore)
            managedProperty().bind(viewModel.showMore)
        }
    }

    private fun bindText() {
        titleLabel.textProperty().bind(viewModel.titleTextProperty)
        headerLabel.textProperty().bind(viewModel.headerTextProperty)
        sendReportCheckbox.apply {
            textProperty().bind(viewModel.sendReportTextProperty)
            viewModel.sendReportProperty.bind(selectedProperty())
        }
        stacktraceText.textProperty().bind(viewModel.stackTraceProperty)
        showMoreButton.apply {
            textProperty().bind(
                Bindings.`when`(viewModel.showMore)
                    .then(viewModel.showLessTextProperty)
                    .otherwise(viewModel.showMoreTextProperty)
            )
            graphicProperty().bind(
                Bindings.`when`(viewModel.showMore)
                    .then(FontIcon("gmi-expand-less").apply {
                        styleClass.add("btn__icon")
                    })
                    .otherwise(FontIcon("gmi-expand-more").apply {
                        styleClass.add("btn__icon")
                    })
            )
        }
    }

    private fun bindAction() {
        showMoreButton.setOnAction {
            showMore()
        }
        closeButton.apply {
            onActionProperty().bind(viewModel.onCloseAction)
            textProperty().bind(viewModel.closeTextProperty)
        }
    }

    private fun showMore() {
        viewModel.toggleShowMore()
        scene.window.sizeToScene()
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("ExceptionDialog.fxml"))
        loader.setRoot(this)
        loader.setController(this)
        loader.load<ExceptionDialog>()
    }

    fun openModal() {
        val stage = Stage(StageStyle.UNDECORATED)
        stage.initModality(Modality.APPLICATION_MODAL)
        stage.scene = Scene(this)
        stage.scene.stylesheets.addAll(
            "/css/root.css",
            "/css/button.css",
            "/css/exception-dialog.css"
        )
        stage.showAndWait()
    }

    fun close() {
        scene.window.hide()
    }
}

fun exceptionDialog(
    op: ExceptionDialog.() -> Unit = {}
) = ExceptionDialog().apply(op)
