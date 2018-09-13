package org.wycliffeassociates.otter.jvm.app.widgets

import java.io.File
import java.util.*

data class Take(
        var number: Int,
        var date: Date,
        var file: File,
        var played: Boolean = false
)