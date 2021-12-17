/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata

interface IResourceMetadataRepository : IRepository<ResourceMetadata> {
    fun exists(metadata: ResourceMetadata): Single<Boolean>
    fun get(metadata: ResourceMetadata): Single<ResourceMetadata>
    fun insert(metadata: ResourceMetadata): Single<Int>
    fun updateSource(metadata: ResourceMetadata, source: ResourceMetadata?): Completable
    fun getSource(metadata: ResourceMetadata): Maybe<ResourceMetadata>
    fun getAllSources(): Single<List<ResourceMetadata>>
    // These functions are commutative
    fun addLink(firstMetadata: ResourceMetadata, secondMetadata: ResourceMetadata): Completable
    fun removeLink(firstMetadata: ResourceMetadata, secondMetadata: ResourceMetadata): Completable
    fun getLinked(metadata: ResourceMetadata): Single<List<ResourceMetadata>>
}
