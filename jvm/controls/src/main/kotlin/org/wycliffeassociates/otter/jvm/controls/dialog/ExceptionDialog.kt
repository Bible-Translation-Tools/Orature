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
package org.wycliffeassociates.otter.jvm.controls.dialog

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import org.wycliffeassociates.otter.common.data.ColorTheme
import org.wycliffeassociates.otter.jvm.controls.media.ExceptionContent

class ExceptionDialog : OtterDialog() {

    val titleTextProperty = SimpleStringProperty()
    val headerTextProperty = SimpleStringProperty()
    val showMoreTextProperty = SimpleStringProperty()
    val showLessTextProperty = SimpleStringProperty()
    val sendReportTextProperty = SimpleStringProperty()
    val sendReportProperty = SimpleBooleanProperty()
    val stackTraceProperty = SimpleStringProperty()
    val closeTextProperty = SimpleStringProperty()
    val sendingReportProperty = SimpleBooleanProperty()

    private val onCloseActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    private val exceptionContent = ExceptionContent().apply {
        titleTextProperty().bind(titleTextProperty)
        headerTextProperty().bind(headerTextProperty)
        showMoreTextProperty().bind(showMoreTextProperty)
        showLessTextProperty().bind(showLessTextProperty)
        sendReportTextProperty().bind(sendReportTextProperty)
        sendingReportProperty().bind(sendingReportProperty)
        stackTraceProperty().bind(stackTraceProperty)
        closeTextProperty().bind(closeTextProperty)
        onCloseActionProperty().bind(onCloseActionProperty)

        sendReportProperty.bind(sendReportProperty())
    }

    init {
        setContent(exceptionContent)
        themeProperty.set(ColorTheme.LIGHT)
    }

    fun onCloseAction(op: () -> Unit) {
        onCloseActionProperty.set(EventHandler { op.invoke() })
    }
}
