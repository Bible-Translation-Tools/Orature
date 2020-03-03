package org.wycliffeassociates.otter.jvm.controls.exception.viewmodel

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler

class ExceptionDialogViewModel {

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

    fun toggleShowMore() {
        showMore.set(!showMore.get())
    }
}