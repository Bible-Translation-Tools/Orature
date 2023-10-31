package org.wycliffeassociates.otter.common.domain.project

import org.wycliffeassociates.otter.common.data.primitives.CheckingStatus

internal data class TakeCheckingSerializable(val takePath: String, val checking: CheckingStatus, val checksum: String? = null)