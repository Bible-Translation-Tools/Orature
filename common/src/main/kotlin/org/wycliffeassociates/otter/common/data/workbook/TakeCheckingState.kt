package org.wycliffeassociates.otter.common.data.workbook

import org.wycliffeassociates.otter.common.data.primitives.CheckingStatus

data class TakeCheckingState(val status: CheckingStatus, val checksum: String? = null)
