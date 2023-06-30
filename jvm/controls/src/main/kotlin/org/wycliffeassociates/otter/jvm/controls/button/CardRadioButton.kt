package org.wycliffeassociates.otter.jvm.controls.button

import com.sun.javafx.scene.control.behavior.ButtonBehavior
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.scene.control.Skin
import javafx.scene.control.SkinBase
import javafx.scene.control.ToggleButton
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import tornadofx.*

class CardRadioButton(tg: ToggleGroup) : ToggleButton() {

    val titleProperty = SimpleStringProperty("")
    val subTitleProperty = SimpleStringProperty("")
    private val radioActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    init {
        styleClass.setAll("radio-button-card__control")
        toggleGroup = tg
    }

    override fun createDefaultSkin(): Skin<*> {
        return CardRadioButtonSkin(this)
    }

    override fun fire() {
        if (toggleGroup == null || !isSelected) {
            super.fire()
            radioActionProperty.value?.handle(ActionEvent())
        }
    }

    fun setOnAction(op: () -> Unit) {
        radioActionProperty.set(EventHandler { op() })
    }
}

class CardRadioButtonSkin(button: CardRadioButton) : SkinBase<CardRadioButton>(button) {
    private val behavior = ButtonBehavior(button)

    private val graphic = HBox().apply {
        addClass("radio-button-card")
        vbox {
            addClass("radio-button-card__labels")
            label {
                addClass("h4", "radio__label-text")
                textProperty().bind(button.titleProperty)
                minHeight = Region.USE_PREF_SIZE
            }
            label {
                addClass("h5", "radio__label-text")
                textProperty().bind(button.subTitleProperty)
                /* extends the label text vertically to avoid ellipsis when overflow */
                minHeight = Region.USE_PREF_SIZE

                visibleWhen { button.subTitleProperty.isNotEmpty }
                managedWhen(visibleProperty())
            }
        }
        region { hgrow = Priority.ALWAYS }
        radiobutton {
            addClass("wa-radio")
            isFocusTraversable = false
            isMouseTransparent = true

            selectedProperty().bind(button.selectedProperty())
        }
    }

    init {
        children.setAll(graphic)
    }

    override fun dispose() {
        super.dispose()
        behavior.dispose()
    }
}

fun EventTarget.cardRadioButton(tg: ToggleGroup, op: CardRadioButton.() -> Unit = {}) = CardRadioButton(tg).attachTo(this, op)