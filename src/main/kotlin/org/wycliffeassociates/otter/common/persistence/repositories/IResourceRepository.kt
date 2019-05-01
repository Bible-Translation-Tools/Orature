package org.wycliffeassociates.otter.common.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Observable
import org.wycliffeassociates.otter.common.data.model.Content
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.workbook.ResourceInfo

interface IResourceRepository : IRepository<Content> {
    fun getResources(collection: Collection, resourceInfo: ResourceInfo): Observable<Content>
    fun getResources(content: Content, resourceInfo: ResourceInfo): Observable<Content>
    fun getSubtreeResourceInfo(collection: Collection): List<ResourceInfo>
    fun getResourceInfo(content: Content): List<ResourceInfo>
    fun getResourceInfo(collection: Collection): List<ResourceInfo>
    fun linkToContent(resource: Content, content: Content, dublinCoreFk: Int): Completable
    fun linkToCollection(resource: Content, collection: Collection, dublinCoreFk: Int): Completable

    // Prepare SubtreeHasResources table
    fun calculateAndSetSubtreeHasResources(collectionId: Int)
}