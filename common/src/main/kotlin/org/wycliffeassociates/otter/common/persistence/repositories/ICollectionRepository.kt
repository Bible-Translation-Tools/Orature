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
    fun getProjectBySlugAndMetadata(slug: String, metadata: ResourceMetadata): Single<Collection>
    fun updateSource(collection: Collection, newSource: Collection): Completable
    fun updateParent(collection: Collection, newParent: Collection): Completable
    fun deriveProject(
        sourceMetadatas: List<ResourceMetadata>,
        sourceCollection: Collection,
        language: Language
    ): Single<Collection>

    /**
     * Deletes a derived project. This should remove all associated derived collections, content, and takes associated
     * with the project. Any backing derived manifests will have the project removed, and if the project is the last
     * referenced, then the manifest will be deleted.
     *
     * @param project the Collection corresponding to the project (Book) being deleted
     * @param deleteAudio flag for if take files referenced by Take entries should be removed from the filesystem
     */
    fun deleteProject(project: Collection, deleteAudio: Boolean): Completable

    /**
     * Deletes a derived project's resources. This should remove all associated takes.
     * Because takes are not attached to a derived project, but instead the source, the collections and content entries
     * will persist.
     *
     * @param project the Collection corresponding to the project (Book) being deleted (Note: this is NOT the resource!)
     * @param deleteAudio flag for if take files referenced by Take entries should be removed from the filesystem
     */
    fun deleteResources(project: Collection, deleteAudio: Boolean): Completable
}
