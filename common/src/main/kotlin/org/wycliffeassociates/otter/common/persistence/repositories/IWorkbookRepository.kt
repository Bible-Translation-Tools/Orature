package org.wycliffeassociates.otter.common.persistence.repositories

import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.workbook.Workbook

interface IWorkbookRepository {
    fun get(source: Collection, target: Collection): Workbook
}