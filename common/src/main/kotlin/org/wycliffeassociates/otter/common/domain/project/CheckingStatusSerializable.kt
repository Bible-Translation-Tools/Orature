package org.wycliffeassociates.otter.common.domain.project

import org.wycliffeassociates.otter.common.data.primitives.CheckingStatus

data class CheckingStatusSerializable(val checking: CheckingStatus, val checksum: String? = null)
typealias TakeCheckingStatusMap = Map<String, CheckingStatusSerializable>