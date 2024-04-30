package org.wycliffeassociates.otter.common.domain.audio.metadata

import org.wycliffeassociates.otter.common.audio.AudioCue
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.audio.AudioMetadata
import org.wycliffeassociates.otter.common.audio.mp3.Mp3Metadata
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import org.wycliffeassociates.otter.common.audio.wav.WavMetadata
import org.wycliffeassociates.otter.common.audio.wav.WavOutputStream
import org.wycliffeassociates.otter.common.domain.audio.OratureCueParser
import org.wycliffeassociates.otter.common.domain.audio.OratureMarkers
import java.io.File

class OratureMetadata(val audioFile: File, val markers: OratureMarkers): AudioMetadata {

    private val audioFormat: AudioFileFormat = AudioFileFormat.of(audioFile.extension)
    private val vttFile: File = File(audioFile.parent, "${audioFile.nameWithoutExtension}.vtt")
    private val cueFile: File? = if (audioFormat == AudioFileFormat.MP3) {
        File(audioFile.parent, "${audioFile.nameWithoutExtension}.cue")
    } else null

    private val vttMetadata = OratureVttMetadata(vttFile, markers)
    private val mp3Metadata = if (audioFormat == AudioFileFormat.MP3) Mp3Metadata(audioFile, cueFile!!) else null
    private val wavMetadata = if (audioFormat == AudioFileFormat.WAV) WavMetadata() else null

    init {
        markers.clearMarkers()

        when {
            (vttFile.exists() && vttFile.length() > 0) -> {
                vttMetadata.parseVTTFile()
            }
            mp3Metadata != null -> {
                markers.import(OratureCueParser.parse(mp3Metadata.getCues()))
            }
            wavMetadata != null -> {
                markers.import(OratureCueParser.parse(wavMetadata.getCues()))
            }
        }
    }

    override fun addCue(location: Int, label: String) {
        mp3Metadata?.addCue(location, label)
        wavMetadata?.addCue(location, label)
        vttMetadata.addCue(location, label)
    }

    override fun getCues(): List<AudioCue> {
        return markers.getCues()
    }

    override fun clearMarkers() {
        vttMetadata.clearMarkers()
        mp3Metadata?.clearMarkers()
        wavMetadata?.clearMarkers()
    }

    fun write() {
        vttMetadata.write(AudioFile(audioFile).totalFrames)
        mp3Metadata?.write()
        wavMetadata?.let { WavFile(audioFile, it).update() }

    }

    override fun artists(): List<String> {
        return when {
            wavMetadata != null -> wavMetadata.artists()
            mp3Metadata != null -> mp3Metadata.artists()
            else -> listOf()
        }
    }

    override fun setArtists(artists: List<String>) {
        mp3Metadata?.setArtists(artists)
        wavMetadata?.setArtists(artists)
    }

    override fun getLegalInformationUrl(): String {
        return when {
            wavMetadata != null -> wavMetadata.getLegalInformationUrl()
            mp3Metadata != null -> mp3Metadata.getLegalInformationUrl()
            else -> ""
        }
    }

    override fun setLegalInformationUrl(url: String) {
        mp3Metadata?.setLegalInformationUrl(url)
        wavMetadata?.setLegalInformationUrl(url)
    }
}