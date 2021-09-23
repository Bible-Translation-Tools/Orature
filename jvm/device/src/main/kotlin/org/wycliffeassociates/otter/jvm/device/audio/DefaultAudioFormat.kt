package org.wycliffeassociates.otter.jvm.device.audio

import javax.sound.sampled.AudioFormat

val DEFAULT_AUDIO_FORMAT = AudioFormat(
    44100F,
    16,
    1,
    true,
    false
)
