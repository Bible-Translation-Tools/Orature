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