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

import com.jfoenix.controls.JFXProgressBar
import org.wycliffeassociates.otter.jvm.workbookapp.updater.install4j.ui.viewmodel.AppUpdaterViewModel
import tornadofx.*

class UpdateDownloadingFragment : Fragment() {

    val vm: AppUpdaterViewModel by inject()

    override val root = vbox {
        fitToParentSize()

        visibleProperty().bind(vm.showUpdateDownloading)
        managedProperty().bind(visibleProperty())

        styleClass.add("app-drawer__section")

        label(messages["updateDownloading"]).apply {
            styleClass.add("app-drawer__subtitle")
        }

        label {
            styleClass.add("app-drawer__text")
            textProperty().bind(vm.statusMessageProperty)
        }
        label {
            styleClass.add("app-drawer__text")
            textProperty().bind(vm.detailedMessageProperty)
        }

        add(
            JFXProgressBar().apply {
                prefWidth = 300.0
                progressProperty().bind(vm.percentCompleteProperty.divide(100.0))
            }
        )
    }
}
