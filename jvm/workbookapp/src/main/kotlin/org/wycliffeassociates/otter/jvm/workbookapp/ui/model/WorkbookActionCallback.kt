package org.wycliffeassociates.otter.jvm.workbookapp.ui.model

import org.wycliffeassociates.otter.common.data.workbook.Workbook

interface WorkbookActionCallback {
    fun openWorkbook(workbook: Workbook)
    fun deleteWorkbook(workbook: Workbook)
    fun exportWorkbook(workbook: Workbook)
}