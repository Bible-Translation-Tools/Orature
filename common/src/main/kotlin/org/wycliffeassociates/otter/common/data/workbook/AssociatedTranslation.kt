package org.wycliffeassociates.otter.common.data.workbook

import com.jakewharton.rxrelay2.BehaviorRelay

class AssociatedTranslation(
    val sourceRate: BehaviorRelay<Double>,
    val targetRate: BehaviorRelay<Double>
) {
    fun updateSourceRate(rate: Double) = sourceRate.accept(rate)

    fun updateTargetRate(rate: Double) = targetRate.accept(rate)
}
