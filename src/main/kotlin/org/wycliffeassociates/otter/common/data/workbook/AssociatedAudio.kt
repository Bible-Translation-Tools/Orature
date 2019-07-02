package org.wycliffeassociates.otter.common.data.workbook

import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.ReplayRelay
import io.reactivex.Observable
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

    fun getAllTakes(): Array<Take> = takes.getValues(emptyArray())

    fun getNewTakeNumber(): Single<Int> =
        Single.just(
            getAllTakes()
                .maxBy { it.number }
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
