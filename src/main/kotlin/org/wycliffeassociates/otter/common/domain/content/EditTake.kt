package org.wycliffeassociates.otter.common.domain.content

import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.model.Take
import org.wycliffeassociates.otter.common.domain.plugins.LaunchPlugin
import org.wycliffeassociates.otter.common.persistence.repositories.ITakeRepository
import java.time.LocalDate

class EditTake(
        private val takeRepository: ITakeRepository,
        private val launchPlugin: LaunchPlugin
) {
    enum class Result {
        SUCCESS,
        NO_EDITOR
    }
    fun edit(take: Take): Single<Result> {
        take.created = LocalDate.now()
        return launchPlugin
                .launchEditor(take.path)
                .flatMap {
                    when (it) {
                        LaunchPlugin.Result.SUCCESS -> takeRepository.update(take).toSingle { Result.SUCCESS }
                        LaunchPlugin.Result.NO_PLUGIN -> Single.just(Result.NO_EDITOR)
                    }
                }
    }
}