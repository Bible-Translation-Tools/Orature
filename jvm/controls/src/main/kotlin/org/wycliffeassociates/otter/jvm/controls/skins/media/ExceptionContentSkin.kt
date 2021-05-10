package org.wycliffeassociates.otter.jvm.controls.skins.media

import javafx.beans.binding.Bindings
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.control.SkinBase
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.controls.media.ExceptionContent
import tornadofx.*

class ExceptionContentSkin(private var content: ExceptionContent) : SkinBase<ExceptionContent>(content) {

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
            visibleProperty().bind(this@ExceptionContentSkin.content.showMoreProperty())
            managedProperty().bind(this@ExceptionContentSkin.content.showMoreProperty())
        }

        importStylesheet(javaClass.getResource("/css/exception-content.css").toExternalForm())
    }

    private fun bindText() {
        titleLabel.textProperty().bind(content.titleTextProperty())
        headerLabel.textProperty().bind(content.headerTextProperty())
        sendReportCheckbox.apply {
            textProperty().bind(content.sendReportTextProperty())
            content.sendReportProperty().bind(selectedProperty())
        }
        stacktraceText.textProperty().bind(content.stackTraceProperty())
        showMoreButton.apply {
            textProperty().bind(
                Bindings.`when`(content.showMoreProperty())
                    .then(content.showLessTextProperty())
                    .otherwise(content.showMoreTextProperty())
            )
            graphicProperty().bind(
                Bindings.`when`(content.showMoreProperty())
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
            onActionProperty().bind(content.onCloseActionProperty())
            textProperty().bind(content.closeTextProperty())
            disableProperty().bind(content.sendingReportProperty())
        }
    }

    private fun showMore() {
        content.showMoreProperty().set(!content.showMoreProperty().get())
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("ExceptionContent.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}
