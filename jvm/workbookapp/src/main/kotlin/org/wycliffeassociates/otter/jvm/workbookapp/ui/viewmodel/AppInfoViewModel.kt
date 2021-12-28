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

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import javafx.beans.property.SimpleStringProperty
import org.wycliffeassociates.otter.common.data.ErrorReportException
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import tornadofx.ViewModel
import tornadofx.runLater
import java.awt.Desktop
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

class AppInfoViewModel : ViewModel() {
    val errorDescription = SimpleStringProperty()
    val reportTimeStamp = SimpleStringProperty()

    @Inject
    lateinit var directoryProvider: IDirectoryProvider
    private val timestampFormatter = SimpleDateFormat("HH:mm:ss - yyyy/MM/dd")

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
    }

    @Throws(ErrorReportException::class)
    fun submitErrorReport() {
        if (errorDescription.isNotEmpty.value) {
            val timestamp = timestampFormatter.format(Date())
            reportTimeStamp.set(timestamp)

            val ex = ErrorReportException(errorDescription.value)
            errorDescription.set("")
            runLater {
                throw ex
            }
        }
    }

    fun browseApplicationLog() {
        if (Desktop.isDesktopSupported()) {
            Completable.fromAction {
                Desktop.getDesktop().open(directoryProvider.logsDirectory)
            }
                .subscribeOn(Schedulers.io())
                .subscribe()
        }
    }
}
