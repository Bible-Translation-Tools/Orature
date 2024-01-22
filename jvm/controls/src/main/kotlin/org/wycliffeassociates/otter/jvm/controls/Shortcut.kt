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
package org.wycliffeassociates.otter.jvm.controls

import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination

enum class Shortcut(val value: KeyCodeCombination) {
    RECORD(KeyCodeCombination(KeyCode.R, KeyCodeCombination.SHORTCUT_DOWN)),
    ADD_MARKER(KeyCodeCombination(KeyCode.D, KeyCodeCombination.SHORTCUT_DOWN)),
    PLAY_SOURCE(KeyCodeCombination(KeyCode.SPACE, KeyCodeCombination.SHORTCUT_DOWN)),
    PLAY_TARGET(KeyCodeCombination(KeyCode.SPACE, KeyCodeCombination.SHORTCUT_DOWN, KeyCodeCombination.SHIFT_DOWN)),
    GO_BACK(KeyCodeCombination(KeyCode.OPEN_BRACKET, KeyCodeCombination.SHORTCUT_DOWN)),
    UNDO(KeyCodeCombination(KeyCode.Z, KeyCodeCombination.SHORTCUT_DOWN)),
    REDO(KeyCodeCombination(KeyCode.Z, KeyCodeCombination.SHORTCUT_DOWN, KeyCodeCombination.SHIFT_DOWN))
}
