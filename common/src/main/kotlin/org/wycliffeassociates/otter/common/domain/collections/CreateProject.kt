package org.wycliffeassociates.otter.common.domain.collections

import io.reactivex.Observable
import io.reactivex.rxkotlin.concatMapIterable
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceRepository

class CreateProject(
    private val collectionRepo: ICollectionRepository,
    private val resourceRepo: IResourceRepository
) {
    /**
     * Create derived collections for each source RC that has content in sourceProject's subtree, optionally
     * limited to resourceId (if not null).
     */
    fun create(sourceProject: Collection, targetLanguage: Language, resourceId: String? = null): Observable<Collection> {
        val sourceRc = sourceProject.resourceContainer
            ?: throw NullPointerException("Source project has no metadata")

        val helpRcs = Observable.fromCallable {
            resourceRepo.getSubtreeResourceMetadata(sourceProject)
        }.concatMapIterable()

        val rcs = helpRcs
            .startWith(sourceRc)
            .filter { resourceId == null || resourceId == it.identifier }

        return rcs.concatMapSingle {
            collectionRepo.deriveProject(it, sourceProject, targetLanguage)
        }
    }
}