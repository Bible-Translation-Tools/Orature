package org.wycliffeassociates.otter.jvm.controls.media

import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.jvm.controls.skins.media.ExceptionContentSkin

class ExceptionContent : Control() {

    private val titleTextProperty = SimpleStringProperty()
    private val headerTextProperty = SimpleStringProperty()
    private val showMoreTextProperty = SimpleStringProperty()
    private val showLessTextProperty = SimpleStringProperty()
    private val showMoreProperty = SimpleBooleanProperty()
    private val sendReportTextProperty = SimpleStringProperty()
    private val sendReportProperty = SimpleBooleanProperty()
    private val stackTraceProperty = SimpleStringProperty()
    private val onCloseActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    private val closeTextProperty = SimpleStringProperty()
    private val sendingReportProperty = SimpleBooleanProperty()

    fun titleTextProperty(): StringProperty {
        return titleTextProperty
    }

    fun headerTextProperty(): StringProperty {
        return headerTextProperty
    }

    fun showMoreTextProperty(): StringProperty {
        return showMoreTextProperty
    }

    fun showLessTextProperty(): StringProperty {
        return showLessTextProperty
    }

    fun showMoreProperty(): BooleanProperty {
        return showMoreProperty
    }

    fun sendReportTextProperty(): StringProperty {
        return sendReportTextProperty
    }

    fun sendReportProperty(): BooleanProperty {
        return sendReportProperty
    }

    fun stackTraceProperty(): StringProperty {
        return stackTraceProperty
    }

    fun closeTextProperty(): StringProperty {
        return closeTextProperty
    }

    fun onCloseAction(op: () -> Unit) {
        onCloseActionProperty.set(EventHandler { op.invoke() })
    }

    fun onCloseActionProperty(): ObjectProperty<EventHandler<ActionEvent>> {
        return onCloseActionProperty
    }

    fun sendingReportProperty(): BooleanProperty {
        return sendingReportProperty
    }

    override fun createDefaultSkin(): Skin<*> {
        return ExceptionContentSkin(this)
    }
}
