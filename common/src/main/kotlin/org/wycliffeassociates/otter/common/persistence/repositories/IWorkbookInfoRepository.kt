package org.wycliffeassociates.otter.common.persistence.repositories

import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.workbook.Translation
import org.wycliffeassociates.otter.common.data.workbook.WorkbookInfo

interface IWorkbookInfoRepository {
    fun getProjects(translation: Translation): Single<List<WorkbookInfo>>
}