/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.controls.card

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.jvm.controls.skins.cards.ProjectCardSkin
import tornadofx.*
import java.io.File

class ProjectCard(
    title: String = "",
    slug: String = "",
    language: String = "",
    actionText: String = "",
    secondaryActions: List<Action> = listOf()
) : Control() {

    private val onPrimaryAction = SimpleObjectProperty<() -> Unit>()
    private val titleTextProperty = SimpleStringProperty(title)
    private val slugTextProperty = SimpleStringProperty(slug)
    private val languageTextProperty = SimpleStringProperty(language)
    private val actionTextProperty = SimpleStringProperty(actionText)
    private val coverArtProperty = SimpleObjectProperty<File>()
    val secondaryActionsList: ObservableList<Action> = FXCollections.observableArrayList<Action>()

    init {
        addActions(*secondaryActions.toTypedArray())
    }

    fun titleTextProperty(): StringProperty {
        return titleTextProperty
    }

    fun slugTextProperty(): StringProperty {
        return slugTextProperty
    }

    fun languageTextProperty(): StringProperty {
        return languageTextProperty
    }

    fun actionTextProperty(): StringProperty {
        return actionTextProperty
    }

    fun onPrimaryActionProperty() = onPrimaryAction

    fun coverArtProperty() = coverArtProperty

    fun setOnAction(op: () -> Unit) {
        onPrimaryAction.set(op)
    }

    fun addActions(vararg actions: Action) {
        secondaryActionsList.addAll(actions)
    }

    override fun createDefaultSkin(): Skin<*> {
        return ProjectCardSkin(this)
    }
}

fun EventTarget.projectcard(
    title: String = "",
    slug: String = "",
    language: String = "",
    actionText: String = "",
    moreActions: List<Action> = listOf(),
    op: ProjectCard.() -> Unit = {}
) = ProjectCard(title, slug, language, actionText, moreActions).attachTo(this, op)

class Action(val text: String, val iconCode: String, val onClicked: () -> Unit)
