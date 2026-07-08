open module org.wycliffeassociates.otter.assets {
    requires org.wycliffeassociates.otter.common;

    requires java.compiler;

    requires kotlin.stdlib;
    requires org.slf4j;
    requires io.reactivex.rxjava2;

    requires dagger;
    requires javax.inject;

    requires com.fasterxml.jackson.kotlin;

    exports org.wycliffeassociates.otter.assets.initialization;
}
