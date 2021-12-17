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
package org.wycliffeassociates.otter.jvm.controls.toggle

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.jvm.controls.skins.media.SourceFormatToggleSkin

class SourceFormatToggle : Control() {

    enum class SourceFormat {
        AUDIO,
        TEXT
    }

    private val USER_AGENT_STYLESHEET = javaClass.getResource("/css/source-format-toggle.css").toExternalForm()

    val activeSourceProperty = SimpleObjectProperty<SourceFormat>(SourceFormat.AUDIO)

    init {
        initialize()
    }

    override fun getUserAgentStylesheet(): String {
        return USER_AGENT_STYLESHEET
    }

    override fun createDefaultSkin(): Skin<*> {
        return SourceFormatToggleSkin(this)
    }

    private fun initialize() {
        stylesheets.setAll(USER_AGENT_STYLESHEET)
    }
}
