package org.wycliffeassociates.otter.common.domain.project

import org.wycliffeassociates.otter.common.data.primitives.CheckingStatus

internal data class CheckingStatusSerializable(val checking: CheckingStatus, val checksum: String? = null)
internal typealias TakeCheckingStatusMap = Map<String, CheckingStatusSerializable>