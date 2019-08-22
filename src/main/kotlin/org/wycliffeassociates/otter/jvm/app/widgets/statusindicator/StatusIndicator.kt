package org.wycliffeassociates.otter.jvm.statusindicator.control

import com.sun.javafx.css.converters.ColorConverter
import javafx.beans.property.*
import javafx.css.*
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.*
import tornadofx.*

class StatusIndicator : Control() {

    val primaryFillProperty: StyleableObjectProperty<Color> =
        object : SimpleStyleableObjectProperty<Color>(
            PRIMARY_FILL,
            StatusIndicator,
            "primaryFill",
            Color.RED
        ) {
        }

    var primaryFill: Color by primaryFillProperty

    val accentFillProperty: StyleableObjectProperty<Color> =
        object : SimpleStyleableObjectProperty<Color>(
            ACCENT_FILL,
            StatusIndicator,
            "accentFill",
            Color.RED
        ) {
        }
    var accentFill: Color by accentFillProperty

    val trackFillProperty: StyleableObjectProperty<Color> =
        object : SimpleStyleableObjectProperty<Color>(
            TRACK_FILL,
            StatusIndicator,
            "trackFill",
            Color.WHITE
        ) {
        }
    var trackFill: Color by trackFillProperty

    val barBorderStyleProperty: StyleableObjectProperty<BorderStrokeStyle> =
        object : SimpleStyleableObjectProperty<BorderStrokeStyle>(
            BAR_BORDER,
            StatusIndicator,
            "barBorderStyle",
            BorderStrokeStyle.NONE
        ) {
        }
    var barBorderStyle by barBorderStyleProperty

    val barBorderColorProperty: StyleableObjectProperty<Paint> =
        object : SimpleStyleableObjectProperty<Paint>(
            BAR_BORDER_COLOR,
            StatusIndicator,
            "barBorderColor",
            Color.BLACK
        ) {
        }
    var barBorderColor by barBorderColorProperty

    val textFillProperty: StyleableObjectProperty<Paint> =
        object : SimpleStyleableObjectProperty<Paint>(
            TEXT_FILL,
            StatusIndicator,
            "textFill",
            Color.BLACK
        ) {
        }
    var textFill by textFillProperty

    val trackBorderStyleProperty: StyleableObjectProperty<BorderStrokeStyle> =
        object : SimpleStyleableObjectProperty<BorderStrokeStyle>(
            TRACK_BORDER,
            StatusIndicator,
            "trackBorder",
            BorderStrokeStyle.NONE
        ) {
        }
    var trackBorderStyle by trackBorderStyleProperty

    val trackBorderColorProperty: StyleableObjectProperty<Paint> =
        object : SimpleStyleableObjectProperty<Paint>(
            TRACK_BORDER_COLOR,
            StatusIndicator,
            "trackBorderColor",
            Color.BLACK
        ) {
        }
    var trackBorderColor by trackBorderColorProperty

    val progressProperty: DoubleProperty = SimpleDoubleProperty(0.0)
    var progress: Double by progressProperty

    val indicatorRadiusProperty: DoubleProperty = SimpleDoubleProperty(0.0)
    var indicatorRadius by indicatorRadiusProperty

    val barHeightProperty: DoubleProperty = SimpleDoubleProperty(10.0)
    var barHeight: Double by barHeightProperty

    val trackHeightProperty: DoubleProperty = SimpleDoubleProperty(10.0)
    var trackHeight: Double by trackHeightProperty

    val showTextProperty = SimpleBooleanProperty(false)
    var showText by showTextProperty

    private val isCompleteProperty: BooleanProperty = SimpleBooleanProperty(false)
    var isComplete by isCompleteProperty

    val barBorderRadiusProperty: DoubleProperty = SimpleDoubleProperty(0.0)
    var barBorderRadius by barBorderRadiusProperty

    val barBorderWidthProperty: DoubleProperty = SimpleDoubleProperty(0.0)
    var barBorderWidth by barBorderWidthProperty

    val trackBorderRadiusProperty: DoubleProperty = SimpleDoubleProperty(0.0)
    var trackBorderRadius by trackBorderRadiusProperty

    val trackBorderWidthProperty: DoubleProperty = SimpleDoubleProperty(0.0)
    var trackBorderWidth by trackBorderWidthProperty

    init {
        isCompleteProperty.onChange {
            if (it) {
                progressProperty.set(1.0)
            }
        }
    }

    override fun createDefaultSkin(): Skin<*> {
        return StatusIndicatorSkin(this)
    }

    private companion object StyleableProperties {
        private val PRIMARY_FILL: CssMetaData<StatusIndicator, Color> =
            object : CssMetaData<StatusIndicator, Color>(
                "-fx-primary-fill",
                ColorConverter.getInstance(),
                Color.BLACK
            ) {
                override fun isSettable(styleable: StatusIndicator): Boolean {
                    return styleable.primaryFillProperty.value == null || styleable.primaryFillProperty.isBound
                }

                override fun getStyleableProperty(styleable: StatusIndicator): StyleableProperty<Color> {
                    return styleable.primaryFillProperty
                }
            }
        private val ACCENT_FILL: CssMetaData<StatusIndicator, Color> =
            object : CssMetaData<StatusIndicator, Color>(
                "-fx-accent-fill",
                ColorConverter.getInstance(),
                Color.BLACK
            ) {
                override fun isSettable(styleable: StatusIndicator): Boolean {
                    return styleable.accentFillProperty.value == null || styleable.accentFillProperty.isBound
                }

                override fun getStyleableProperty(styleable: StatusIndicator): StyleableProperty<Color> {
                    return styleable.accentFillProperty
                }
            }

        private val TRACK_FILL: CssMetaData<StatusIndicator, Color> =
            object : CssMetaData<StatusIndicator, Color>(
                "-fx-track-fill",
                ColorConverter.getInstance(),
                Color.BLACK
            ) {
                override fun isSettable(styleable: StatusIndicator): Boolean {
                    return styleable.trackFillProperty.value == null || styleable.trackFillProperty.isBound
                }

                override fun getStyleableProperty(styleable: StatusIndicator): StyleableProperty<Color> {
                    return styleable.trackFillProperty
                }
            }

        private val BAR_BORDER: CssMetaData<StatusIndicator, BorderStrokeStyle> =
            object : CssMetaData<StatusIndicator, BorderStrokeStyle>(
                "-fx-bar-border",
                StyleConverter<StatusIndicator, BorderStrokeStyle>(),
                BorderStrokeStyle.NONE
            ) {
                override fun isSettable(styleable: StatusIndicator): Boolean {
                    return styleable.barBorderStyleProperty.value == null || styleable.barBorderStyleProperty.isBound
                }

                override fun getStyleableProperty(styleable: StatusIndicator): StyleableProperty<BorderStrokeStyle> {
                    return styleable.barBorderStyleProperty
                }
            }

        private val TEXT_FILL: CssMetaData<StatusIndicator, Paint> =
            object : CssMetaData<StatusIndicator, Paint>(
                "-fx-text-fill",
                StyleConverter<StatusIndicator, Paint>(),
                Color.BLACK
            ) {
                override fun isSettable(styleable: StatusIndicator): Boolean {
                    return styleable.textFillProperty.value == null || styleable.textFillProperty.isBound
                }

                override fun getStyleableProperty(styleable: StatusIndicator): StyleableProperty<Paint> {
                    return styleable.textFillProperty
                }
            }

        private val BAR_BORDER_COLOR: CssMetaData<StatusIndicator, Paint> =
            object : CssMetaData<StatusIndicator, Paint>(
                "-fx-bar-border-color",
                StyleConverter<StatusIndicator, Paint>(),
                Color.BLACK
            ) {
                override fun isSettable(styleable: StatusIndicator): Boolean {
                    return styleable.barBorderColorProperty.value == null || styleable.barBorderColorProperty.isBound
                }

                override fun getStyleableProperty(styleable: StatusIndicator): StyleableProperty<Paint> {
                    return styleable.barBorderColorProperty
                }
            }

        private val TRACK_BORDER_COLOR: CssMetaData<StatusIndicator, Paint> =
            object : CssMetaData<StatusIndicator, Paint>(
                "-fx-track-border-color",
                StyleConverter<StatusIndicator, Paint>(),
                Color.BLACK
            ) {
                override fun isSettable(styleable: StatusIndicator): Boolean {
                    return styleable.trackBorderColorProperty.value == null || styleable.trackBorderColorProperty.isBound
                }

                override fun getStyleableProperty(styleable: StatusIndicator): StyleableProperty<Paint> {
                    return styleable.trackBorderColorProperty
                }
            }

        private val TRACK_BORDER: CssMetaData<StatusIndicator, BorderStrokeStyle> =
            object : CssMetaData<StatusIndicator, BorderStrokeStyle>(
                "-fx-track-border",
                StyleConverter<StatusIndicator, BorderStrokeStyle>(),
                BorderStrokeStyle.NONE
            ) {
                override fun isSettable(styleable: StatusIndicator): Boolean {
                    return styleable.trackBorderStyleProperty.value == null || styleable.trackBorderStyleProperty.isBound
                }

                override fun getStyleableProperty(styleable: StatusIndicator): StyleableProperty<BorderStrokeStyle> {
                    return styleable.trackBorderStyleProperty
                }
            }

        private val STYLEABLES: MutableList<CssMetaData<out Styleable, *>> = mutableListOf()

        object Obj {
            var styleables: MutableList<CssMetaData<out Styleable, *>> = Control.getClassCssMetaData()

            init {
                styleables.add(PRIMARY_FILL)
                styleables.add(ACCENT_FILL)
                styleables.add(TRACK_FILL)
                styleables.add(BAR_BORDER)
                styleables.add(BAR_BORDER_COLOR)
                styleables.add(TEXT_FILL)
                styleables.add(TRACK_BORDER)
                styleables.add(TRACK_BORDER_COLOR)
                STYLEABLES.addAll(styleables)
            }
        }
    }

    override fun getControlCssMetaData(): MutableList<CssMetaData<out Styleable, *>> {
        return getClassCssMetadata()
    }

    // fun getClassMetaData is public final so used getClassCssMetadata
    fun getClassCssMetadata(): MutableList<CssMetaData<out Styleable, *>> {
        return STYLEABLES
    }
}

fun statusindicator(init: StatusIndicator.() -> Unit = {}): StatusIndicator {
    val si = StatusIndicator()
    si.init()
    return si
}