package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking

import org.wycliffeassociates.otter.jvm.markerapp.app.view.MarkerView
import javafx.geometry.Pos
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Region
import javafx.scene.control.ButtonBar
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.shape.Rectangle
import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight
import tornadofx.*

class ChunkingWizard : Wizard() {

    val vm: ChunkingViewModel by inject()

    val consumeStep = Rectangle().apply {
        style {
            fillProperty().bind(vm.consumeStepColor)
            width = 80.0
            height = 8.0
            arcHeight = 6.px
            arcWidth = 6.px


        } }
    val verbalizeStep = Rectangle().apply { style {
        fillProperty().bind(vm.verbalizeStepColor)
        width = 80.0
        height = 8.0
        arcHeight = 6.px
        arcWidth = 6.px

    } }
    val chunkStep = Rectangle().apply { style {
        fillProperty().bind(vm.chunkStepColor)
        width = 80.0
        height = 8.0
        arcHeight = 6.px
        arcWidth = 6.px

    } }


    override fun onDock() {
        val top = vbox {
            addClass(WizardStyles.header)
            removeClass(WizardStyles.content)
            alignment = Pos.CENTER
            label {
                textProperty().bind(vm.titleProperty)
                style {
                    fontSize = 26.px
                    //fontSize = 18.pt
                    //fontFamily = "Atkinson Hyperlegible"
                    fontWeight = FontWeight.EXTRA_BOLD
                    fontStyle = FontPosture.REGULAR
                }
            }
            label {
                textProperty().bind(vm.stepProperty)
                style {
                    fontSize = 20.px
                    //fontSize = 12.pt
                    //fontFamily = "Atkinson Hyperlegible"
                    fontWeight = FontWeight.NORMAL
                    fontStyle = FontPosture.REGULAR
                }
            }
            hbox {
                spacing = 5.0
                alignment = Pos.CENTER
                addClass(WizardStyles.buttons)
                spacer()
                button() {
                    styleClass.addAll("btn", "btn--secondary")
                    textProperty().bind(backButtonTextProperty)
                    runLater {
                        enableWhen(canGoBack)
                    }
                    action { back() }
                }

                add(consumeStep)
                add(verbalizeStep)
                add(chunkStep)

                button() {
                    styleClass.addAll("btn", "btn--secondary")
                    textProperty().bind(nextButtonTextProperty)
                    runLater {
                        enableWhen(canGoNext.and(hasNext).and(currentPageComplete))
                    }
                    action { next() }
                }

                spacer()
                style {
                    padding = box(0.px)
                    backgroundColor += Color.WHITE
                    borderWidth += box(0.px)
                    borderColor += box(Color.TRANSPARENT)
                }
            }
        }
        root.bottom.getChildList()!!.clear()
        root.top.replaceWith(top)
        root.bottom.replaceWith(Region())
        root.left.replaceWith(Region())
        root.center.style {
            padding = box(0.px)
        }
    }

    init {
        add<Consume>()
        add<Verbalize>()
        add<ChunkPage>()
    }

    override fun closeWizard() {
        complete.set(false)
        workspace.navigateBack()
    }
}
