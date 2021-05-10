package org.wycliffeassociates.otter.jvm.controls.dialog

import javafx.geometry.Bounds
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import tornadofx.*

abstract class OtterDialog : Fragment() {

    private val roundRadius = 15.0

    private val mainContainer = VBox().apply {
        addClass("otter-dialog-container")
    }

    override val root = VBox().apply {
        addClass("otter-dialog-overlay")
        add(mainContainer)
    }

    init {
        importStylesheet(resources.get("/css/otter-dialog.css"))
    }

    fun open() {
        val stage = openModal(StageStyle.TRANSPARENT, Modality.APPLICATION_MODAL, false)
        stage?.let {
            fitStageToParent(it)
        }
    }

    fun setContent(content: Region) {
        mainContainer.add(
            content.apply {
                addClass("otter-dialog-content")

                vgrow = Priority.NEVER
                maxWidth = Region.USE_PREF_SIZE

                layoutBoundsProperty().onChange {
                    it?.let {
                        clipRegion(content, it)
                    }
                }
            }
        )
    }

    private fun fitStageToParent(stage: Stage) {
        stage.width = primaryStage.width
        stage.height = primaryStage.height
        stage.x = primaryStage.x
        stage.y = primaryStage.y
        stage.scene.fill = Color.TRANSPARENT
    }

    private fun clipRegion(region: Region, bounds: Bounds) {
        val rect = Rectangle()
        rect.width = bounds.width
        rect.height = bounds.height
        rect.arcWidth = roundRadius
        rect.arcHeight = roundRadius
        region.clip = rect
    }
}
