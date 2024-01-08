module otter.common.audio {
    requires SeekableJlayer;
    requires cuelib.core;
    requires kotlin.stdlib;
    requires kotlin.stdlib.common;
    requires mp3agic;
    requires org.slf4j;

    exports org.wycliffeassociates.otter.common.audio;
    exports org.wycliffeassociates.otter.common.audio.pcm;
    exports org.wycliffeassociates.otter.common.audio.wav;
}