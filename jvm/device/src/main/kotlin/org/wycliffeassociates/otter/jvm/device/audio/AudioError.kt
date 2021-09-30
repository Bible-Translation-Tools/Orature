package org.wycliffeassociates.otter.jvm.device.audio

import java.lang.Exception

enum class AudioErrorType {
    PLAYBACK,
    RECORDING
}

class AudioError(val type: AudioErrorType, val exception: Exception)
