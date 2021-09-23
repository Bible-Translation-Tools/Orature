package org.wycliffeassociates.otter.common.domain.collections

import io.reactivex.Completable
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import javax.inject.Inject

class UpdateProject @Inject constructor(
    private val collectionRepo: ICollectionRepository
) {
    fun update(project: Collection): Completable {
        return collectionRepo.update(project)
    }
}
