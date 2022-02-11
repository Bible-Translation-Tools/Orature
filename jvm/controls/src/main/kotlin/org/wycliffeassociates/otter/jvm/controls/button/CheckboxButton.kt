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

import javafx.scene.control.CheckBox
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.jvm.controls.skins.button.CheckboxButtonSkin
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import tornadofx.*

class CheckboxButton : CheckBox() {

    init {
        tryImportStylesheet(javaClass.getResource("/css/checkbox-button.css").toExternalForm())
        styleClass.setAll("checkbox-button")
    }

    override fun createDefaultSkin(): Skin<*> {
        return CheckboxButtonSkin(this)
    }
}
