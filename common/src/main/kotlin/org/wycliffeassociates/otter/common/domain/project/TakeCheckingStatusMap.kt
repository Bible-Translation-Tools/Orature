package org.wycliffeassociates.otter.common.domain.project

import org.wycliffeassociates.otter.common.data.workbook.TakeCheckingState

/**
 * Serializable map of take file paths (internal project directory) and its corresponding checking status.
 */
typealias TakeCheckingStatusMap = Map<String, TakeCheckingState>
