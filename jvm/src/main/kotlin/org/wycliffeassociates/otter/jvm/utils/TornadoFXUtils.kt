package org.wycliffeassociates.otter.jvm.utils

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Observable
import javafx.application.Platform
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.layout.Pane
import tornadofx.onChange
import java.lang.IllegalStateException
import tornadofx.*

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
 * Binds this [Pane]'s child to the node in [observableNode]. This method sets up a contract that
 * the [Pane] can only contain at most one child: the value in [observableNode]. If the value of
 * [observableNode] is null, the [Pane] will contain no children. If any other child is added to this
 * [Pane], an [IllegalStateException] is thrown.
 */
fun <T : Node> Pane.bindSingleChild(observableNode: ObservableValue<T>) {
    observableNode.onChangeAndDoNow { node ->
        clear()
        node?.let { add(node) }
    }
    children.onChange { change ->
        while (change.next()) {
            if (change.addedSubList.any { it != observableNode.value }) {
                throw IllegalStateException(
                    "bindSingleChild() contract ensures that the only child that " +
                            "can be added to this pane is the one specified by the contract"
                )
            }
        }
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
            Platform.runLater {}
        } catch (e: IllegalStateException) {
            return false
        }
        return true
    }
}
