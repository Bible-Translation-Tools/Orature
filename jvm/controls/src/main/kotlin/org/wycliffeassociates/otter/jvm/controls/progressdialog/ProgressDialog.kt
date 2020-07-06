package org.wycliffeassociates.otter.jvm.controls.progressdialog

import com.github.thomasnield.rxkotlinfx.toObservable
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.*
import javafx.stage.Modality
import javafx.stage.StageStyle
import tornadofx.*

class ProgressDialog : Fragment() {

    val graphicProperty = SimpleObjectProperty<Node>(VBox())
    var graphic by graphicProperty

    val textProperty = SimpleStringProperty()
    var text by textProperty

    init {
        importStylesheet<ProgressDialogStyles>()
    }

    override val root = borderpane {
        addClass(ProgressDialogStyles.defaultProgressDialog)
        center {
            stackpane {
                progressindicator()
                hbox {
                    alignment = Pos.CENTER
                    graphicProperty.toObservable().subscribe {
                        clear()
                        it.addClass(ProgressDialogStyles.progressGraphic)
                        add(it)
                    }
                }
            }
        }
        bottom {
            label(textProperty) {
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
