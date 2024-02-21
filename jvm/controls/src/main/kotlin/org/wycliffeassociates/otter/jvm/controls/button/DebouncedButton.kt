/**
 * Copyright (C) 2020-2024 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.controls.button

import javafx.animation.Animation
import javafx.animation.PauseTransition
import javafx.event.EventTarget
import javafx.scene.control.Button
import javafx.scene.control.ButtonBase
import javafx.util.Duration
import tornadofx.*

private const val DEFAULT_COOL_DOWN_MILLIS = 500.0

/**
 * A custom button with the ability to debounce actions occurring within
 * a specified duration. It prevents repetitive actions triggered rapidly over
 * a short period of time by the user.
 *
 * Example use case: when double-clicking or multi-clicking is not desirable.
 */
class DebouncedButton : Button {
    private val coolDownTransition = PauseTransition(Duration.millis(DEFAULT_COOL_DOWN_MILLIS))

    constructor(text: String) : super(text)

    /**
     * @param coolDownMillis the duration during which the button omits action events.
     */
    constructor(text: String, coolDownMillis: Double) : super(text) {
        setCoolDownDuration(coolDownMillis)
    }

    fun setCoolDownDuration(millis: Double) {
        coolDownTransition.duration = Duration.millis(millis)
    }

    fun setOnAction(op: () -> Unit) {
        super.setOnAction {
            if (coolDownTransition.status != Animation.Status.RUNNING) {
                op()
                coolDownTransition.playFromStart()
            }
        }
    }

    fun action(op: () -> Unit) = this.setOnAction(op)
}

/**
 * A button that prevents repetitive actions triggered rapidly
 * over a short period of time.
 */
fun EventTarget.debouncedButton(
    text: String = "",
    coolDownMillis: Double = DEFAULT_COOL_DOWN_MILLIS,
    op: DebouncedButton.() -> Unit = {}
) = DebouncedButton(text, coolDownMillis).attachTo(this, op)


/**
 * Prevents repetitive actions triggered rapidly over a short period of time on a button.
 *
 * Only call this method **after** onActionProperty has been set. Otherwise, it will not take effect.
 *
 * Example:
 * ```
 * setOnAction {
 *   ...
 * }
 * debounce(700.0) // called after setOnAction()
 * ```
 */
fun ButtonBase.debounce(coolDownMillis: Double = DEFAULT_COOL_DOWN_MILLIS) {
    val actionHandler = this.onActionProperty().value ?: return
    val coolDownTransition = PauseTransition(Duration.millis(coolDownMillis))

    this.setOnAction {
        if (coolDownTransition.status != Animation.Status.RUNNING) {
            actionHandler.handle(it)
            coolDownTransition.playFromStart()
        }
    }
}