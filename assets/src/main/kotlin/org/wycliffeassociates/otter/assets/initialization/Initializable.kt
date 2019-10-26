package org.wycliffeassociates.otter.assets.initialization

import io.reactivex.Completable

interface Initializable {
    fun exec(): Completable
}