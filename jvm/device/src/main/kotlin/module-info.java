module otter.jvm.device {
    requires java.desktop;
    requires javax.inject;
    requires io.reactivex.rxjava2;
    requires kotlin.stdlib;
    requires kotlin.stdlib.common;
    requires rxkotlin;
    requires rxrelay;
    requires org.slf4j;
    requires tarsosdsp;
    requires otter.common.audio;
    requires otter.common;
    requires java.compiler;
    requires dagger;

    exports org.wycliffeassociates.otter.jvm.device;
    exports org.wycliffeassociates.otter.jvm.device.audio;
    exports org.wycliffeassociates.otter.jvm.device.system;
}