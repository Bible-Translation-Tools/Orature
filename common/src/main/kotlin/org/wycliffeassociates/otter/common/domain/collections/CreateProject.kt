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
        // Find the source RC and its linked (help) RCs
        val sourceRc = sourceProject.resourceContainer
            ?: throw NullPointerException("Source project has no metadata")
        val sourceLinkedRcs = resourceMetadataRepo.getLinked(sourceRc)
            .toObservable()
            .concatMapIterable()
        val sourceAndLinkedRcs = sourceLinkedRcs
            .startWith(sourceRc)
            .filter { resourceId == null || resourceId == it.identifier }

        // Create derived projects for each of the sources
        val derivedProjects = sourceAndLinkedRcs
            .concatMapSingle {
                collectionRepo.deriveProject(it, sourceProject, targetLanguage)
            }
            .cache()

        linkDerivedRcs(derivedProjects)

        return derivedProjects
    }

    /**
     *  Link RC-derived-from-sourceRC to RCs-derived-from-RCs-linked-to-sourceRC.
     *  @param derivedProjects a CACHED observable, with the main project as the first element
     */
    private fun linkDerivedRcs(derivedProjects: Observable<Collection>) {
        val mainDerived = derivedProjects.firstElement().cache()
        val linkDerived = derivedProjects.skip(1)

        linkDerived.subscribe {
            it.resourceContainer?.let { linkingRc ->
                mainDerived.blockingGet().resourceContainer?.let { mainRc ->

                    resourceMetadataRepo.addLink(mainRc, linkingRc).blockingAwait()
                }
            }
        }
    }
}