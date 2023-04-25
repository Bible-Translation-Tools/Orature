package org.wycliffeassociates.otter.jvm.controls.event

import org.wycliffeassociates.otter.common.data.workbook.WorkbookStatus
import tornadofx.FXEvent

class WorkbookOpenEvent(val data: WorkbookStatus) : FXEvent()
class WorkbookExportEvent(val data: WorkbookStatus) : FXEvent()
class WorkbookDeleteEvent(val data: WorkbookStatus) : FXEvent()