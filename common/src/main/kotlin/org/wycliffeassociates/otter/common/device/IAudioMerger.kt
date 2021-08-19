package org.wycliffeassociates.otter.common.device

import io.reactivex.Completable
import java.io.File

interface IAudioMerger {
    fun merge(files: List<File>, output: File): Completable
}
