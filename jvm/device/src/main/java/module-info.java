open module org.wycliffeassociates.otter.jvm.device {
    requires org.wycliffeassociates.otter.common;
    requires org.wycliffeassociates.otter.common.audio;

    requires java.compiler;
    requires java.desktop;

    requires kotlin.stdlib;
    requires org.slf4j;

    requires io.reactivex.rxjava2;
    requires rxkotlin;
    requires rxrelay;

    requires dagger;
    requires javax.inject;

    requires tarsosdsp;

    exports org.wycliffeassociates.otter.jvm.device;
    exports org.wycliffeassociates.otter.jvm.device.audio;
    exports org.wycliffeassociates.otter.jvm.device.system;
}
