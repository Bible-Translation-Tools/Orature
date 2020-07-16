package org.wycliffeassociates.otter.jvm.markerapp.app.view

import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import tornadofx.*
import tornadofx.FX.Companion.stylesheets

class TitleFragment: Fragment() {
    private val USER_AGENT_STYLESHEET = javaClass.getResource("/css/verse-marker-app.css").toExternalForm()

    init {
        stylesheets.setAll(USER_AGENT_STYLESHEET)
    }

    override val root = vbox {
        alignment = Pos.CENTER
        styleClass.add("vm-header")
        text("Add Verse Markers") {
            styleClass.add("vm-header__title")
        }
        text("Genesis Chapter 03") {
            styleClass.add("vm-header__subtitle")
        }
    }
}