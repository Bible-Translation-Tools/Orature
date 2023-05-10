package org.wycliffeassociates.otter.jvm.controls.event

import org.wycliffeassociates.otter.common.data.workbook.ProjectInfo
import tornadofx.FXEvent

class WorkbookOpenEvent(val data: ProjectInfo) : FXEvent()
class WorkbookExportEvent(val data: ProjectInfo) : FXEvent()
class WorkbookDeleteEvent(val data: ProjectInfo) : FXEvent()