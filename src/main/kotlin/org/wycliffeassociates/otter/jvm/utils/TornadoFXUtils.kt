package org.wycliffeassociates.otter.jvm.utils

import com.github.thomasnield.rxkotlinfx.toObservableChangesNonNull
import javafx.beans.value.ObservableValue
import tornadofx.onChange
import tornadofx.onChangeOnce

/**
 * Runs the given operation now and also calls tornadofx's [onChange] with the given operation to set up an
 * on change listener
 */
fun <T> ObservableValue<T>.onChangeAndDoNow(op: (T?) -> Unit) {
    op(this.value)
    this.onChange {
        op(it)
    }
}

fun <T> ObservableValue<T>.waitForNotNull(op: (T) -> Unit) {
    this.value?.let {
        op(it)
    } ?: this.onChangeOnce {
        this.waitForNotNull(op)
    }
}
