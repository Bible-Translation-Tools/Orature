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
package org.wycliffeassociates.otter.jvm.workbookapp.updater.install4j.ui.view

import com.jfoenix.controls.JFXButton
import org.wycliffeassociates.otter.jvm.workbookapp.updater.install4j.ui.viewmodel.AppUpdaterViewModel
import org.kordamp.ikonli.javafx.FontIcon
import tornadofx.*

class UpdateCompleteFragment : Fragment() {

    private val restartNowIcon = FontIcon("mdi-power")
    private val restartLaterIcon = FontIcon("mdi-calendar-clock")

    private val vm: AppUpdaterViewModel by inject()

    override val root = vbox {
        fitToParentWidth()

        styleClass.add("app-drawer__section")

        visibleProperty().bind(vm.showUpdateCompleted)
        managedProperty().bind(visibleProperty())

        label(messages["downloadComplete"]) {
            styleClass.add("app-drawer__subtitle")
        }

        label(messages["updateNeedsRestart"]) {
            styleClass.add("app-drawer__text")
        }

        add(
            JFXButton(messages["restartLater"]).apply {
                wrapTextProperty().set(true)
                graphic = restartLaterIcon
                styleClass.addAll("btn", "btn--secondary")
                tooltip(text)
                setOnAction {
                    vm.updateLater()
                }
            }
        )
        add(
            JFXButton(messages["restartNow"]).apply {
                wrapTextProperty().set(true)
                graphic = restartNowIcon
                styleClass.addAll("btn", "btn--secondary")
                tooltip(text)
                setOnAction {
                    vm.updateAndRestart()
                }
            }
        )
    }
}
