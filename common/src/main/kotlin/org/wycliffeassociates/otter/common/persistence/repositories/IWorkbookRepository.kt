package org.wycliffeassociates.otter.common.persistence.repositories

import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Take
import org.wycliffeassociates.otter.common.data.workbook.Book
import org.wycliffeassociates.otter.common.data.workbook.Workbook

interface IWorkbookRepository {
    fun get(source: Collection, target: Collection): Workbook
    fun getSoftDeletedTakes(book: Book): Single<List<Take>>
    fun getProjects(): Single<List<Workbook>>
}
