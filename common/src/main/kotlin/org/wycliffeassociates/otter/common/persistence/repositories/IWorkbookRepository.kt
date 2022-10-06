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

import io.reactivex.Maybe
import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.Take
import org.wycliffeassociates.otter.common.data.workbook.Book
import org.wycliffeassociates.otter.common.data.workbook.Translation
import org.wycliffeassociates.otter.common.data.workbook.Workbook

interface IWorkbookRepository {
    fun get(source: Collection, target: Collection): Workbook

    /**
     * Returns a list (observable wrapped) containing the takes of the given book
     * that are marked to be deleted.
     */
    fun getSoftDeletedTakes(book: Book): Single<List<Take>>
    fun getProjects(): Single<List<Workbook>>
    fun getProjects(translation: Translation): Single<List<Workbook>>
    fun getWorkbook(project: Collection): Maybe<Workbook>

    /**
     * Closes the given workbook and clean up resources.
     */
    fun closeWorkbook(workbook: Workbook)
}
