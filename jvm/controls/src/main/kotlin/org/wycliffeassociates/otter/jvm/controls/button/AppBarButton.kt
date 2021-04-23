package org.wycliffeassociates.otter.jvm.controls.button

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.css.PseudoClass
import javafx.event.EventHandler
import javafx.scene.control.Control
import javafx.scene.control.Skin
import javafx.scene.input.MouseEvent
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.controls.skins.button.AppBarButtonSkin
import tornadofx.*

class AppBarButton : Control() {

    val btnTextProperty = SimpleStringProperty()
    val btnIconProperty = SimpleObjectProperty<FontIcon>()
    val isActiveProperty = SimpleBooleanProperty(false)
    val onActionProperty = SimpleObjectProperty<EventHandler<MouseEvent>>()

    private val activePseudoClass = PseudoClass.getPseudoClass("active")

    init {
        isActiveProperty.onChange {
            pseudoClassStateChanged(activePseudoClass, it)
        }

        styleClass.setAll("app-bar-button")
    }

    fun onAction(op: () -> Unit) {
        onActionProperty.set(EventHandler { op.invoke() })
    }

    override fun createDefaultSkin(): Skin<*> {
        return AppBarButtonSkin(this)
    }
}
