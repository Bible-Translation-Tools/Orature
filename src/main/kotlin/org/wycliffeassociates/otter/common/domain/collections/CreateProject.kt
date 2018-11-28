package org.wycliffeassociates.otter.common.domain.collections

import io.reactivex.Completable
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository

class CreateProject(
        private val collectionRepo: ICollectionRepository
) {
    fun create(sourceProject: Collection, targetLanguage: Language): Completable {
        // Some concat maps can be removed when dao synchronization is added
        if (sourceProject.resourceContainer == null) throw NullPointerException("Source project has no metadata")
        return collectionRepo.deriveProject(sourceProject, targetLanguage)
    }
}