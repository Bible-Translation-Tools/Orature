package org.wycliffeassociates.otter.common.data.workbook

import com.jakewharton.rxrelay2.Relay

class AssociatedTranslation(
    val sourceRate: Relay<Double>,
    val targetRate: Relay<Double>
) {
    fun updateSourceRate(rate: Double) = sourceRate.accept(rate)

    fun updateTargetRate(rate: Double) = targetRate.accept(rate)
}
