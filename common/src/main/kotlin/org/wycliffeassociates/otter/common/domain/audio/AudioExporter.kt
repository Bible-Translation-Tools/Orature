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
package org.wycliffeassociates.otter.common.domain.audio

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.data.primitives.Contributor
import org.wycliffeassociates.otter.common.data.primitives.License
import java.io.File
import javax.inject.Inject

class AudioExporter @Inject constructor() {
    @Inject
    lateinit var audioConverter: AudioConverter

    private val logger = LoggerFactory.getLogger(javaClass)

    fun exportMp3(
        audio: File,
        outputDir: File,
        license: License? = null,
        contributors: List<Contributor> = listOf()
    ): Completable {
        val mp3Name = audio.nameWithoutExtension + ".mp3"
        val mp3File = File(outputDir, mp3Name)

        return audioConverter.wavToMp3(audio, mp3File)
            .subscribeOn(Schedulers.io())
            .andThen(updateMetadata(mp3File, license, contributors))
    }

    private fun updateMetadata(
        file: File,
        license: License?,
        contributors: List<Contributor>
    ): Completable {
        return Completable
            .fromAction {
                val audioFile = AudioFile(file)
                audioFile.metadata.setArtists(contributors.map { it.name })
                license?.url?.let {
                    audioFile.metadata.setLegalInformationUrl(it)
                }
                audioFile.update()
            }
            .subscribeOn(Schedulers.io())
            .doOnError {
                logger.error("Error while updating output file metadata.", it)
            }
    }
}