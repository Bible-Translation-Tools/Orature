package org.wycliffeassociates.otter.common.utils

import io.reactivex.Observable

/**
 * Apply the given function as with `map()`, but remove any resulting nulls from the
 * observable stream.
 *
 * This is how `observable.map(f).filter { it != null }` should behave, but it keeps
 * nulls from ever appearing in an Observable, which would cause a crash in that
 * two-step map/filter example.
 */
fun <T, R : Any> Observable<T>.mapNotNull(f: (T) -> R?): Observable<R> =
    concatMapIterable { listOfNotNull(f(it)) }
