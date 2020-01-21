package org.wycliffeassociates.otter.jvm.workbookapp.ui.resourcetakes.viewmodel

import javafx.beans.binding.Bindings
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

    private fun getFormattedText(): String? = (recordable as? ResourceRecordable)?.textItem?.text
}