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
package org.wycliffeassociates.otter.jvm.controls.waveform

import javafx.scene.layout.BorderPane
import javafx.scene.layout.Priority
import tornadofx.*

class MarkerViewBackground : BorderPane() {

    init {
        fitToParentSize()
        hgrow = Priority.ALWAYS
        vgrow = Priority.ALWAYS

        with(this) {
            top {
                region {
                    styleClass.add("vm-waveform-frame__top-track")
                }
            }

            bottom {
                region {
                    styleClass.add("vm-waveform-frame__bottom-track")
                }
            }
        }
    }
}
