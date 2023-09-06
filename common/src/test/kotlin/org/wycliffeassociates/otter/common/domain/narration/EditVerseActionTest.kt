package org.wycliffeassociates.otter.common.domain.narration

import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.data.audio.VerseMarker

class EditVerseActionTest{

    val totalVerses: MutableList<VerseNode> = mutableListOf()
    lateinit var workingAudioFile: AudioFile

    @Before
    fun setup() {
        workingAudioFile = mockWorkingAudio()
        initializeTotalVerses()
    }

    fun mockWorkingAudio(): AudioFile {
        return mockk<AudioFile> {
            every { totalFrames } returns 411000
        }
    }

    // Initializes each verse with placed equal to true and with one sector
    // that is an int range of 44100*i until 44100*(i+1)
    // so each verseNode will have one second of recording
    fun initializeTotalVerses() {
        val numVerses = 31
        for (i in 0 until numVerses) {
            val verseMarker = VerseMarker((i + 1), (i + 1), 0)
            val sectors = mutableListOf<IntRange>()
            val verseNode = VerseNode(0, 0, true, verseMarker, sectors)
            sectors.add(44100 * i until (44100 * (i + 1)))
            totalVerses.add(verseNode)
        }
    }

    // TODO: figure out when this action is used and if it is correct because I thought we where not using
    //  verseNode.startScratchFrame and verseNode.endScratchFrame, and those are the only values being changed
    //  besides placed being set to true
}