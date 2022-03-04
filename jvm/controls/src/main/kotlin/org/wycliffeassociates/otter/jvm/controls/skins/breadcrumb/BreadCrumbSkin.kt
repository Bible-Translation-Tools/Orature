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
package org.wycliffeassociates.otter.jvm.controls.skins.breadcrumb

import com.sun.javafx.scene.control.behavior.ButtonBehavior
import javafx.beans.binding.Bindings
import javafx.scene.control.SkinBase
import javafx.scene.layout.HBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadCrumb
import tornadofx.*

class BreadCrumbSkin(private val button: BreadCrumb) : SkinBase<BreadCrumb>(button) {
    private val behavior = ButtonBehavior(button)

    init {
        children.addAll(
            HBox().apply {
                addClass("breadcrumb__root")

                label {
                    graphicProperty().bind(button.iconProperty)
                    textProperty().bind(button.titleProperty)
                    tooltip { textProperty().bind(button.titleProperty) }

                    addClass("breadcrumb__content")
                }

                label {
                    addClass("breadcrumb__separator")
                    graphic = FontIcon(MaterialDesign.MDI_MENU_RIGHT).apply {
                        scaleXProperty().bind(button.orientationScaleProperty)
                    }
                    hiddenWhen(button.isActiveProperty)
                    managedWhen(visibleProperty())
                }

                label {
                    addClass("breadcrumb__help")

                    graphic = FontIcon(MaterialDesign.MDI_HELP_CIRCLE)
                    visibleWhen(
                        Bindings.and(button.isActiveProperty, button.tooltipTextProperty.isNotEmpty)
                    )
                    managedWhen(visibleProperty())

                    tooltip {
                        textProperty().bind(button.tooltipTextProperty)
                        prefWidth = 256.0
                    }
                }
            }
        )
    }

    override fun dispose() {
        super.dispose()
        behavior.dispose()
    }
}
