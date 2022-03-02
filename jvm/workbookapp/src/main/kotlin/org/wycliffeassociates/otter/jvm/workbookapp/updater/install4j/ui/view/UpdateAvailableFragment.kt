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

class UpdateAvailableFragment : Fragment() {

    val vm: AppUpdaterViewModel by inject()

    override val root = vbox {

        fitToParentWidth()

        visibleProperty().bind(vm.showUpdateAvailable)
        managedProperty().bind(visibleProperty())

        styleClass.add("app-drawer__section")

        label(messages["updateAvailable"]).apply {
            styleClass.add("app-drawer__subtitle")
        }

        hbox {
            styleClass.add("app-drawer__section")
            label(messages["updateVersion"]).apply {
                styleClass.add("app-drawer__text")
            }
            label {
                textProperty().bind(vm.updateVersion)
                styleClass.add("app-drawer__text")
            }
        }

        hbox {
            styleClass.add("app-drawer__section")
            label(messages["updateSize"]).apply {
                styleClass.add("app-drawer__text")
            }
            label {
                styleClass.add("app-drawer__text")
                textProperty().bind(vm.updateSize)
            }
        }

        label(messages["updateUrl"]).apply {
            styleClass.add("app-drawer__subtitle")
        }

        hyperlink {
            styleClass.add("app-drawer__text--link")
            textProperty().bind(vm.updateUrlText)
            action {
                hostServices.showDocument(textProperty().value)
            }
        }

/** currently does not work on Windows/Linux **/
//        add(
//            JFXButton(messages["updateNow"]).apply {
//                styleClass.addAll("btn", "btn--secondary")
//                tooltip(text)
//                setOnAction {
//                    vm.downloadUpdate()
//                }
//            }
//        )
    }
}
