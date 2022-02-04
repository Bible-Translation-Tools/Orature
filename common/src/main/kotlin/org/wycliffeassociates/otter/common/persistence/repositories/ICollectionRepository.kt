/**
 * Copyright (C) 2020-2022 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.common.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata

interface ICollectionRepository : IRepository<Collection> {
    fun insert(collection: Collection): Single<Int>
    fun getProject(id: Int): Maybe<Collection>
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
