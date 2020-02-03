package org.wycliffeassociates.otter.common.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.common.data.model.ResourceMetadata

interface ICollectionRepository : IRepository<Collection> {
    fun insert(collection: Collection): Single<Int>
    fun getDerivedProjects(): Single<List<Collection>>
    fun getSourceProjects(): Single<List<Collection>>
    fun getRootSources(): Single<List<Collection>>
    fun getSource(project: Collection): Maybe<Collection>
    fun getChildren(collection: Collection): Single<List<Collection>>
    fun updateSource(collection: Collection, newSource: Collection): Completable
    fun updateParent(collection: Collection, newParent: Collection): Completable
    fun deriveProject(
        sourceMetadata: ResourceMetadata,
        sourceCollection: Collection,
        language: Language
    ): Single<Collection>
    fun deleteProject(project: Collection, deleteAudio: Boolean): Completable
}