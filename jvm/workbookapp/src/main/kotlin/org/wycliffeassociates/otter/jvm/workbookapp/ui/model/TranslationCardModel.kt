package org.wycliffeassociates.otter.jvm.workbookapp.ui.model

import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.workbook.Workbook

class TranslationCardModel(
    val sourceLanguage: Language,
    val targetLanguage: Language,
    val books: ObservableList<Workbook>
)
