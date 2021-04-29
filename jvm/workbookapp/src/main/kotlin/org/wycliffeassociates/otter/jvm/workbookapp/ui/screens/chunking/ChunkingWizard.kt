package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking

import org.wycliffeassociates.otter.jvm.markerapp.app.view.MarkerView
import javafx.geometry.Pos
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Region
import javafx.scene.control.ButtonBar
import tornadofx.*

class ChunkingWizard : Wizard() {

    override fun onDock() {
        val top = vbox {
            addClass(WizardStyles.header)
            alignment = Pos.CENTER
            label {
                textProperty().bind(titleProperty)
            }
            label {
                textProperty().bind(stepsTextProperty)
            }
            hbox {
                alignment = Pos.CENTER
                addClass(WizardStyles.buttons)
                button() {
                    textProperty().bind(backButtonTextProperty)
                    runLater {
                        enableWhen(canGoBack)
                    }
                    action { back() }
                }
                spacer()
                button() {
                    textProperty().bind(nextButtonTextProperty)
                    runLater {
                        enableWhen(canGoNext.and(hasNext).and(currentPageComplete))
                    }
                    action { next() }
                }
            }
        }
        root.bottom.getChildList()!!.clear()
        root.top.replaceWith(top)
        root.bottom.replaceWith(Region())
        root.left.replaceWith(Region())
    }

    init {
        add<Consume>()
        add<Verbalize>()
        add<MarkerView>()
    }

    override fun closeWizard() {
        complete.set(false)
        workspace.navigateBack()
    }
}
