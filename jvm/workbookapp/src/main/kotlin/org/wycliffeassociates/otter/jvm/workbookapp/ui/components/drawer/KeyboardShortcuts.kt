package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer

import javafx.geometry.HPos
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.text.TextAlignment
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.Shortcut
import tornadofx.*

class KeyboardShortcuts : VBox() {

    init {
        addClass("app-drawer__section")

        gridpane {
            vgap = 5.0
            constraintsForColumn(0).apply {
                hgrow = Priority.ALWAYS
            }
            constraintsForColumn(1).apply {
                halignment = HPos.RIGHT
            }

            add(
                Label(FX.messages["action"]).apply {
                    addClass("app-drawer__subtitle--small")
                }, 0, 0
            )
            add(
                Label(FX.messages["shortcut"]).apply {
                    addClass("app-drawer__subtitle--small")
                }, 1, 0
            )

            add(makeShortcutAction(FX.messages["focus"]), 0, 1)
            add(
                makeShortcutBox(makeShortcutButton("tab")), 1, 1
            )

            add(makeShortcutAction(FX.messages["select"]), 0, 2)
            add(
                makeShortcutBox(
                    makeShortcutButton("space"),
                    makeShortcutButton("enter")
                ), 1, 2
            )

            add(makeShortcutAction(FX.messages["navigation"]), 0, 3)
            add(
                makeShortcutBox(
                    makeShortcutButton(icon = FontIcon(MaterialDesign.MDI_ARROW_UP)),
                    makeShortcutButton(icon = FontIcon(MaterialDesign.MDI_ARROW_DOWN)),
                    makeShortcutButton(icon = FontIcon(MaterialDesign.MDI_ARROW_LEFT)),
                    makeShortcutButton(icon = FontIcon(MaterialDesign.MDI_ARROW_RIGHT))
                ), 1, 3
            )

            add(makeShortcutAction(FX.messages["scrollDown"]), 0, 4)
            add(
                makeShortcutBox(
                    makeShortcutButton("PgDn"),
                    makeShortcutButton(icon = FontIcon(MaterialDesign.MDI_ARROW_DOWN))
                ), 1, 4
            )

            add(makeShortcutAction(FX.messages["scrollUp"]), 0, 5)
            add(
                makeShortcutBox(
                    makeShortcutButton("PgUp"),
                    makeShortcutButton(icon = FontIcon(MaterialDesign.MDI_ARROW_UP))
                ), 1, 5
            )

            add(makeShortcutAction(FX.messages["goBack"]), 0, 6)
            add(
                makeShortcutBox(
                    makeShortcutButton(Shortcut.GO_BACK.value.displayText)
                ), 1, 6
            )

            add(makeShortcutAction(FX.messages["addVerseMarker"]), 0, 7)
            add(
                makeShortcutBox(
                    makeShortcutButton(Shortcut.ADD_MARKER.value.displayText)
                ), 1, 7
            )

            add(makeShortcutAction(FX.messages["recordStop"]), 0, 8)
            add(
                makeShortcutBox(
                    makeShortcutButton(Shortcut.RECORD.value.displayText)
                ), 1, 8
            )

            add(makeShortcutAction(FX.messages["playPauseSource"]), 0, 9)
            add(
                makeShortcutBox(
                    makeShortcutButton(Shortcut.PLAY_SOURCE.value.displayText)
                ), 1, 9
            )

            add(makeShortcutAction(FX.messages["playPauseTarget"]), 0, 10)
            add(
                makeShortcutBox(
                    makeShortcutButton(Shortcut.PLAY_TARGET.value.displayText)
                ), 1, 10
            )
        }
    }

    private fun makeShortcutAction(action: String): Label {
        return Label(action).apply {
            addClass("app-drawer__text")
        }
    }

    private fun makeShortcutBox(vararg shortcuts: Node): Node {
        return HBox().apply {
            spacing = 5.0
            alignment = Pos.TOP_RIGHT
            children.addAll(shortcuts)
        }
    }

    private fun makeShortcutButton(str: String? = null, icon: Node? = null): Node {
        return Label().apply {
            addClass("app-drawer__shortcut")
            text = str?.replace("+", " + ")
            graphic = icon
            textAlignment = TextAlignment.CENTER
        }
    }
}