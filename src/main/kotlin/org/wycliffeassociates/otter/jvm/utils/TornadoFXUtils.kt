package org.wycliffeassociates.otter.jvm.utils

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Observable
import javafx.application.Platform
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import tornadofx.onChange
import java.lang.IllegalStateException

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

/**
 * Runs the given operation now and also calls tornadofx's [onChange] with the given operation to set up an
 * on change listener
 */
fun <T> ObservableList<T>.onChangeAndDoNow(op: (List<T>) -> Unit) {
    op(this)
    this.onChange {
        op(it.list)
    }
}

/**
 * Calls tornadofx's [observeOnFx] if JavaFX is running. Using this method instead of [observeOnFx] directly
 * makes it safe to run unit tests when the FX platform is not running.
 */
fun <T> Observable<T>.observeOnFxSafe(): Observable<T> {
    return when (IsFXInitialized.isInitialized) {
        true -> observeOnFx()
        false -> this
    }
}

private object IsFXInitialized {
    // Since object declarations are initialized lazily, isInitialized will be computed when
    // IsFXInitialized is accessed for the first time
    val isInitialized: Boolean = testIsInitialized()

    private fun testIsInitialized(): Boolean {
        try {
            Platform.runLater{}
        } catch (e: IllegalStateException) {
            return false
        }
        return true
    }
}
