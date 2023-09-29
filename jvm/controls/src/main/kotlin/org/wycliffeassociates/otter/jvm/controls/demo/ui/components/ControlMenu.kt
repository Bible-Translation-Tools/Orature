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
package org.wycliffeassociates.otter.jvm.controls.demo.ui.components

import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.combobox.ComboboxItem
import org.wycliffeassociates.otter.jvm.controls.combobox.IconComboBoxCell
import org.wycliffeassociates.otter.jvm.controls.demo.ui.viewmodels.DemoViewModel
import org.wycliffeassociates.otter.jvm.controls.demo.ui.fragments.ButtonFragment
import org.wycliffeassociates.otter.jvm.controls.demo.ui.fragments.ComboBoxFragment
import org.wycliffeassociates.otter.jvm.controls.demo.ui.fragments.TextFieldFragment
import org.wycliffeassociates.otter.jvm.utils.overrideDefaultKeyEventHandler
import tornadofx.*

class ControlMenu : View() {
    private val viewModel: DemoViewModel by inject()

    override val root = VBox()

    init {
        root.apply {
            spacing = 10.0
            addClass("demo__menu")

            combobox(viewModel.selectedThemeProperty, viewModel.supportedThemes) {
                addClass("wa-combobox")
                fitToParentWidth()

                cellFormat {
                    val view = ComboboxItem()
                    graphic = view.apply {
                        topTextProperty.set(it.titleKey)
                    }
                }

                buttonCell = IconComboBoxCell(FontIcon(MaterialDesign.MDI_BRIGHTNESS_6)) {
                    it?.titleKey ?: ""
                }
                overrideDefaultKeyEventHandler {
                    viewModel.updateTheme(it)
                }
            }

            button("Buttons") {
                addClass("btn", "btn--primary")
                fitToParentWidth()

                action {
                    viewModel.showContent<ButtonFragment>()
                }
            }

            button("ComboBox") {
                addClass("btn", "btn--primary")
                fitToParentWidth()

                action {
                    viewModel.showContent<ComboBoxFragment>()
                }
            }
        }
    }
}
