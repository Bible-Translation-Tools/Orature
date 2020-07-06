package org.wycliffeassociates.otter.common.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Content

interface IContentRepository : IRepository<Content> {
    // Insert for a collection
    fun insertForCollection(content: Content, collection: Collection): Single<Int>
    // Get all the chunks for a collection
    fun getByCollection(collection: Collection): Single<List<Content>>
    // Get the collection meta-chunk
    fun getCollectionMetaContent(collection: Collection): Single<Content>
    // Get sources this content is derived from
    fun getSources(content: Content): Single<List<Content>>
    // Update the sources for a content
    fun updateSources(content: Content, sourceContents: List<Content>): Completable
}
