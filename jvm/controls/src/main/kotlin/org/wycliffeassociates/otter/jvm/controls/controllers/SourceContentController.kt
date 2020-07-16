package org.wycliffeassociates.otter.jvm.controls.controllers

import javafx.beans.binding.Bindings
import javafx.beans.binding.StringBinding
import javafx.beans.property.StringProperty
import java.util.concurrent.Callable

class SourceContentController {

    fun titleBinding(
        bookTitleProperty: StringProperty,
        chapterTitleProperty: StringProperty,
        chunkTitleProperty: StringProperty
    ): StringBinding {
        return Bindings.createStringBinding(
            Callable {
                val titleStringBuilder = StringBuilder()

                bookTitleProperty.value?.let {
                    titleStringBuilder
                        .append(it)
                }

                chapterTitleProperty.value?.let {
                    titleStringBuilder
                        .append(" ")
                        .append(it)
                }

                chunkTitleProperty.value?.let {
                    titleStringBuilder
                        .append(":")
                        .append(it)
                }

                titleStringBuilder.toString()
            },
            bookTitleProperty,
            chapterTitleProperty,
            chunkTitleProperty
        )
    }
}
