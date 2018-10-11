package org.wycliffeassociates.otter.jvm.app.ui.viewtakes.viewmodel


import org.wycliffeassociates.otter.common.data.model.Take
import org.wycliffeassociates.otter.jvm.app.ui.viewtakes.model.ViewTakesModel

import tornadofx.*

class ViewTakesViewModel : ViewModel() {
    val model = ViewTakesModel()

    val titleProperty = bind { model.titleProperty }
    val selectedTakeProperty = bind(autocommit = true) { model.selectedTakeProperty }
    val alternateTakes = model.alternateTakes

    fun acceptTake(take: Take) {
        model.acceptTake(take)
    }

    fun setTakePlayed(take: Take) {
        model.setTakePlayed(take)
    }

    fun reset() {
        model.reset()
    }
}