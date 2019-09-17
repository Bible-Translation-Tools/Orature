package org.wycliffeassociates.otter.jvm.app.widgets.card

import com.jfoenix.controls.JFXButton
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import tornadofx.*

class CardFront : StackPane() {

    val childrenList = observableList<Node>()

    val defaultFill = c("#CC4141")
    val forwardArrow = MaterialIconView(MaterialIcon.ARROW_FORWARD, "20px")

    val isActiveProperty = SimpleBooleanProperty(false)
    var isActive by isActiveProperty

    val isCompleteProperty = SimpleBooleanProperty(false)
    var isComplete by isCompleteProperty

    fun innercard(cardGraphic: Node? = null, init: InnerCard.() -> Unit = {}): InnerCard {
        val ic = InnerCard(cardGraphic)
        ic.init()
        addLayer(ic)
        return ic
    }

    fun cardbutton(init: JFXButton.() -> Unit = {}): JFXButton {
        val bttn = JFXButton()
        bttn.init()
        addLayer(bttn)
        return bttn
    }

    fun addLayer(layer: Node) {
        val tempVbox: VBox = vbox(10) {
            alignment = Pos.CENTER
            style {
                padding = box(2.0.px)
            }
            // add all existing children into the temp Vbox
            childrenList.forEach {
                add(it)
            }
            // add the new layer that will be at bottom of vbox
            add(layer)
        }

        childrenList.setAll(tempVbox)
    }

    init {
        importStylesheet<DefaultStyles>()
        forwardArrow.fill = defaultFill
        alignment = Pos.TOP_CENTER
        // the colored top half of the card
        vbox {
            vgrow = Priority.ALWAYS
            addClass(DefaultStyles.defaultBaseTop)
            toggleClass(DefaultStyles.activeBaseTop, isActiveProperty)
            toggleClass(DefaultStyles.completeBaseTop, isCompleteProperty)
        }

        vbox(10) {
            childrenList.onChange {
                it.list.forEach {
                    add(it)
                }
            }
        }
    }
}
