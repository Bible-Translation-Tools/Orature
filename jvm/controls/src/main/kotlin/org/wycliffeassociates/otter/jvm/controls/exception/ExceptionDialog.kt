package org.wycliffeassociates.otter.jvm.controls.exception

import javafx.beans.property.*
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.jvm.controls.skins.ExceptionDialogSkin

class ExceptionDialog : Control() {

    val titleTextProperty = SimpleStringProperty()
    val headerTextProperty = SimpleStringProperty()
    val showMoreTextProperty = SimpleStringProperty()
    val showLessTextProperty = SimpleStringProperty()
    val showMore = SimpleBooleanProperty()
    val sendReportTextProperty = SimpleStringProperty()
    val sendReportProperty = SimpleBooleanProperty()
    val stackTraceProperty = SimpleStringProperty()
    val onCloseAction = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val closeTextProperty = SimpleStringProperty()

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
        onCloseAction.set(EventHandler { op.invoke() })
    }

    override fun createDefaultSkin(): Skin<*> {
        return ExceptionDialogSkin(this)
    }
}

fun EventTarget.exceptionDialog(
    op: ExceptionDialog.() -> Unit = {}
) = ExceptionDialog().apply(op)
