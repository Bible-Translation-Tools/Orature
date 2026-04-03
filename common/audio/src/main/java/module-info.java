module org.wycliffeassociates.otter.common.audio {
    requires jdk.unsupported;
    requires kotlin.stdlib;
    requires org.slf4j;

    requires SeekableJlayer;
    requires cuelib.core;
    requires kotlin.vtt;
    requires mp3agic;
    requires tarsosdsp;

    exports org.wycliffeassociates.otter.common.audio;
    exports org.wycliffeassociates.otter.common.audio.mp3;
    exports org.wycliffeassociates.otter.common.audio.pcm;
    exports org.wycliffeassociates.otter.common.audio.wav;
}
