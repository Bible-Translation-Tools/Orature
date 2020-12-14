package org.wycliffeassociates.otter.jvm.markerapp.app.view

import com.sun.glass.ui.Screen
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.SECONDS_ON_SCREEN

private const val EPSILON = 10
private const val MS_IN_SECOND = 1000

internal fun pixelsInSecond(width: Double, durationMs: Int): Int {
    val msInPixels = durationMs / width
    return (MS_IN_SECOND / msInPixels + EPSILON).toInt()
}

internal fun positionToMs(x: Int, width: Double, durationMs: Int): Int {
    val msInPixels = durationMs / width
    return (x * msInPixels).toInt()
}

internal fun pixelsToFrames(pixels: Double): Int {
    val framerate = 44100
    val framesOnScreen = SECONDS_ON_SCREEN * framerate
    val framesInPixel = framesOnScreen / Screen.getMainScreen().platformWidth.toDouble()
    return (pixels * framesInPixel).toInt()
}

internal fun framesToPixels(frames: Int): Int {
    val framerate = 44100
    val framesOnScreen = SECONDS_ON_SCREEN * framerate
    val framesInPixel = framesOnScreen / Screen.getMainScreen().platformWidth.toDouble()
    return (frames / framesInPixel).toInt()
}

internal fun pixelsToFrames(pixels: Double, scale: Double): Int {
    return (pixels * scale).toInt()
}
