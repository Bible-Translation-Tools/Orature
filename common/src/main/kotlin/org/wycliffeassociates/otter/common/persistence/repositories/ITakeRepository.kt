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
import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.primitives.Content
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.data.primitives.Take

interface ITakeRepository : IRepository<Take> {
    fun insertForContent(take: Take, content: Content): Single<Int>
    fun getByContent(content: Content, includeDeleted: Boolean = false): Single<List<Take>>
    fun removeNonExistentTakes(): Completable
    fun markDeleted(take: Take): Completable
    fun getSoftDeletedTakes(project: Collection): Single<List<Take>>
    fun deleteExpiredTakes(expiry: Int = 0): Completable
    fun getByCollection(chapterCollection: Collection, includeDeleted: Boolean = false): Single<List<Take>>
    fun getContentType(take: Take): Single<ContentType>
}
