/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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
package org.wycliffeassociates.otter.common

import org.junit.Assert

/**
 * Applies the given transformation to each element in the keyset of the map, and uses [doAssertEquals]
 * to compare the result of each transform with the corresponding expected value in the valueset.
 *
 * This assumes that the map keys represent the input to a test function, and the values represent the
 * corresponding expected output.
 *
 * @param transform The transformation to apply to each key in the keyset. This is the function under test.
 */
fun <T, V> Map<T, V>.assertEqualsForEach(transform: (T) -> V) {
    var allPassed = true
    forEach {
        val output = transform(it.key)
        val expected = it.value
        allPassed = doAssertEquals(expected, output) && allPassed
    }
    if (!allPassed) throw AssertionError()
}
/**
 * Wrapper function for [org.junit.Assert.assertEquals] that catches any thrown [AssertionError].
 * If an error is caught, the expected output and actual output will be printed
 *
 * @return The boolean result of the comparison
 */
fun <T> doAssertEquals(expected: T, output: T): Boolean {
    return try {
        Assert.assertEquals(expected, output)
        true
    } catch (e: AssertionError) {
        println("Expected: $expected, Output: $output")
        false
    }
}
