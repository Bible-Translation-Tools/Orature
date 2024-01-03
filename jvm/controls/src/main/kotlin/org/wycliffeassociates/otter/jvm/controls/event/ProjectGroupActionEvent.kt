package org.wycliffeassociates.otter.jvm.controls.event

import org.wycliffeassociates.otter.common.data.workbook.WorkbookDescriptor
import tornadofx.FXEvent

class ProjectContributorsEvent(val books: List<WorkbookDescriptor>) : FXEvent()

class ProjectGroupDeleteEvent(val books: List<WorkbookDescriptor>) : FXEvent()
