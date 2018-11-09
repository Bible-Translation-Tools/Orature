package org.wycliffeassociates.otter.jvm.app.extensions

import javafx.beans.value.ObservableValue
import tornadofx.onChange

// Map an observable property value so the same code will be run initially
// to set up and on change
fun <T: Any> ObservableValue<T>.listen(listener: (T?) -> Unit) {
    listener(value)
    onChange { listener(value) }
}