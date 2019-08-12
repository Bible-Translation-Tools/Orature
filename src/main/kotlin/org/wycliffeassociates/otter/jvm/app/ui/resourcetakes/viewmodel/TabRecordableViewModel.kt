package org.wycliffeassociates.otter.jvm.app.ui.resourcetakes.viewmodel

import javafx.beans.binding.Bindings
import javafx.beans.binding.StringBinding
import javafx.beans.property.SimpleStringProperty
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.viewmodel.AudioPluginViewModel
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.viewmodel.RecordableViewModel
import java.util.concurrent.Callable

class TabRecordableViewModel(
    val labelProperty: SimpleStringProperty,
    audioPluginViewModel: AudioPluginViewModel
): RecordableViewModel(
    audioPluginViewModel
) {
    fun getFormattedTextBinding(): StringBinding = Bindings.createStringBinding(
        Callable { getFormattedText() },
        recordableProperty
    )

    private fun getFormattedText(): String? = recordable?.textItem?.text
}