package org.wycliffeassociates.otter.jvm.app.widgets.projectnav

import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.effect.DropShadow
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.paint.Color

import tornadofx.*

class ProjectNav : VBox() {
    val selectProjectTextProperty = SimpleStringProperty()
    var selectProjectText by selectProjectTextProperty

    val selectChapterTextProperty = SimpleStringProperty()
    var selectChapterText by selectChapterTextProperty

    val selectChunkTextProperty = SimpleStringProperty()
    var selectChunkText by selectChunkTextProperty


    val nodeList = observableList<Node>()

    fun ProjectNav.navbox(mainLabel: String? = null, graphic: Node? = null, init: NavBox.() -> Unit = {}): NavBox {
        val nb = NavBox(mainLabel, graphic)
        nb.init()
        addBox(nb)
        return nb
    }

    fun navButton(init: Button.() -> Unit = {}): Button {
        val bttn = Button()
        bttn.init()
        bttn.addClass(ProjectNavStyles.navbutton)
        nodeList.add(bttn)
        return bttn
    }

    fun addBox(newBox: Node) {
        nodeList.add(newBox)
    }

    init {
        importStylesheet<ProjectNavStyles>()
        vbox(10) {
            vgrow = Priority.ALWAYS
            hgrow = Priority.ALWAYS

            style {
                prefWidth = 200.0.px
                alignment = Pos.TOP_CENTER
                padding = box(15.0.px)
                backgroundColor += Color.WHITE
                effect = DropShadow(3.0, 3.0, 0.0, Color.LIGHTGRAY)
            }

            nodeList.onChange {
                it.list.forEach{
                    add(it)
                }
            }

        }
    }
}


fun projectnav(init: ProjectNav.() -> Unit = {}): ProjectNav {
    val pn = ProjectNav()
    pn.init()
    return pn
}
