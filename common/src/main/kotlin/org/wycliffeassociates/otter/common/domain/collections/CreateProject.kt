package org.wycliffeassociates.otter.common.domain.collections

import io.reactivex.Observable
import io.reactivex.rxkotlin.concatMapIterable
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
    ): Observable<Collection> {
        val sourceRc = sourceProject.resourceContainer
            ?: throw NullPointerException("Source project has no metadata")

        val helpRcs = resourceMetadataRepo.getLinked(sourceRc)
            .toObservable()
            .concatMapIterable()

        val rcs = helpRcs
            .startWith(sourceRc)
            .filter { resourceId == null || resourceId == it.identifier }

        val derivedProjects = rcs.concatMapSingle {
            collectionRepo.deriveProject(it, sourceProject, targetLanguage)
        }.cache()

        // Link RC-derived-from-sourceRC to RCs-derived-from-RCs-linked-to-sourceRC
        val mainDerived = derivedProjects.firstElement().cache()
        derivedProjects.subscribe {
            val linkingRc = it.resourceContainer
            val mainRc = mainDerived.blockingGet().resourceContainer
            if (mainRc != linkingRc && mainRc != null && linkingRc != null) {
                resourceMetadataRepo.addLink(mainRc, linkingRc).blockingAwait()
            }
        }

        return derivedProjects
    }
}