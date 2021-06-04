package org.wycliffeassociates.otter.common.domain.collections

import io.reactivex.Single
import io.reactivex.rxkotlin.flatMapIterable
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.workbook.Translation
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ILanguageRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceMetadataRepository
import javax.inject.Inject

class CreateTranslation @Inject constructor(
    private val languageRepo: ILanguageRepository
) {
    /**
     * Create translation based on source and target languages
     */
    fun create(
        sourceLanguage: Language,
        targetLanguage: Language
    ): Single<Int> {
        val translation = Translation(sourceLanguage, targetLanguage)
        return languageRepo.insertTranslation(translation)
    }
}
