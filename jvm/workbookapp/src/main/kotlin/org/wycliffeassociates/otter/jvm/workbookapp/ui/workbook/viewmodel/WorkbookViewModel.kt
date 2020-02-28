package org.wycliffeassociates.otter.jvm.workbookapp.ui.workbook.viewmodel

import javafx.beans.property.*
import org.wycliffeassociates.otter.common.data.model.ResourceMetadata
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.jvm.workbookapp.ui.inject.Injector
import tornadofx.*
import java.io.File
import java.lang.IllegalStateException

class WorkbookViewModel : ViewModel() {
    private val injector: Injector by inject()
    private val directoryProvider = injector.directoryProvider

    val activeWorkbookProperty = SimpleObjectProperty<Workbook>()
    val workbook: Workbook
        get() = activeWorkbookProperty.value ?: throw IllegalStateException("Workbook is null")

    val activeChapterProperty = SimpleObjectProperty<Chapter>()
    val chapter: Chapter
        get() = activeChapterProperty.value ?: throw IllegalStateException("Chapter is null")

    val activeChunkProperty = SimpleObjectProperty<Chunk>()
    val chunk: Chunk? by activeChunkProperty

    val activeResourceMetadataProperty = SimpleObjectProperty<ResourceMetadata>()
    val activeResourceMetadata
        get() = activeResourceMetadataProperty.value ?: throw IllegalStateException("Resource Metadata is null")

    val activeProjectAudioDirectoryProperty = SimpleObjectProperty<File>()
    val activeProjectAudioDirectory: File
        get() = activeProjectAudioDirectoryProperty.value
            ?: throw IllegalStateException("Project audio directory is null")

    val sourceAudioFileProperty = activeChapterProperty.objectBinding { chap ->
        chap?.let {
            workbook.sourceAudioAccessor.get(it.sort)
        }
    }
    val sourceAudioAvailableProperty = sourceAudioFileProperty.booleanBinding { it?.exists() ?: false }

    fun setProjectAudioDirectory(resourceMetadata: ResourceMetadata) {
        val projectAudioDir = directoryProvider.getProjectAudioDirectory(
            source = workbook.source.resourceMetadata,
            target = resourceMetadata,
            bookSlug = workbook.target.slug
        )
        activeProjectAudioDirectoryProperty.set(projectAudioDir)
    }
}