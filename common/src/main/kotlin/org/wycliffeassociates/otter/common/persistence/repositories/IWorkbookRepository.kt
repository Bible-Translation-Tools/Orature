package org.wycliffeassociates.otter.common.persistence.repositories

import io.reactivex.Maybe
import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.primitives.Take
import org.wycliffeassociates.otter.common.data.workbook.Book
import org.wycliffeassociates.otter.common.data.workbook.Workbook

interface IWorkbookRepository {
    fun get(source: Collection, target: Collection): Workbook
    fun getSoftDeletedTakes(book: Book): Single<List<Take>>
    fun getProjects(): Single<List<Workbook>>
    fun getProjects(target: ResourceMetadata): Single<List<Workbook>>
    fun getWorkbook(project: Collection): Maybe<Workbook>
    fun closeWorkbook(workbook: Workbook)
}
