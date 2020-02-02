package org.wycliffeassociates.otter.jvm.controls.card

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.event.EventTarget
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.jvm.controls.skins.ProjectCardSkin
import tornadofx.*

class ProjectCard(
    private val title: String = "",
    private val slug: String = "",
    private val actionText: String = ""
) : Control() {

    private val onPrimaryAction = SimpleObjectProperty<() -> Unit>()
    private val titleTextProperty = SimpleStringProperty(title)
    private val slugTextProperty = SimpleStringProperty(slug)
    private val actionTextProperty = SimpleStringProperty(actionText)

    fun titleTextProperty(): StringProperty {
        return titleTextProperty
    }

    fun slugTextProperty(): StringProperty {
        return slugTextProperty
    }

    fun actionTextProperty(): StringProperty {
        return actionTextProperty
    }

    fun onPrimaryActionProperty() = onPrimaryAction

    fun setOnAction(op: () -> Unit) {
        onPrimaryAction.set(op)
    }

    override fun createDefaultSkin(): Skin<*> {
        return ProjectCardSkin(this)
    }
}

fun EventTarget.projectcard(
    title: String = "",
    slug: String = "",
    actionText: String = "",
    op: ProjectCard.() -> Unit = {}
) = ProjectCard(title, slug, actionText).attachTo(this, op)