package org.wycliffeassociates.otter.jvm.controls.exception.viewmodel

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

class ExceptionDialogViewModel : ViewModel() {

    val stackTrace = SimpleStringProperty("")
    val showMore = SimpleBooleanProperty(false)
    val sendReport = SimpleBooleanProperty(true)

    fun toggleShowMore() {
        showMore.value = !showMore.value
    }

    fun sendReport() {
        if(sendReport.value) {
            println("Report is being sent...")
            // TODO implement sending report
        }
    }
}