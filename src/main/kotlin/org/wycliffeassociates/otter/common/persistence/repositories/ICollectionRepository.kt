package org.wycliffeassociates.otter.common.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.wycliffeassociates.otter.common.collections.tree.Tree
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.common.data.model.ResourceMetadata
import org.wycliffeassociates.resourcecontainer.ResourceContainer

interface ICollectionRepository : IRepository<Collection> {
    fun insert(collection: Collection): Single<Int>
    fun getRootProjects(): Single<List<Collection>>
    fun getRootSources(): Single<List<Collection>>
    fun getSource(project: Collection): Maybe<Collection>
    fun getBySlugAndContainer(slug: String, container: ResourceMetadata): Maybe<Collection>
    fun getChildren(collection: Collection): Single<List<Collection>>
    fun updateSource(collection: Collection, newSource: Collection): Completable
    fun updateParent(collection: Collection, newParent: Collection): Completable
    fun deriveProject(source: Collection, language: Language): Completable
    fun importResourceContainer(rc: ResourceContainer, tree: Tree, languageSlug: String): Completable
}