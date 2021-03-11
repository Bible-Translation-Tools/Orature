package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.schedulers.Schedulers
import javafx.beans.binding.Bindings
import javafx.beans.binding.StringBinding
import javafx.beans.property.SimpleObjectProperty
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.Resource
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.resourcecontainer.SourceAudio
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import tornadofx.*
import java.text.MessageFormat
import java.util.concurrent.Callable
import javax.inject.Inject

class WorkbookDataStore : Component(), ScopedInstance {
    @Inject
    lateinit var directoryProvider: IDirectoryProvider

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

    val activeProjectFilesAccessorProperty = SimpleObjectProperty<ProjectFilesAccessor>()
    val activeProjectFilesAccessor: ProjectFilesAccessor
        get() = activeProjectFilesAccessorProperty.value
            ?: throw IllegalStateException("Project files is null")

    val sourceAudioProperty = SimpleObjectProperty<SourceAudio>()
    val sourceAudioAvailableProperty = sourceAudioProperty.booleanBinding { it?.file?.exists() ?: false }

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
        activeChapterProperty.onChange { updateSourceAudio() }
        activeChunkProperty.onChangeAndDoNow { updateSourceAudio() }
    }

    fun setProjectFilesAccessor(resourceMetadata: ResourceMetadata) {
        val projectFilesAccessor = ProjectFilesAccessor(
            directoryProvider,
            workbook.source.resourceMetadata,
            resourceMetadata,
            workbook.target.toCollection()
        )
        activeProjectFilesAccessorProperty.set(projectFilesAccessor)

        val linkedResource = workbook
            .source
            .linkedResources
            .firstOrNull { it.identifier == resourceMetadata.identifier }

        activeProjectFilesAccessor.initializeResourceContainerInDir()
        activeProjectFilesAccessor.copySourceFiles(linkedResource)
        activeProjectFilesAccessor.createSelectedTakesFile()
    }

    fun updateSelectedTakesFile() {
        Completable
            .fromCallable {
                val projectIsBook = activeResourceMetadata.identifier == workbook.target.resourceMetadata.identifier
                activeProjectFilesAccessor.writeSelectedTakesFile(workbook, projectIsBook)
            }
            .subscribeOn(Schedulers.io())
            .subscribe()
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

    fun activeChunkTitleBinding(): StringBinding {
        return Bindings.createStringBinding(
            Callable {
                if (activeWorkbookProperty.value != null && activeChapterProperty.value != null) {
                    if (activeChunkProperty.value != null) {
                        MessageFormat.format(
                            messages["bookChapterChunkTitle"],
                            activeWorkbookProperty.value.source.title,
                            activeChapterProperty.value.title,
                            activeChunkProperty.value.start
                        )
                    } else {
                        MessageFormat.format(
                            messages["bookChapterTitle"],
                            activeWorkbookProperty.value.source.title,
                            activeChapterProperty.value.title
                        )
                    }
                } else {
                    null
                }
            },
            activeWorkbookProperty,
            activeChapterProperty,
            activeChunkProperty
        )
    }

    fun getSourceChapter(): Maybe<Chapter> {
        return workbook
            .source
            .chapters
            .filter {
                it.title == chapter.title
            }
            .singleElement()
    }

    fun getSourceChunk(): Maybe<Chunk> {
        return getSourceChapter()
            .flatMap { _chapter ->
                _chapter
                    .chunks
                    .filter { _chunk ->
                        _chunk.start == chunk?.start
                    }
                    .singleElement()
            }
    }
}
