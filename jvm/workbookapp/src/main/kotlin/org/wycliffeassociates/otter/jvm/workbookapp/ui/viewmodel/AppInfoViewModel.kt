/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import javafx.beans.property.SimpleStringProperty
import org.wycliffeassociates.otter.common.data.CustomException
import tornadofx.ViewModel
import java.text.SimpleDateFormat
import java.util.Date


class AppInfoViewModel : ViewModel() {
    val errorDescription = SimpleStringProperty()
    val reportTimeStamp = SimpleStringProperty()

    @Throws(CustomException::class)
    fun submitErrorReport() {
        if (errorDescription.isNotEmpty.value) {
            val formatter = SimpleDateFormat("HH:mm:ss - yyyy/MM/dd")
            val timestamp = formatter.format(Date())
            reportTimeStamp.set(timestamp)
            println(timestamp)
            val ex = CustomException(errorDescription.value)
            errorDescription.set("")
            throw ex
        }
    }
}