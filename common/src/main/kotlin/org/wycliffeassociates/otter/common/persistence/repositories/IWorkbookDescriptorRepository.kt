package org.wycliffeassociates.otter.common.persistence.repositories

import io.reactivex.Maybe
import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.workbook.WorkbookDescriptor

interface IWorkbookDescriptorRepository {
    fun getById(id: Int): Maybe<WorkbookDescriptor>
    fun getAll(): Single<List<WorkbookDescriptor>>
}