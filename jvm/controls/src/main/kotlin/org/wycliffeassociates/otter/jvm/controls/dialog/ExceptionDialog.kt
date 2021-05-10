package org.wycliffeassociates.otter.jvm.controls.dialog

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import org.wycliffeassociates.otter.jvm.controls.media.ExceptionContent

class ExceptionDialog : OtterDialog() {

    val titleTextProperty = SimpleStringProperty()
    val headerTextProperty = SimpleStringProperty()
    val showMoreTextProperty = SimpleStringProperty()
    val showLessTextProperty = SimpleStringProperty()
    val sendReportTextProperty = SimpleStringProperty()
    val sendReportProperty = SimpleBooleanProperty()
    val stackTraceProperty = SimpleStringProperty()
    val closeTextProperty = SimpleStringProperty()
    val sendingReportProperty = SimpleBooleanProperty()

    private val onCloseActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    private val exceptionContent = ExceptionContent().apply {
        titleTextProperty().bind(titleTextProperty)
        headerTextProperty().bind(headerTextProperty)
        showMoreTextProperty().bind(showMoreTextProperty)
        showLessTextProperty().bind(showLessTextProperty)
        sendReportTextProperty().bind(sendReportTextProperty)
        sendingReportProperty().bind(sendingReportProperty)
        stackTraceProperty().bind(stackTraceProperty)
        closeTextProperty().bind(closeTextProperty)
        onCloseActionProperty().bind(onCloseActionProperty)

        sendReportProperty.bind(sendReportProperty())
    }

    init {
        setContent(exceptionContent)
    }

    fun onCloseAction(op: () -> Unit) {
        onCloseActionProperty.set(EventHandler { op.invoke() })
    }
}
