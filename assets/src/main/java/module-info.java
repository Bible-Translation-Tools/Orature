module otter.assets.main {
    requires javax.inject;
    requires io.reactivex.rxjava2;
    requires kotlin.stdlib;
    requires kotlin.stdlib.common;
    requires rxkotlin;
    requires org.slf4j;
    requires otter.common;
    requires otter.common.audio;
    requires java.compiler;
    requires dagger;

    exports org.wycliffeassociates.otter.assets.initialization;
}