package org.wycliffeassociates.otter.common.domain.collections

import io.reactivex.Single
import io.reactivex.rxkotlin.flatMapIterable
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceMetadataRepository
import javax.inject.Inject

class CreateTranslation @Inject constructor(
    private val collectionRepo: ICollectionRepository,
    private val resourceMetadataRepo: IResourceMetadataRepository
) {
    /**
     * Create derived translations for each source RC that has content in sourceMetadata subtree, optionally
     * limited to resourceId (if not null).
     */
    fun create(
        sourceMetadata: ResourceMetadata,
        targetLanguage: Language,
        resourceId: String? = null
    ): Single<ResourceMetadata> {
        // Find the source RC and its linked (help) RCs
        val sourceLinkedRcs = resourceMetadataRepo.getLinked(sourceMetadata)
            .toObservable()
            .flatMapIterable()
        val sourceAndLinkedRcs = sourceLinkedRcs.startWith(sourceMetadata)

        // If a resourceId filter is requested, apply it.
        val matchingRcs = when (resourceId) {
            null -> sourceAndLinkedRcs
            else -> sourceAndLinkedRcs.filter { resourceId == it.identifier }
        }

        // Create derived projects for each of the sources
        return matchingRcs
            .toList()
            .flatMap {
                collectionRepo.deriveTranslation(it, targetLanguage)
            }
    }
}
