package org.wycliffeassociates.otter.jvm.app.widgets


import io.reactivex.subjects.PublishSubject
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import tornadofx.*
import tornadofx.Stylesheet.Companion.root

class ActivityPanel : HBox() {
    var tabs= PublishSubject.create<ArrayList<ActivityTab>>()

    init {
        with(root) {
            spacing = 10.0
            alignment = Pos.CENTER
            tabs.subscribe {
                it.forEach{
                    add(it)
                }
            }
        }
    }
}

fun activitypanel(init: ActivityPanel.() -> Unit) : ActivityPanel {
    val ap = ActivityPanel()
    ap.init()
    return ap
}

