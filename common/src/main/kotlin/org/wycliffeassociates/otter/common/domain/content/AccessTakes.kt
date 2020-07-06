package org.wycliffeassociates.otter.common.domain.content

import io.reactivex.Completable
import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.model.Content
import org.wycliffeassociates.otter.common.data.model.Take
import org.wycliffeassociates.otter.common.persistence.repositories.IContentRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ITakeRepository

class AccessTakes(
    private val contentRepo: IContentRepository,
    private val takeRepo: ITakeRepository
) {
    fun getByContent(content: Content): Single<List<Take>> {
        return takeRepo.getByContent(content)
    }

    fun setSelectedTake(content: Content, selectedTake: Take?): Completable {
        content.selectedTake = selectedTake
        return contentRepo.update(content)
    }

    fun setTakePlayed(take: Take, played: Boolean): Completable {
        take.played = played
        return takeRepo.update(take)
    }

    fun delete(take: Take): Completable {
        return takeRepo.delete(take)
    }

    fun getTakeCount(content: Content): Single<Int> {
        return takeRepo
            .getByContent(content)
            .map { it.size }
    }
}
