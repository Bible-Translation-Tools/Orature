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
package org.wycliffeassociates.otter.jvm.utils

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Observable
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.control.Tab
import javafx.scene.layout.Pane
import tornadofx.onChange
import java.lang.IllegalStateException
import tornadofx.*

interface ListenerDisposer {
    fun dispose()
}

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
 * Sets up an on change listener to run [op] function
 * @param op the function to run when observable value is changed
 * @return ListenerDisposer
 */
fun <T> ObservableValue<T>.onChangeWithDisposer(op: (T?) -> Unit): ListenerDisposer {
    val listener = ChangeListener<T> { _, _, newValue -> op(newValue) }
    addListener(listener)
    return object: ListenerDisposer {
        override fun dispose() {
            removeListener(listener)
        }
    }
}

/**
 * Sets up an on change listener to run [op] function
 * @param op the function to run when observable list is changed
 * @return ListenerDisposer
 */
fun <T> ObservableList<T>.onChangeWithDisposer(op: (ListChangeListener.Change<out T>) -> Unit): ListenerDisposer {
    val listener = ListChangeListener<T> { op(it) }
    addListener(listener)
    return object: ListenerDisposer {
        override fun dispose() {
            removeListener(listener)
        }
    }
}

/**
 * Runs the given operation now and also calls [onChangeWithDisposer] with the given operation to set up an
 * on change listener
 * @param op the function to run when observable value is changed
 * @return ListenerDisposer
 */
fun <T> ObservableValue<T>.onChangeAndDoNowWithDisposer(op: (T?) -> Unit): ListenerDisposer {
    op(this.value)
    return this.onChangeWithDisposer {
        op(it)
    }
}

/**
 * Runs an [op] function when tab is selected
 * @param op the function to run when tab is selected
 * @return ListenerDisposer
 */
fun Tab.whenSelectedWithDisposer(op: () -> Unit): ListenerDisposer {
    return selectedProperty().onChangeWithDisposer { if (it == true) op() }
}

/**
 * Bind the children of this Layout node to the given observable list of items by converting them into nodes via
 * the given converter function. Changes to the source list will be reflected in the children list of this layout node.
 * @return ListenerDisposer
 */
fun <T> EventTarget.bindChildrenWithDisposer(sourceList: ObservableList<T>, converter: (T) -> Node): ListenerDisposer {
    val listener = this.bindChildren(sourceList, converter)
    return object: ListenerDisposer {
        override fun dispose() {
            sourceList.removeListener(listener)
        }
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
