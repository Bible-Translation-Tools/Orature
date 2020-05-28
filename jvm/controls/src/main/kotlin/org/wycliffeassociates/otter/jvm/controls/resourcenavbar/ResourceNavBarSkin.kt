package org.wycliffeassociates.otter.jvm.controls.resourcenavbar

import com.jfoenix.controls.JFXButton
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.control.SkinBase
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

class ResourceNavBarSkin(private var navBar: ResourceNavBar): SkinBase<ResourceNavBar>(navBar) {

    private val container: HBox = HBox()
    private val left: VBox = VBox()
    private val right: VBox = VBox()
    private val nextButton = JFXButton()
    private val previousButton = JFXButton()

    init {
        nextButton.apply {
            styleClass.add("navbar--resource__button")
            textProperty().bind(navBar.nextButtonTextProperty())
            graphic = MaterialIconView(MaterialIcon.ARROW_FORWARD, "26px")
            onActionProperty().bind(navBar.onNextActionProperty())
            disableProperty().bind(navBar.hasNextProperty().not())
            maxWidthProperty().bind(navBar.nextButtonMaxWidthProperty())
        }

        previousButton.apply {
            styleClass.add("navbar--resource__button")
            textProperty().bind(navBar.previousButtonTextProperty())
            graphic = MaterialIconView(MaterialIcon.ARROW_BACK, "26px")
            onActionProperty().bind(navBar.onPreviousActionProperty())
            disableProperty().bind(navBar.hasPreviousProperty().not())
            maxWidthProperty().bind(navBar.previousButtonMaxWidthProperty())
        }

        left.apply {
            HBox.setHgrow(this, Priority.ALWAYS)
            alignment = Pos.CENTER
            children.addAll(previousButton)
        }

        right.apply {
            HBox.setHgrow(this, Priority.ALWAYS)
            alignment = Pos.CENTER
            children.addAll(nextButton)
        }

        container.apply {
            children.addAll(left, right)
            styleClass.add("navbar--resource")
        }

        children.setAll(container)
    }
}