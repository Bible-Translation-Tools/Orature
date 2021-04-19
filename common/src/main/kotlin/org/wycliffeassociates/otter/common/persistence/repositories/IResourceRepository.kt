package org.wycliffeassociates.otter.common.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Observable
import org.wycliffeassociates.otter.common.data.primitives.Content
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata

interface IResourceRepository : IRepository<Content> {
    fun getResources(collection: Collection, resourceMetadata: ResourceMetadata): Observable<Content>
    fun getResources(content: Content, resourceMetadata: ResourceMetadata): Observable<Content>
    fun getSubtreeResourceMetadata(collection: Collection): List<ResourceMetadata>
    fun getResourceMetadata(content: Content): List<ResourceMetadata>
    fun getResourceMetadata(collection: Collection): List<ResourceMetadata>
    fun linkToContent(resource: Content, content: Content, dublinCoreFk: Int): Completable
    fun linkToCollection(resource: Content, collection: Collection, dublinCoreFk: Int): Completable

    // Prepare SubtreeHasResources table
    fun calculateAndSetSubtreeHasResources(collectionId: Int)
}
