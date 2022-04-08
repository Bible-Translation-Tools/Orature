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

import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.ComboBox
import javafx.scene.control.ListView
import javafx.scene.control.skin.VirtualFlow
import javafx.scene.control.TextArea
import javafx.scene.control.skin.ListViewSkin
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import kotlin.reflect.KClass

inline fun <reified T: Node> Node.findChild(): Node? = findChildren<T>().firstOrNull()
inline fun <reified T: Node> Node.findChildren(): List<Node> = findChildren(T::class)
fun <T: Node> Node.findChildren(type: KClass<T>): List<Node> {
    if (this !is Parent) return listOf()

    val list = this.childrenUnmodifiable
        .filter { type.isInstance(it) }
        .filter { it.isVisible }
        .toMutableList()

    for (node: Node in this.childrenUnmodifiable) {
        (node as? Parent)?.findChildren(type)?.let {
            list.addAll(it)
        }
    }

    return list
}

fun Node.simulateKeyPress(
    key: KeyCode,
    shiftDown: Boolean = false,
    controlDown: Boolean = false,
    altDown: Boolean = false,
    metaDown: Boolean = false
) {
    fireEvent(
        KeyEvent(
            KeyEvent.KEY_PRESSED,
            "",
            "",
            key,
            shiftDown,
            controlDown,
            altDown,
            metaDown
        )
    )
}

/**
 * Overrides ComboBox's default keyboard events
 * And triggers action only when value has changed
 * @param action Action to invoke when value has changed
 */
fun <T> ComboBox<T>.overrideDefaultKeyEventHandler(action: (T) -> Unit = {}) {
    var oldValue: T? = null

    this.addEventFilter(MouseEvent.MOUSE_PRESSED) {
        oldValue = this.value
        setOnHidden {
            if (oldValue != this.value) {
                action(this.value)
            }
        }
    }

    this.addEventFilter(KeyEvent.KEY_RELEASED) {
        onHiddenProperty().set(null)

        when (it.code) {
            KeyCode.ENTER, KeyCode.SPACE -> {
                if (this.isShowing) return@addEventFilter

                if (oldValue != null && oldValue != this.value) {
                    action(this.value)
                }
                oldValue = this.value
                it.consume()
            }
            KeyCode.ESCAPE -> {
                this.value = oldValue
                it.consume()
            }
        }
    }

    this.addEventFilter(KeyEvent.KEY_PRESSED) {
        when (it.code) {
            KeyCode.ENTER, KeyCode.SPACE -> {
                it.consume()
                this.show()
            }
            KeyCode.RIGHT, KeyCode.DOWN -> {
                it.consume()
                this.simulateKeyPress(KeyCode.TAB)
            }
            KeyCode.LEFT, KeyCode.UP -> {
                it.consume()
                this.simulateKeyPress(KeyCode.TAB, shiftDown = true)
            }
            KeyCode.TAB -> {
                if (this.isShowing) it.consume()
            }
        }
    }
}

fun <T> ListView<T>.enableScrollByKey(
    smallDelta: Double = 20.0,
    largeDelta: Double = 500.0
) {
    addEventFilter(KeyEvent.KEY_PRESSED) { keyEvent ->
        val flow = childrenUnmodifiable
            .find { it is VirtualFlow<*> } as VirtualFlow<*>

        when (keyEvent.code) {
            KeyCode.UP -> {
                flow.scrollPixels(-smallDelta)
                keyEvent.consume()
            }
            KeyCode.DOWN -> {
                flow.scrollPixels(smallDelta)
                keyEvent.consume()
            }
            KeyCode.PAGE_UP -> {
                flow.scrollPixels(-largeDelta)
                keyEvent.consume()
            }
            KeyCode.PAGE_DOWN -> {
                flow.scrollPixels(largeDelta)
                keyEvent.consume()
            }
        }
    }
}
/**
 * Overrides TextArea's default keyboard events
 * And triggers action only when Shift + Enter is pressed
 * @param action Action to invoke when Shift + Enter is pressed
 */
fun TextArea.overrideDefaultKeyEventHandler(action: (String) -> Unit = {}) {
    this.addEventFilter(KeyEvent.KEY_RELEASED) {
        if (it.code == KeyCode.ENTER && it.isShiftDown) {
            it.consume()
            action(this.text ?: "")
            this.simulateKeyPress(KeyCode.TAB, controlDown = true)
        }
    }
    this.addEventFilter(KeyEvent.KEY_PRESSED) {
        when (it.code) {
            KeyCode.TAB -> {
                if (!it.isControlDown && !it.isShiftDown) {
                    it.consume()
                    this.simulateKeyPress(
                        KeyCode.TAB,
                        controlDown = !it.isShiftDown,
                        shiftDown = it.isShiftDown
                    )
                }
            }
        }
    }
}

fun <T> ListView<T>.overrideDefaultKeyEventHandler(action: (KeyCode) -> Unit = {}) {
    this.addEventFilter(KeyEvent.KEY_PRESSED) {
        when (it.code) {
            KeyCode.UP, KeyCode.DOWN -> {
                it.consume()
                action(it.code)
            }
        }
    }
}

fun <T> ListView<T>.virtualFlow(): VirtualFlow<*> {
    return (this.skin as ListViewSkin<*>).children.first() as VirtualFlow<*>
}
