package org.wycliffeassociates.otter.common.audio.mp3

import java.io.File
import java.lang.Exception
import kotlin.math.roundToInt
import org.digitalmediaserver.cuelib.CueParser
import org.digitalmediaserver.cuelib.CueSheet
import org.digitalmediaserver.cuelib.CueSheetSerializer
import org.digitalmediaserver.cuelib.FileData
import org.digitalmediaserver.cuelib.Index
import org.digitalmediaserver.cuelib.Position
import org.digitalmediaserver.cuelib.TrackData
import org.wycliffeassociates.otter.common.audio.AudioCue
import org.wycliffeassociates.otter.common.audio.AudioMetadata

class Mp3Metadata(val file: File): AudioMetadata {

    private val _cues = mutableListOf<AudioCue>()
    private var title = ""

    init {
        if(file.exists() && file.length() > 0) {
            try {
                val cuesheet = CueParser.parse(file, Charsets.UTF_8)
                title = cuesheet.title
                cuesheet.allTrackData.forEach {
                    val label = it.title
                    val index = it.indices.find { it.number == 1 }
                    index?.let {
                        val position = (index.position.totalFrames / 75.0 * 44100.0).roundToInt()
                        _cues.add(AudioCue(position, label))
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    override fun addCue(location: Int, label: String) {
        _cues.add(AudioCue(location, label))
    }

    override fun getCues(): List<AudioCue> {
        return _cues
    }

    fun write() {
        file.delete()
        file.createNewFile()
        val sheet = CueSheet()
        sheet.title = title
        val fileData = FileData(sheet, "\"${file.name}\"", "MP3")
        for ((i, cue) in _cues.sortedBy { it.location }.withIndex()) {
            val cueNumber = cue.label.toInt()
            val trackData = TrackData(fileData, cueNumber, "AUDIO")
            trackData.title = cue.label
            val index = Index()
            index.number = 1
            index.position = Position(cue.location.toLong(), 44100)
            trackData.indices.add(index)

            fileData.trackData.add(trackData)
        }
        sheet.fileData.add(fileData)
        val serialized = CueSheetSerializer().serializeCueSheet(sheet)
        file.writer().use {
            it.write(serialized)
        }
    }
}
