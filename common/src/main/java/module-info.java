open module org.wycliffeassociates.otter.common {
    requires org.wycliffeassociates.otter.common.audio;

    requires java.compiler;

    requires kotlin.stdlib;
    requires org.slf4j;

    requires rxkotlin;
    requires rxrelay;
    requires io.reactivex.rxjava2;

    requires jump3r;
    requires kotlin.tstudio2rc;
    requires kotlin.resource.container;

    requires usfmtools;

    requires dagger;
    requires javax.inject;

    requires kotlin.reflect;

    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.kotlin;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires com.fasterxml.jackson.dataformat.csv;

    requires retrofit2;
    requires okhttp3;
    requires retrofit2.converter.jackson;
    requires retrofit2.adapter.rxjava2;

    requires tika.core;

    requires kotlin.scripture.burrito;
    requires kotlin.vtt;
    requires kotlin.scripture.alignment;

    exports org.wycliffeassociates.otter.common;
    exports org.wycliffeassociates.otter.common.collections;
    exports org.wycliffeassociates.otter.common.data;
    exports org.wycliffeassociates.otter.common.data.audio;
    exports org.wycliffeassociates.otter.common.data.primitives;
    exports org.wycliffeassociates.otter.common.data.workbook;
    exports org.wycliffeassociates.otter.common.device;
    exports org.wycliffeassociates.otter.common.domain;
    exports org.wycliffeassociates.otter.common.domain.audio;
    exports org.wycliffeassociates.otter.common.domain.audio.metadata;
    exports org.wycliffeassociates.otter.common.domain.collections;
    exports org.wycliffeassociates.otter.common.domain.content;
    exports org.wycliffeassociates.otter.common.domain.languages;
    exports org.wycliffeassociates.otter.common.domain.mapper;
    exports org.wycliffeassociates.otter.common.domain.model;
    exports org.wycliffeassociates.otter.common.domain.narration;
    exports org.wycliffeassociates.otter.common.domain.narration.teleprompter;
    exports org.wycliffeassociates.otter.common.domain.plugins;
    exports org.wycliffeassociates.otter.common.domain.project;
    exports org.wycliffeassociates.otter.common.domain.project.exporter;
    exports org.wycliffeassociates.otter.common.domain.project.exporter.resourcecontainer;
    exports org.wycliffeassociates.otter.common.domain.project.importer;
    exports org.wycliffeassociates.otter.common.domain.resourcecontainer;
    exports org.wycliffeassociates.otter.common.domain.resourcecontainer.artwork;
    exports org.wycliffeassociates.otter.common.domain.resourcecontainer.burrito;
    exports org.wycliffeassociates.otter.common.domain.resourcecontainer.burrito.auth;
    exports org.wycliffeassociates.otter.common.domain.resourcecontainer.project;
    exports org.wycliffeassociates.otter.common.domain.resourcecontainer.project.markdown;
    exports org.wycliffeassociates.otter.common.domain.resourcecontainer.project.usfm;
    exports org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport;
    exports org.wycliffeassociates.otter.common.domain.theme;
    exports org.wycliffeassociates.otter.common.domain.translation;
    exports org.wycliffeassociates.otter.common.domain.versification;
    exports org.wycliffeassociates.otter.common.io.zip;
    exports org.wycliffeassociates.otter.common.persistence;
    exports org.wycliffeassociates.otter.common.persistence.config;
    exports org.wycliffeassociates.otter.common.persistence.mapping;
    exports org.wycliffeassociates.otter.common.persistence.repositories;
    exports org.wycliffeassociates.otter.common.recorder;
    exports org.wycliffeassociates.otter.common.utils;
}
