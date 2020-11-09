package org.wycliffeassociates.otter.jvm.markerapp.app.view

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
