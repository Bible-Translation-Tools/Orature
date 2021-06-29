/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.common.data.workbook

import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.ReplayRelay
import io.reactivex.Single

data class AssociatedAudio(
    /**
     *  This will cache and emit all Takes. As takes are created, this will emit more items.
     *  The UX may push new items here for propagation, and the persistence layer should respond by storing them.
     */
    val takes: ReplayRelay<Take>,

    /**
     *  This will cache and emit the latest value.
     *  The UX may push updates here for propagation, and the persistence layer should respond by storing them.
     */
    val selected: BehaviorRelay<TakeHolder> = BehaviorRelay.createDefault(TakeHolder.empty)
) {
    fun insertTake(take: Take) = takes.accept(take)

    fun selectTake(take: Take?) = selected.accept(TakeHolder(take))

    fun getAllTakes(): Array<Take> = takes.getValues(emptyArray())

    fun getNewTakeNumber(): Single<Int> =
        Single.just(
            getAllTakes()
                .maxByOrNull { it.number }
                ?.number
                ?.plus(1)
                ?: 1
        )
}

data class TakeHolder(val value: Take?) {
    companion object {
        val empty = TakeHolder(null)
    }
}
