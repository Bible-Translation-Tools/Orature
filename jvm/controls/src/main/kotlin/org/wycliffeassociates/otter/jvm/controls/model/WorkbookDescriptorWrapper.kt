package org.wycliffeassociates.otter.jvm.controls.model

import javafx.beans.property.SimpleDoubleProperty
import org.wycliffeassociates.otter.common.data.workbook.WorkbookDescriptor
import tornadofx.*

data class WorkbookDescriptorWrapper(
    val workbookDescriptor: WorkbookDescriptor
) {
    val id = workbookDescriptor.id
    val sourceCollection = workbookDescriptor.sourceCollection
    val targetCollection = workbookDescriptor.targetCollection
    val mode = workbookDescriptor.mode
    val hasSourceAudio = workbookDescriptor.hasSourceAudio
    val slug = workbookDescriptor.targetCollection.slug
    val title = workbookDescriptor.targetCollection.titleKey
    val label = workbookDescriptor.targetCollection.labelKey
    val sort = workbookDescriptor.sourceCollection.sort
    val lastModified = workbookDescriptor.targetCollection.modifiedTs
    val sourceLanguage = workbookDescriptor.sourceLanguage
    val targetLanguage = workbookDescriptor.targetLanguage
    val anthology = workbookDescriptor.anthology

    val progressProperty = SimpleDoubleProperty(0.0)
    val progress by progressProperty
}