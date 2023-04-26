package org.wycliffeassociates.otter.jvm.controls.event

import org.wycliffeassociates.otter.common.data.workbook.WorkbookInfo
import tornadofx.FXEvent

class WorkbookOpenEvent(val data: WorkbookInfo) : FXEvent()
class WorkbookExportEvent(val data: WorkbookInfo) : FXEvent()
class WorkbookDeleteEvent(val data: WorkbookInfo) : FXEvent()