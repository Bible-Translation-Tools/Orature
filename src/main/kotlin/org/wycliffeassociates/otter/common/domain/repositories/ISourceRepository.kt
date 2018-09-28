package org.wycliffeassociates.otter.common.domain.repositories

import io.reactivex.Completable
import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.model.ProjectCollection
import org.wycliffeassociates.otter.common.data.model.SourceCollection

interface ISourceRepository : IRepository<SourceCollection> {
    fun getAllRoot(): Single<List<SourceCollection>>
    fun getByProjectCollection(project: ProjectCollection): Single<SourceCollection>
    fun getChildren(source: SourceCollection): Single<List<SourceCollection>>
    fun updateParent(source: SourceCollection, newParent: SourceCollection): Completable
}