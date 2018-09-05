package org.wycliffeassociates.otter.jvm.app.widgets


import javafx.geometry.Pos
import javafx.scene.layout.HBox
import tornadofx.*
import tornadofx.Stylesheet.Companion.root

class ActivityPanel(tabs:ArrayList<ActivityTab>) : HBox() {
    init {
        with(root) {
            spacing = 10.0
            alignment = Pos.CENTER
            tabs.forEach{
                add(it)
            }
        }
    }
}

fun activitypanel(list: ArrayList<ActivityTab>,init: ActivityPanel.() -> Unit) : ActivityPanel {
    val ap = ActivityPanel(list)
    ap.init()
    return ap
}

