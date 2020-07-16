package org.wycliffeassociates.otter.jvm.workbookapp.ui.workbook.viewmodel

import io.reactivex.Maybe
import javafx.beans.binding.Bindings
import javafx.beans.binding.StringBinding
import javafx.beans.property.SimpleObjectProperty
import org.wycliffeassociates.otter.common.data.model.ResourceMetadata
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.Resource
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.resourcecontainer.SourceAudio
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.ui.inject.Injector
import tornadofx.*
import java.io.File
import java.util.concurrent.Callable

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

    val activeResourceComponentProperty = SimpleObjectProperty<Resource.Component>()
    val activeResourceComponent by activeResourceComponentProperty
    val activeResourceProperty = SimpleObjectProperty<Resource>()

    val activeResourceMetadataProperty = SimpleObjectProperty<ResourceMetadata>()
    val activeResourceMetadata
        get() = activeResourceMetadataProperty.value ?: throw IllegalStateException("Resource Metadata is null")

    val activeProjectAudioDirectoryProperty = SimpleObjectProperty<File>()
    val activeProjectAudioDirectory: File
        get() = activeProjectAudioDirectoryProperty.value
            ?: throw IllegalStateException("Project audio directory is null")

    val sourceAudioProperty = SimpleObjectProperty<SourceAudio>()
    val sourceAudioAvailableProperty = sourceAudioProperty.booleanBinding { it?.file?.exists() ?: false }

    init {
        activeChapterProperty.onChange { updateSourceAudio() }
        activeChunkProperty.onChangeAndDoNow { updateSourceAudio() }
    }

    fun setProjectAudioDirectory(resourceMetadata: ResourceMetadata) {
        val projectAudioDir = directoryProvider.getProjectAudioDirectory(
            source = workbook.source.resourceMetadata,
            target = resourceMetadata,
            bookSlug = workbook.target.slug
        )
        activeProjectAudioDirectoryProperty.set(projectAudioDir)
    }

    fun updateSourceAudio() {
        val _chunk = activeChunkProperty.get()
        val _chapter = activeChapterProperty.get()
        if (_chapter != null && _chunk != null) {
            sourceAudioProperty.set(workbook.sourceAudioAccessor.getChunk(_chapter.sort, _chunk.sort))
        } else if (_chapter != null) {
            sourceAudioProperty.set(workbook.sourceAudioAccessor.getChapter(_chapter.sort))
        } else {
            sourceAudioProperty.set(null)
        }
    }

    fun getSourceAudio(): SourceAudio? {
        val sourceAudio = workbook.sourceAudioAccessor
        return chunk?.let { chunk ->
            sourceAudio.getChunk(
                chapter.sort,
                chunk.start
            )
        } ?: run { sourceAudio.getChapter(chapter.sort) }
    }

    fun getSourceText(): Maybe<String> {
        return when {
            activeResourceComponent != null -> Maybe.just(
                activeResourceComponent.textItem.text
            )
            chunk != null -> getSourceChunk().map { _chunk ->
                _chunk.textItem.text
            }
            else -> getSourceChapter().map { _chapter ->
                _chapter.textItem.text
            }
        }
    }

    fun sourceTextBinding(): StringBinding {
        return Bindings.createStringBinding(
            Callable {
                activeChapterProperty.value?.let {
                    getSourceText().blockingGet()
                }
            },
            activeChapterProperty,
            activeChunkProperty,
            activeResourceComponentProperty
        )
    }

    fun getSourceChapter(): Maybe<Chapter> {
        return workbook.source.chapters.filter {
            it.title == chapter.title
        }
            .singleElement()
    }

    fun getSourceChunk(): Maybe<Chunk> {
        return getSourceChapter()
            .flatMap { _chapter ->
                _chapter.chunks.filter { _chunk ->
                    _chunk.start == chunk?.start
                }
                    .singleElement()
            }
    }
}
