package org.wycliffeassociates.otter.common.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.model.ProjectCollection
import org.wycliffeassociates.otter.common.data.model.SourceCollection

interface ISourceRepository : IRepository<SourceCollection> {
    fun insert(source: SourceCollection): Single<Int>
    fun getAllRoot(): Single<List<SourceCollection>>
    fun getByProjectCollection(project: ProjectCollection): Maybe<SourceCollection>
    fun getChildren(source: SourceCollection): Single<List<SourceCollection>>
    fun updateParent(source: SourceCollection, newParent: SourceCollection): Completable
}