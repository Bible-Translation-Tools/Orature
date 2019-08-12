package org.wycliffeassociates.otter.jvm.utils

import javafx.beans.value.ObservableValue
import tornadofx.onChange

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
