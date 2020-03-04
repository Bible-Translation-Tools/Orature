package org.wycliffeassociates.otter.jvm.controls.skins

import javafx.beans.binding.Bindings
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.*
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.controls.exception.ExceptionDialog
import tornadofx.*


class ExceptionDialogSkin(private var dialog: ExceptionDialog) : SkinBase<ExceptionDialog>(dialog) {

    @FXML
    private lateinit var titleLabel: Label

    @FXML
    private lateinit var headerLabel: Label

    @FXML
    private lateinit var showMoreButton: Button

    @FXML
    private lateinit var stacktraceScrollPane: ScrollPane

    @FXML
    private lateinit var stacktraceText: Label

    @FXML
    private lateinit var sendReportCheckbox: CheckBox

    @FXML
    private lateinit var closeButton: Button

    private val showMoreIcon = FontIcon("gmi-expand-more").apply { styleClass.add("btn__icon") }
    private val showLessIcon = FontIcon("gmi-expand-less").apply { styleClass.add("btn__icon") }

    init {
        loadFXML()
        bindText()
        bindAction()

        stacktraceScrollPane.apply {
            visibleProperty().bind(dialog.showMore)
            managedProperty().bind(dialog.showMore)
        }
    }

    private fun bindText() {
        titleLabel.textProperty().bind(dialog.titleTextProperty)
        headerLabel.textProperty().bind(dialog.headerTextProperty)
        sendReportCheckbox.apply {
            textProperty().bind(dialog.sendReportTextProperty)
            dialog.sendReportProperty.bind(selectedProperty())
        }
        stacktraceText.textProperty().bind(dialog.stackTraceProperty)
        showMoreButton.apply {
            textProperty().bind(
                Bindings.`when`(dialog.showMore)
                    .then(dialog.showLessTextProperty)
                    .otherwise(dialog.showMoreTextProperty)
            )
            graphicProperty().bind(
                Bindings.`when`(dialog.showMore)
                    .then(showLessIcon)
                    .otherwise(showMoreIcon)
            )
        }
    }

    private fun bindAction() {
        showMoreButton.setOnAction {
            showMore()
        }
        closeButton.apply {
            onActionProperty().bind(dialog.onCloseAction)
            textProperty().bind(dialog.closeTextProperty)
        }
    }

    private fun showMore() {
        dialog.showMore.set(!dialog.showMore.get())
        dialog.scene.window.sizeToScene()
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("ExceptionDialog.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}
