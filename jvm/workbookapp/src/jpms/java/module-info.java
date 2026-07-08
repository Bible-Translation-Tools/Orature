open module org.wycliffeassociates.otter.workbookapp {
    requires org.wycliffeassociates.otter.assets;
    requires org.wycliffeassociates.otter.common;
    requires org.wycliffeassociates.otter.common.audio;
    requires org.wycliffeassociates.otter.jvm.controls;
    requires org.wycliffeassociates.otter.jvm.device;
    requires org.wycliffeassociates.otter.jvm.utils;
    requires org.wycliffeassociates.otter.jvm.workbookplugin;
    requires org.wycliffeassociates.otter.javafx.gridview;

    requires java.compiler;
    requires java.desktop;
    requires java.sql;

    // Loaded reflectively (Class.forName in AppDatabase); without an explicit requires,
    // jlink would not resolve the module into the runtime image.
    requires org.xerial.sqlitejdbc;

    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.media;

    requires MaterialFX;
    requires com.jfoenix;
    requires kfoenix;
    requires org.controlsfx.controls;

    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome;
    requires org.kordamp.ikonli.fontawesome5;
    requires org.kordamp.ikonli.materialdesign;
    requires org.kordamp.ikonli.material;

    requires kotlin.stdlib;
    requires kotlin.reflect;

    requires org.slf4j;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;

    requires dagger;
    requires javax.inject;

    requires io.reactivex.rxjava2;
    requires rxkotlin;
    requires rxrelay;
    requires rxkotlinfx;

    requires tornadofx;

    requires retrofit2;
    requires retrofit2.converter.jackson;
    requires retrofit2.adapter.rxjava2;
    requires okhttp3;

    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.kotlin;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires com.fasterxml.jackson.dataformat.csv;

    requires org.commonmark;

    requires javautil;

    requires kotlin.resource.container;
    requires kotlin.scripture.burrito;
    requires kotlin.scripture.alignment;
    requires usfmtools;

    requires com.jthemedetector;
    requires com.install4j.runtime;

    requires sentry;

    requires org.json;

    requires mp3agic;
    requires tarsosdsp;
}
