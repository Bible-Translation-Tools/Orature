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
import org.wycliffeassociates.otter.jvm.workbookapp.ui.system.AppInfo
import org.wycliffeassociates.otter.jvm.workbookapp.updater.install4j.ui.viewmodel.AppUpdaterViewModel
import tornadofx.*

class CheckForUpdatesFragment : Fragment() {

    val vm: AppUpdaterViewModel by inject()
    val info = AppInfo()

    override val root = vbox {
        fitToParentSize()

        visibleProperty().bind(vm.showCheckForUpdate)
        managedProperty().bind(visibleProperty())

        vbox {
            styleClass.addAll("app-drawer__version", "app-drawer__section")

            label(messages["updateFailedNoInternet"]) {
                styleClass.addAll("app-drawer__text", "app-drawer__text--error")
                visibleProperty().bind(vm.showOffline)
                managedProperty().bind(visibleProperty())
            }
        }

        add(
            JFXButton(messages["checkForUpdates"]).apply {
                styleClass.addAll("btn", "btn--secondary")
                tooltip(text)
                setOnAction {
                    vm.checkForUpdates()
                }
            }
        )
    }
}
