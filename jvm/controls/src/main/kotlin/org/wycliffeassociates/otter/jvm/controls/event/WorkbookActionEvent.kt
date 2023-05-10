package org.wycliffeassociates.otter.jvm.controls.event

import org.wycliffeassociates.otter.common.data.workbook.WorkbookDescriptor
import tornadofx.FXEvent

class WorkbookOpenEvent(val data: WorkbookDescriptor) : FXEvent()
class WorkbookExportEvent(val data: WorkbookDescriptor) : FXEvent()
class WorkbookDeleteEvent(val data: WorkbookDescriptor) : FXEvent()