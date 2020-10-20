package org.wycliffeassociates.otter.jvm.markerapp.app.view

const val epsilon = 10
const val MS_IN_SECOND = 1000

internal fun pixelsInSecond(width: Double, durationMs: Int): Int {
    val msInPixels = durationMs / width
    return (MS_IN_SECOND / msInPixels + epsilon).toInt()
}

internal fun positionToMs(x: Int, width: Double, durationMs: Int): Int {
    val msInPixels = durationMs / width
    return (x * msInPixels).toInt()
}
