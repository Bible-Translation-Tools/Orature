package org.wycliffeassociates.otter.common.domain.narration

import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.data.audio.VerseMarker

class EditVerseActionTest{

    val totalVerses: MutableList<VerseNode> = mutableListOf()
    lateinit var workingAudioFile: AudioFile
    val numTestVerses = 31

    @Before
    fun setup() {
        workingAudioFile = mockWorkingAudio()
        initializeVerseNodeList(totalVerses)
    }

    fun mockWorkingAudio(): AudioFile {
        return mockk<AudioFile> {
            every { totalFrames } returns 411000
        }
    }

    // Initializes each verse with placed equal to true and with one sector that holds one second worth of frames.
    // where the start of each added sector is offset by "paddingLength" number of frames
    fun initializeVerseNodeList(verseNodeList : MutableList<VerseNode>, paddingLength: Int = 0) {
        var start = -1
        for (i in 0 until numTestVerses) {
            val verseMarker = VerseMarker((i + 1), (i + 1), 0)
            val sectors = mutableListOf<IntRange>()
            val verseNode = VerseNode(true, verseMarker, sectors)
            sectors.add(start + 1 .. start + 44100)
            start += 44100 + paddingLength
            verseNodeList.add(verseNode)
        }
    }

    // TODO: figure out when this action is used and if it is correct because I thought we where not using
    //  verseNode.startScratchFrame and verseNode.endScratchFrame, and those are the only values being changed
    //  besides placed being set to true
}