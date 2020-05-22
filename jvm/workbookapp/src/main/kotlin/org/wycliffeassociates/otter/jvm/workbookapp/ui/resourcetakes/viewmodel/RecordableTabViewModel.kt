package org.wycliffeassociates.otter.jvm.workbookapp.ui.resourcetakes.viewmodel

import javafx.beans.binding.Bindings
import javafx.beans.binding.DoubleBinding
import javafx.beans.binding.StringBinding
import javafx.beans.property.SimpleStringProperty
import org.wycliffeassociates.otter.common.domain.content.ResourceRecordable
import org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.viewmodel.AudioPluginViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.viewmodel.RecordableViewModel
import java.util.concurrent.Callable

class RecordableTabViewModel(
    val labelProperty: SimpleStringProperty,
    audioPluginViewModel: AudioPluginViewModel
) : RecordableViewModel(
    audioPluginViewModel
) {
    fun getFormattedTextBinding(): StringBinding = Bindings.createStringBinding(
        Callable { getFormattedText() },
        recordableProperty
    )

    fun getProgressBinding(): DoubleBinding = Bindings.createDoubleBinding(
        Callable { getProgress() },
        selectedTakeProperty
    )

    private fun getFormattedText(): String? = (recordable as? ResourceRecordable)?.textItem?.text

    private fun getProgress(): Double = selectedTakeProperty.get()?.let { 1.0 } ?: 0.0
}