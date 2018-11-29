package org.wycliffeassociates.otter.common.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.model.Content
import org.wycliffeassociates.otter.common.data.model.Collection

interface IResourceRepository : IRepository<Content> {
    // Get resources for a content
    fun getByContent(content: Content): Single<List<Content>>
    // Get resources for a collection
    fun getByCollection(collection: Collection): Single<List<Content>>
    // Link/Unlink
    fun linkToContent(resource: Content, content: Content): Completable
    fun unlinkFromContent(resource: Content, content: Content): Completable
    fun linkToCollection(resource: Content, collection: Collection): Completable
    fun unlinkFromCollection(resource: Content, collection: Collection): Completable
}