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

import org.wycliffeassociates.otter.jvm.workbookapp.updater.install4j.ui.viewmodel.AppUpdaterViewModel
import tornadofx.*

class NoUpdatesAvailable : Fragment() {

    val vm: AppUpdaterViewModel by inject()

    override val root = vbox {

        fitToParentWidth()

        visibleProperty().bind(vm.showNoUpdatesAvailable)
        managedProperty().bind(visibleProperty())

        styleClass.add("app-drawer__section")

        label(messages["versionUpToDate"]) {
            styleClass.add("app-drawer__subtitle")
        }

        label(messages["versionUpToDateLong"]) {
            styleClass.add("app-drawer__text")
        }
    }
}
