package org.wycliffeassociates.otter.common.device

import io.reactivex.Maybe
import java.io.File

interface IAudioMerger {
    fun merge(files: List<File>): Maybe<File>
}
