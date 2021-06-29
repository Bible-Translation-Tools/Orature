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
package org.wycliffeassociates.otter.common.domain.content

import io.reactivex.Completable
import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.primitives.Content
import org.wycliffeassociates.otter.common.data.primitives.Take
import org.wycliffeassociates.otter.common.persistence.repositories.IContentRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ITakeRepository

class AccessTakes(
    private val contentRepo: IContentRepository,
    private val takeRepo: ITakeRepository
) {
    fun getByContent(content: Content): Single<List<Take>> {
        return takeRepo.getByContent(content)
    }

    fun setSelectedTake(content: Content, selectedTake: Take?): Completable {
        content.selectedTake = selectedTake
        return contentRepo.update(content)
    }

    fun setTakePlayed(take: Take, played: Boolean): Completable {
        take.played = played
        return takeRepo.update(take)
    }

    fun delete(take: Take): Completable {
        return takeRepo.delete(take)
    }

    fun getTakeCount(content: Content): Single<Int> {
        return takeRepo
            .getByContent(content)
            .map { it.size }
    }
}
