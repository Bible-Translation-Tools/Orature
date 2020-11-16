package org.wycliffeassociates.otter.common.domain.collections

import io.reactivex.Single
import io.reactivex.rxkotlin.flatMapIterable
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceMetadataRepository

class CreateProject(
    private val collectionRepo: ICollectionRepository,
    private val resourceMetadataRepo: IResourceMetadataRepository
) {
    /**
     * Create derived collections for each source RC that has content in sourceProject's subtree, optionally
     * limited to resourceId (if not null).
     */
    fun create(
        sourceProject: Collection,
        targetLanguage: Language,
        resourceId: String? = null
    ): Single<Collection> {
        // Find the source RC and its linked (help) RCs
        val sourceRc = sourceProject.resourceContainer
            ?: throw NullPointerException("Source project has no metadata")
        val sourceLinkedRcs = resourceMetadataRepo.getLinked(sourceRc)
            .toObservable()
            .flatMapIterable()
        val sourceAndLinkedRcs = sourceLinkedRcs.startWith(sourceRc)

        // If a resourceId filter is requested, apply it.
        val matchingRcs = when (resourceId) {
            null -> sourceAndLinkedRcs
            else -> sourceAndLinkedRcs.filter { resourceId == it.identifier }
        }

        // Create derived projects for each of the sources
        return matchingRcs
            .toList()
            .flatMap {
                collectionRepo.deriveProject(it, sourceProject, targetLanguage)
            }
    }
}
