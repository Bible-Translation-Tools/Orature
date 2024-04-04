/**
 * Copyright (C) 2020-2024 Wycliffe Associates
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

import io.reactivex.subjects.PublishSubject
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import tornadofx.*
import java.io.File

class ImportAudioViewModel : ViewModel() {

    private val logger = LoggerFactory.getLogger(ImportAudioViewModel::class.java)
    
    val snackBarObservable: PublishSubject<String> = PublishSubject.create()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
    }

    fun isValidImportFile(files: List<File>): Boolean {
        return when {
            files.size > 1 -> {
                snackBarObservable.onNext(messages["importMultipleError"])
                logger.error(
                    "(Drag-Drop) Multi-files import is not supported. Input files: $files"
                )
                false
            }

            files.first().isDirectory -> {
                snackBarObservable.onNext(messages["importDirectoryError"])
                logger.error(
                    "(Drag-Drop) Directory import is not supported. Input path: ${files.first()}"
                )
                false
            }


            !AudioFileFormat.isSupported(files.first().extension) -> {
                snackBarObservable.onNext(messages["importInvalidAudioFileError"])
                logger.error(
                    "(Drag-Drop) Invalid import file extension. Input files: ${files.first()}"
                )
                false
            }

            else -> true
        }
    }
}
