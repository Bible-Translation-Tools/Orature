package org.wycliffeassociates.otter.common.domain

import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.domain.resourcecontainer.SourceAudioAccessor
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceMetadataRepository
import javax.inject.Inject

data class SourceInfo(val metadata: ResourceMetadata)

class SourceAudioRepository @Inject constructor(
    private val directoryProvider: IDirectoryProvider,
    private val resourceMetadataRepository: IResourceMetadataRepository,
    private val collectionRepository: ICollectionRepository
) {
    fun getAllSources(): Single<List<SourceInfo>> {
        return resourceMetadataRepository.getAll()
            .map { list ->
                list.map { SourceInfo(it) }
            }
    }

    fun getSourcesByBook(): List<Collection> {
        val books = collectionRepository.getSourceProjects()
            .blockingGet()

        val booksWithAudio = books.filter {
            SourceAudioAccessor.hasSourceAudio(it.resourceContainer!!, it.slug)
        }

//        booksWithAudio.first().let { SourceAudioAccessor(directoryProvider, it.resourceContainer!!, it.slug) }.getChapter(1)
        return books
    }
}