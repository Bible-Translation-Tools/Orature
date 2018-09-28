package org.wycliffeassociates.otter.common.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.model.ProjectCollection
import org.wycliffeassociates.otter.common.data.model.SourceCollection

interface IProjectRepository : IRepository<ProjectCollection> {
    fun getAllRoot(): Single<List<ProjectCollection>>
    fun getChildren(project: ProjectCollection): Single<List<ProjectCollection>>
    fun updateSource(project: ProjectCollection, newSource: SourceCollection): Completable
    fun updateParent(project: ProjectCollection, newParent: ProjectCollection): Completable
}