package org.wycliffeassociates.otter.jvm.statusindicator.control

import com.sun.javafx.css.converters.ColorConverter
import com.sun.javafx.css.converters.PaintConverter
import javafx.beans.property.*
import javafx.css.*
import javafx.geometry.Dimension2D
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.VPos
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.*
import tornadofx.*

class StatusIndicator : Control() {

    var primaryFillProperty: StyleableObjectProperty<Color> =
        object : SimpleStyleableObjectProperty<Color>(
            PRIMARY_FILL,
            StatusIndicator,
            "primaryFill",
            Color.RED
        ) {
        }

    var primaryFill: Color by primaryFillProperty


    var accentFillProperty: StyleableObjectProperty<Color> =
        object : SimpleStyleableObjectProperty<Color>(
            ACCENT_FILL,
            StatusIndicator,
            "accentFill",
            Color.RED
        ) {
        }
    var accentFill: Color by accentFillProperty

    var trackFillProperty: StyleableObjectProperty<Color> =
        object : SimpleStyleableObjectProperty<Color>(
            TRACK_FILL,
            StatusIndicator,
            "trackFill",
            Color.WHITE
        ) {
        }
    var trackFill: Color by trackFillProperty


    val progressProperty: DoubleProperty = SimpleDoubleProperty(0.0)
    var progress: Double by progressProperty

    val indicatorRadiusProperty: DoubleProperty = SimpleDoubleProperty(0.0)
    var indicatorRadius by indicatorRadiusProperty

    val barHeightProperty: DoubleProperty = SimpleDoubleProperty(0.0)
    var barHeight: Double by barHeightProperty

    private val isCompleteProperty: BooleanProperty = SimpleBooleanProperty(false)
    var isComplete by isCompleteProperty

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

    fun progressProperty(): DoubleProperty {
        return progressProperty
    }

    fun setProgressProperty(progress: Double) {
        this.progressProperty.set(progress)
    }

    fun primaryFillProperty(): StyleableObjectProperty<Color> {
        if (primaryFill == null) {
            primaryFillProperty = SimpleStyleableObjectProperty<Color>(
                StyleableProperties.PRIMARY_FILL,
                this,
                "primaryFill",
                Color.HOTPINK
            )
        }
        return primaryFillProperty
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

        private val STYLEABLES: MutableList<CssMetaData<out Styleable, *>> = mutableListOf()

        object Obj {
            var styleables: MutableList<CssMetaData<out Styleable, *>> = Control.getClassCssMetaData()

            init {
                styleables.add(PRIMARY_FILL)
                styleables.add(ACCENT_FILL)
                styleables.add(TRACK_FILL)
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