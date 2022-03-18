package org.wycliffeassociates.otter.common.domain.audio

import com.mpatric.mp3agic.ID3v24Tag
import com.mpatric.mp3agic.Mp3File
import java.io.File

class Mp3MetadataAccessor(private val file: File) {
    private val metadata = Mp3File(file.path)

    init {
        if (!metadata.hasId3v2Tag()) {
            metadata.id3v2Tag = ID3v24Tag()
        }
    }

    fun setArtists(artists: List<String>) {
        metadata.id3v2Tag.artist = artists.joinToString("/")
    }

    fun artists(): List<String> = metadata.id3v2Tag.artist.split("/")

    fun getLegalInformationUrl(): String = metadata.id3v2Tag.copyrightUrl

    fun setLegalInformationUrl(url: String) {
        metadata.id3v2Tag.copyrightUrl = url
    }

    fun execute(output: File? = null) {
        if (output != null) {
            metadata.save(output.path)
        } else {
            /* writing to currently opened file is not allowed;
            Make a copy first, then overwrite the original file */
            val tempFile = kotlin.io.path.createTempFile("orature-audio", ".mp3")
                .toFile()

            metadata.save(tempFile.path)
            tempFile.copyTo(file, true)
            tempFile.delete()
        }
    }
}