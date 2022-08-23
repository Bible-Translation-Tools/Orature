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
