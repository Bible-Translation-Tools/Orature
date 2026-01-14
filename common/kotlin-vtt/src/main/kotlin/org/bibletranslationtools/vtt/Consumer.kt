package org.bibletranslationtools.vtt

/**
 * Represents an operation that accepts a single input argument and returns no result. Unlike most
 * other functional interfaces, Consumer is expected to operate via side-effects.
 */
interface Consumer<T> {
    /** Performs this operation on the given argument.  */
    fun accept(t: T)
}