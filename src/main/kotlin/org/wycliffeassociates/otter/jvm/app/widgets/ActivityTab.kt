package org.wycliffeassociates.otter.jvm.app.widgets

import javafx.scene.control.Button
import tornadofx.*
import tornadofx.Stylesheet.Companion.root


class ActivityTab : Button() {
    init{
        with(root) {
            importStylesheet(WidgetsStyles::class)
            addClass(WidgetsStyles.activityPanelButton)
        }
    }
}

fun activityTab(init: ActivityTab.() -> Unit): ActivityTab {
    val at = ActivityTab()
    at.init()
    return at
}