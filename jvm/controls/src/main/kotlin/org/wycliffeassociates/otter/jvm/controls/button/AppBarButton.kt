/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.controls.button

import javafx.scene.control.Skin
import javafx.scene.control.ToggleButton
import org.wycliffeassociates.otter.jvm.controls.skins.button.AppBarButtonSkin
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet

class AppBarButton : ToggleButton() {

    init {
        tryImportStylesheet(javaClass.getResource("/css/app-bar-button.css").toExternalForm())
        styleClass.setAll("app-bar-button")
    }

    override fun createDefaultSkin(): Skin<*> {
        return AppBarButtonSkin(this)
    }
}
