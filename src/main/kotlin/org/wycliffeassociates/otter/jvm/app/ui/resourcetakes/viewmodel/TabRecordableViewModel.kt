package org.wycliffeassociates.otter.jvm.app.ui.resourcetakes.viewmodel

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleStringProperty
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.viewmodel.RecordableViewModel
import java.util.concurrent.Callable

class TabRecordableViewModel(
    val labelProperty: SimpleStringProperty
): RecordableViewModel() {

    fun getFormattedTextBinding() = Bindings.createStringBinding(
        Callable { getFormattedText() },
        recordableProperty
    )

    private fun getFormattedText(): String? = recordable?.textItem?.text
}