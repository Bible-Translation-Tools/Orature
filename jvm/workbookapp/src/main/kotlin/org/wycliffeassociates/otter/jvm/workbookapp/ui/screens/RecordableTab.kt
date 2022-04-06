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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.scene.control.Tab
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.common.domain.content.Recordable
import org.wycliffeassociates.otter.jvm.controls.statusindicator.StatusIndicator
import org.wycliffeassociates.otter.jvm.controls.statusindicator.statusindicator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.RecordableTabViewModel
import tornadofx.*

class RecordableTab(
    private val viewModel: RecordableTabViewModel,
    private val onTabSelect: (Recordable) -> Unit
) : Tab() {

    val recordableProperty = SimpleObjectProperty<Recordable?>()
    var recordableListener: ChangeListener<Recordable?>? = null
    private val recordResourceFragment = RecordResourceFragment(viewModel)

    init {
        graphic = statusindicator {
            initForRecordableTab()
            progressProperty.bind(viewModel.getProgressBinding())
        }

        recordResourceFragment.apply {
            formattedTextProperty.bind(viewModel.getFormattedTextBinding())
            this@RecordableTab.content = this.root
        }

        selectedProperty().onChange { selected ->
            if (selected) {
                callOnTabSelect()
            }
        }
    }

    fun bindProperties() {
        textProperty().bind(viewModel.labelProperty)
        recordableProperty.bind(viewModel.recordableProperty)
        viewModel.openPlayers()
        recordResourceFragment.onDock()
    }

    fun unbindProperties() {
        textProperty().unbind()
        recordableProperty.unbind()
        viewModel.closePlayers()
        recordResourceFragment.onUndock()
    }

    fun removeListeners() {
        recordableListener?.let { recordableProperty.removeListener(it) }
    }

    fun recordNewTake() {
        viewModel.recordNewTake()
    }

    private fun callOnTabSelect() {
        viewModel.recordable?.let { onTabSelect(it) }
            ?: throw IllegalStateException("Selected tab's recordable is null")
    }

    private fun StatusIndicator.initForRecordableTab() {
        prefWidth = 50.0
        primaryFill = Color.ORANGE
        accentFill = Color.LIGHTGRAY
        trackFill = Color.LIGHTGRAY
        indicatorRadius = 4.0
    }
}
