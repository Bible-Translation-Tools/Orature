package org.wycliffeassociates.otter.common.domain.content

import io.reactivex.Completable
import org.wycliffeassociates.otter.common.data.model.Take
import org.wycliffeassociates.otter.common.domain.plugins.LaunchPlugin
import org.wycliffeassociates.otter.common.persistence.repositories.ITakeRepository
import java.time.LocalDate

class EditTake(
        private val takeRepository: ITakeRepository,
        private val launchPlugin: LaunchPlugin
) {
    fun edit(take: Take): Completable {
        take.timestamp = LocalDate.now()
        return launchPlugin
                .launchEditor(take.path)
                .concatWith(takeRepository.update(take))
    }
}