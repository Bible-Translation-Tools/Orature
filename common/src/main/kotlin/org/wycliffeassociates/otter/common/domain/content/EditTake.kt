package org.wycliffeassociates.otter.common.domain.content

import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.PluginParameters
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.domain.plugins.LaunchPlugin

class EditTake(
    private val launchPlugin: LaunchPlugin
) {
    enum class Result {
        SUCCESS,
        NO_EDITOR
    }

    fun edit(take: Take, pluginParameters: PluginParameters): Single<Result> = launchPlugin
        .launchEditor(take.file, pluginParameters)
        .map {
            when (it) {
                LaunchPlugin.Result.SUCCESS -> Result.SUCCESS
                LaunchPlugin.Result.NO_PLUGIN -> Result.NO_EDITOR
            }
        }
}