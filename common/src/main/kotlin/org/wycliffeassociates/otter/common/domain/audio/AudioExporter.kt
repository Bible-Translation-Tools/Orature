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
package org.wycliffeassociates.otter.common.domain.audio

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioCue
import org.wycliffeassociates.otter.common.data.primitives.Contributor
import org.wycliffeassociates.otter.common.data.primitives.License
import java.io.File
import javax.inject.Inject

const val WAV_TO_MP3_COMPRESSED_RATE = 10 // converting from wav to mp3 yields ~10x smaller file size

class AudioExporter @Inject constructor() {
    @Inject
    lateinit var audioConverter: AudioConverter

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Exports the given wav file to mp3 file
     * including the relevant metadata.
     * @param wavAudio the input wav file
     * @param outputPath either a directory or a complete file path
     */
    fun exportMp3(
        wavAudio: File,
        outputPath: File,
        metadata: ExportMetadata
    ): Completable {
        val mp3File = if (outputPath.isDirectory) {
            File(outputPath, wavAudio.nameWithoutExtension + ".mp3")
        } else {
            outputPath
        }

        return audioConverter.wavToMp3(wavAudio, mp3File)
            .subscribeOn(Schedulers.io())
            .andThen(updateMetadata(mp3File, metadata))
    }

    private fun updateMetadata(
        file: File,
        metadata: ExportMetadata
    ): Completable {
        return Completable
            .fromAction {
                val oratureAudioFile = OratureAudioFile(file)
                oratureAudioFile.metadata.setArtists(metadata.contributors.map { it.name })
                metadata.license?.url?.let {
                    oratureAudioFile.metadata.setLegalInformationUrl(it)
                }
                oratureAudioFile.importCues(metadata.markers)
                oratureAudioFile.update()
            }
            .subscribeOn(Schedulers.io())
            .doOnError {
                logger.error("Error while updating output file metadata.", it)
            }
    }

    data class ExportMetadata(
        val license: License?,
        val contributors: List<Contributor> = listOf(),
        val markers: List<AudioCue> = listOf()
    )
}