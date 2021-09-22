package org.wycliffeassociates.otter.common.domain.collections

import io.reactivex.Completable
import org.wycliffeassociates.otter.common.data.workbook.Translation
import org.wycliffeassociates.otter.common.persistence.repositories.ILanguageRepository
import javax.inject.Inject

class UpdateTranslation @Inject constructor(
    private val languageRepo: ILanguageRepository
) {
    fun update(translation: Translation): Completable {
        return languageRepo.updateTranslation(translation)
    }
}
