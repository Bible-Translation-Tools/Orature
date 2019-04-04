package org.wycliffeassociates.otter.jvm.statusindicator.control

import javafx.beans.property.DoubleProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.VPos
import javafx.scene.control.SkinBase
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.Stop
import tornadofx.*


class StatusIndicatorSkin(control: StatusIndicator) : SkinBase<StatusIndicator>(control) {

    private var bar: StackPane
    private var track: StackPane
    private var barWidth: Double = 0.0


    private var invalidBar = true
    private val localProgressProperty: DoubleProperty = SimpleDoubleProperty(0.0)
    private val skinPrimaryFill: ObjectProperty<Color> = SimpleObjectProperty<Color>(Color.RED)
    private val skinAccentFill: ObjectProperty<Color> = SimpleObjectProperty<Color>(Color.RED)
    private val skinTrackColor: ObjectProperty<Color> = SimpleObjectProperty<Color>(Color.WHITE)
    private val barHeight: DoubleProperty = SimpleDoubleProperty(0.0)
    private val indicatorRadius: DoubleProperty = SimpleDoubleProperty(0.0)


    init {
        //need to define super(statusindicator)? not sure if this is required atm
        bar = StackPane()
        bar.styleClass.setAll("bar")

        track = StackPane()
        track.styleClass.setAll("track")

        children.setAll(track, bar)

        control.widthProperty().onChange { invalidBar = true }
        control.heightProperty().onChange {invalidBar = true }
        control.primaryFillProperty.onChange { updateBarFill(control.primaryFill, control.accentFill) }
        control.accentFillProperty.onChange { updateBarFill(control.primaryFill, control.accentFill) }
        control.progressProperty.onChange { updateStatusIndicator(control.width, control.height)
        }
        skinnable.requestLayout()

        localProgressProperty.bind(control.progressProperty)
        skinPrimaryFill.bind(control.primaryFillProperty)
        skinAccentFill.bind(control.accentFillProperty)
        barHeight.bind(control.barHeightProperty)
        indicatorRadius.bind(control.indicatorRadiusProperty)
        skinTrackColor.bind(control.trackFillProperty)
    }

    fun updateBarFill(primaryFill: Color, accentFill: Color) {
        val stops = listOf(Stop(0.0, primaryFill), Stop(1.0, accentFill))
        if (bar != null && track != null) {
            bar.background = Background(
                BackgroundFill(
                    LinearGradient(
                        0.0,
                        0.0,
                        0.05,
                        0.7,
                        true,
                        CycleMethod.REPEAT,
                        stops
                    ),
                    CornerRadii(0.0),
                    Insets(0.0)
                )
            )
            skinnable.requestLayout()
        }
    }

    fun updateStatusIndicator(width: Double, height: Double) {
        val stops = listOf(Stop(0.0, skinPrimaryFill.value), Stop(1.0, skinAccentFill.value))

        if (bar != null && track != null) {
            children.remove(bar)
            children.remove(track)
        }

        bar = StackPane()
        track = StackPane()
        if (localProgressProperty.value <= 1.0000001) {
            barWidth =
                ((localProgressProperty.value * width) - snappedLeftInset() - snappedRightInset()).toInt().toDouble()
        }

        track.background = Background(
            BackgroundFill(
                skinTrackColor.value,
                CornerRadii(indicatorRadius.value),
                Insets(1.0)
            )
        )
        bar.background = Background(
            BackgroundFill(
                LinearGradient(
                    0.0,
                    0.0,
                    0.1,
                    0.5,
                    true,
                    CycleMethod.REFLECT,
                    stops
                ),
                CornerRadii(indicatorRadius.value),
                Insets(1.0)
            )
        )

        bar.styleClass.setAll("bar")
        track.styleClass.setAll("track")
        children.setAll(track, bar)

    }

    override fun layoutChildren(contentX: Double, contentY: Double, contentWidth: Double, contentHeight: Double) {
        if (invalidBar) {
            updateStatusIndicator(contentWidth, contentHeight)
        }
        track.resizeRelocate(contentX, contentY, contentWidth, contentHeight)
        if (barHeight.value == 0.0) {
            bar.resizeRelocate(contentX, contentY, barWidth, contentHeight)
            layoutInArea(track, contentX, contentY, contentWidth, contentHeight, -1.0, HPos.CENTER, VPos.CENTER)
            layoutInArea(bar, contentX, contentY, barWidth, contentHeight, -1.0, HPos.CENTER, VPos.CENTER)
        } else {
            bar.resizeRelocate(contentX, contentY, barWidth, barHeight.value)
            layoutInArea(track, contentX, contentY, contentWidth, contentHeight, -1.0, HPos.CENTER, VPos.CENTER)
            layoutInArea(
                bar,
                contentX,
                contentY - (barHeight.value / 4.0),
                barWidth,
                barHeight.value,
                -1.0,
                HPos.CENTER,
                VPos.CENTER
            )
        }

        track.isVisible = true
        bar.isVisible = true
    }


    override fun computePrefHeight(
        width: Double,
        topInset: Double,
        rightInset: Double,
        bottomInset: Double,
        leftInset: Double
    ): Double {
        return topInset + bottomInset + 10
    }

    override fun computePrefWidth(
        height: Double,
        topInset: Double,
        rightInset: Double,
        bottomInset: Double,
        leftInset: Double
    ): Double {
        return rightInset + leftInset + 200
    }

    override fun computeMinHeight(
        width: Double,
        topInset: Double,
        rightInset: Double,
        bottomInset: Double,
        leftInset: Double
    ): Double {
        return 7.5 + topInset + bottomInset
    }

    override fun computeMinWidth(
        height: Double,
        topInset: Double,
        rightInset: Double,
        bottomInset: Double,
        leftInset: Double
    ): Double {
        return 7.5 + rightInset + leftInset
    }

    override fun computeMaxHeight(
        width: Double,
        topInset: Double,
        rightInset: Double,
        bottomInset: Double,
        leftInset: Double
    ): Double {
        return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset)
    }

    override fun computeMaxWidth(
        height: Double,
        topInset: Double,
        rightInset: Double,
        bottomInset: Double,
        leftInset: Double
    ): Double {
        return computePrefWidth(height, topInset, rightInset, bottomInset, leftInset)
    }

    fun updateProgress(progress: Double) {
        barWidth = (progress - snappedLeftInset() - snappedRightInset()).toInt().toDouble()
    }

}