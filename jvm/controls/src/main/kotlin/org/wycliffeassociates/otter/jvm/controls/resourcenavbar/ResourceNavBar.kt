package org.wycliffeassociates.otter.jvm.controls.resourcenavbar

import javafx.beans.property.*
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Control
import javafx.scene.control.Skin

class ResourceNavBar: Control() {

    private val nextButtonText = SimpleStringProperty("Next")
    private val previousButtonText = SimpleStringProperty("Previous")

    private val hasNext = SimpleBooleanProperty()
    private val hasPrevious = SimpleBooleanProperty()

    private val onNextActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    private val onPreviousActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    private val nextButtonMaxWidth = SimpleDoubleProperty(500.0)
    private val previousButtonMaxWidth = SimpleDoubleProperty(500.0)

    fun nextButtonTextProperty(): StringProperty {
        return nextButtonText
    }

    fun previousButtonTextProperty(): StringProperty {
        return previousButtonText
    }

    fun hasNextProperty(): BooleanProperty {
        return hasNext
    }

    fun hasPreviousProperty(): BooleanProperty {
        return hasPrevious
    }

    fun onNextAction(op: () -> Unit) {
        onNextActionProperty.set(EventHandler { op.invoke() })
    }

    fun onNextActionProperty(): ObjectProperty<EventHandler<ActionEvent>> {
        return onNextActionProperty
    }

    fun onPreviousAction(op: () -> Unit) {
        onPreviousActionProperty.set(EventHandler { op.invoke() })
    }

    fun onPreviousActionProperty(): ObjectProperty<EventHandler<ActionEvent>> {
        return onPreviousActionProperty
    }

    fun nextButtonMaxWidthProperty(): DoubleProperty {
        return nextButtonMaxWidth
    }

    fun previousButtonMaxWidthProperty(): DoubleProperty {
        return previousButtonMaxWidth
    }

    override fun createDefaultSkin(): Skin<*> {
        return ResourceNavBarSkin(this)
    }

    override fun getUserAgentStylesheet(): String {
        return javaClass.getResource("/css/resourcenavbar.css").toExternalForm()
    }
}

fun resourcenavbar(init: ResourceNavBar.() -> Unit = {}): ResourceNavBar {
    val navBar = ResourceNavBar()
    navBar.init()
    return navBar
}