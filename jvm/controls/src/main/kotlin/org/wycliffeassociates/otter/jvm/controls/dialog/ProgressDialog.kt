package org.wycliffeassociates.otter.jvm.controls.dialog

import com.github.thomasnield.rxkotlinfx.toObservable
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.StageStyle
import org.slf4j.LoggerFactory
import tornadofx.*

class ProgressDialog : Fragment() {

    private val logger = LoggerFactory.getLogger(ProgressDialog::class.java)

    val graphicProperty = SimpleObjectProperty<Node>(VBox())
    var graphic by graphicProperty

    val textProperty = SimpleStringProperty()
    var text by textProperty

    init {
        importStylesheet(resources.get("/css/progress-dialog.css"))
    }

    override val root = borderpane {
        addClass("progress-dialog")
        center {
            stackpane {
                progressindicator {
                    addClass("progress-dialog__progress-indicator")
                }
                hbox {
                    alignment = Pos.CENTER
                    graphicProperty
                        .toObservable()
                        .doOnError { e ->
                            logger.error("Error in ProgressDialog", e)
                        }
                        .subscribe {
                            clear()
                            it.addClass("progress-dialog__graphic")
                            add(it)
                        }
                }
            }
        }
        bottom {
            label(textProperty) {
                addClass("progress-dialog__message")
                visibleProperty().bind(textProperty().isNotEmpty)
                managedProperty().bind(visibleProperty())
            }
        }
    }

    fun open() {
        openModal(StageStyle.UNDECORATED, Modality.APPLICATION_MODAL)
    }
}

fun progressdialog(setup: ProgressDialog.() -> Unit = {}): ProgressDialog {
    val progressDialog = ProgressDialog()
    progressDialog.setup()
    return progressDialog
}
