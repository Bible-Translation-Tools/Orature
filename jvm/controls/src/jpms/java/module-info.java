open module org.wycliffeassociates.otter.jvm.controls {
    requires org.wycliffeassociates.otter.common;
    requires org.wycliffeassociates.otter.common.audio;
    requires org.wycliffeassociates.otter.jvm.device;
    requires org.wycliffeassociates.otter.jvm.utils;

    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires kotlin.stdlib;

    requires io.reactivex.rxjava2;
    requires rxkotlin;
    requires rxrelay;
    requires rxkotlinfx;

    requires tornadofx;

    requires FranzXaverSVG.unspecified;

    requires com.jfoenix;
    requires kfoenix;
    requires MaterialFX;
    requires org.controlsfx.controls;

    requires org.slf4j;

    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome;
    requires org.kordamp.ikonli.fontawesome5;
    requires org.kordamp.ikonli.materialdesign;
    requires org.kordamp.ikonli.material;

    requires com.jthemedetector;

    requires org.commonmark;

    exports org.wycliffeassociates.otter.jvm.controls;
    exports org.wycliffeassociates.otter.jvm.controls.banner;
    exports org.wycliffeassociates.otter.jvm.controls.bar;
    exports org.wycliffeassociates.otter.jvm.controls.breadcrumbs;
    exports org.wycliffeassociates.otter.jvm.controls.button;
    exports org.wycliffeassociates.otter.jvm.controls.canvas;
    exports org.wycliffeassociates.otter.jvm.controls.card;
    exports org.wycliffeassociates.otter.jvm.controls.card.events;
    exports org.wycliffeassociates.otter.jvm.controls.chapterselector;
    exports org.wycliffeassociates.otter.jvm.controls.combobox;
    exports org.wycliffeassociates.otter.jvm.controls.controllers;
    exports org.wycliffeassociates.otter.jvm.controls.demo;
    exports org.wycliffeassociates.otter.jvm.controls.demo.ui.components;
    exports org.wycliffeassociates.otter.jvm.controls.demo.ui.fragments;
    exports org.wycliffeassociates.otter.jvm.controls.demo.ui.models;
    exports org.wycliffeassociates.otter.jvm.controls.demo.ui.screens;
    exports org.wycliffeassociates.otter.jvm.controls.demo.ui.viewmodels;
    exports org.wycliffeassociates.otter.jvm.controls.dialog;
    exports org.wycliffeassociates.otter.jvm.controls.dragtarget;
    exports org.wycliffeassociates.otter.jvm.controls.dragtarget.events;
    exports org.wycliffeassociates.otter.jvm.controls.event;
    exports org.wycliffeassociates.otter.jvm.controls.listview;
    exports org.wycliffeassociates.otter.jvm.controls.marker;
    exports org.wycliffeassociates.otter.jvm.controls.media;
    exports org.wycliffeassociates.otter.jvm.controls.model;
    exports org.wycliffeassociates.otter.jvm.controls.narration;
    exports org.wycliffeassociates.otter.jvm.controls.popup;
    exports org.wycliffeassociates.otter.jvm.controls.rollingtext;
    exports org.wycliffeassociates.otter.jvm.controls.skins;
    exports org.wycliffeassociates.otter.jvm.controls.skins.banner;
    exports org.wycliffeassociates.otter.jvm.controls.skins.bar;
    exports org.wycliffeassociates.otter.jvm.controls.skins.breadcrumb;
    exports org.wycliffeassociates.otter.jvm.controls.skins.button;
    exports org.wycliffeassociates.otter.jvm.controls.skins.cards;
    exports org.wycliffeassociates.otter.jvm.controls.skins.media;
    exports org.wycliffeassociates.otter.jvm.controls.skins.slider;
    exports org.wycliffeassociates.otter.jvm.controls.statusindicator;
    exports org.wycliffeassociates.otter.jvm.controls.styles;
    exports org.wycliffeassociates.otter.jvm.controls.toggle;
    exports org.wycliffeassociates.otter.jvm.controls.waveform;
}
