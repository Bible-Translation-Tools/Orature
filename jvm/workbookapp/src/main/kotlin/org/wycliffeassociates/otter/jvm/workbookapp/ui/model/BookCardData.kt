/**
 * Copyright (C) 2020-2022 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.workbookapp.ui.model

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.domain.resourcecontainer.artwork.Artwork
import tornadofx.FX
import tornadofx.get

class BookCardData(val collection: Collection, artwork: Observable<Artwork>) {
    val artworkProperty = SimpleObjectProperty<Artwork>()
    val attributionProperty = SimpleStringProperty()

    init {
        artwork
            .subscribeOn(Schedulers.io())
            .observeOnFx()
            .subscribe {
                artworkProperty.set(it)
                attributionProperty.set(
                    it.attributionText(
                        FX.messages["artworkLicense"],
                        FX.messages["artworkAttributionTitle"],
                        FX.messages["license"]
                    )
                )
            }
    }
}
