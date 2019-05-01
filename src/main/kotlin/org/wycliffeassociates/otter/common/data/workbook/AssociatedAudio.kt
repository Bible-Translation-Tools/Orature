package org.wycliffeassociates.otter.common.data.workbook

import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.ReplayRelay

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
    val selected: BehaviorRelay<TakeHolder>
)

data class TakeHolder(val value: Take?)
