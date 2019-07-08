package org.wycliffeassociates.otter.jvm.app.ui.takemanagement.viewmodel

import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.domain.content.*
import org.wycliffeassociates.otter.common.domain.plugins.LaunchPlugin
import org.wycliffeassociates.otter.jvm.app.ui.addplugin.view.AddPluginView
import org.wycliffeassociates.otter.jvm.app.ui.addplugin.viewmodel.AddPluginViewModel
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.app.ui.workbook.viewmodel.WorkbookViewModel
import org.wycliffeassociates.otter.jvm.persistence.WaveFileCreator
import tornadofx.*

class TakeManagementViewModel: ViewModel() {
    private val injector: Injector by inject()
    private val pluginRepository = injector.pluginRepository

    private val workbookViewModel: WorkbookViewModel by inject()

    private val launchPlugin = LaunchPlugin(pluginRepository)

    private val recordTake = RecordTake(
        WaveFileCreator(),
        LaunchPlugin(pluginRepository)
    )

    private val editTake = EditTake(launchPlugin)

    fun audioPlayer(): IAudioPlayer = injector.audioPlayer

    fun addPlugin(record: Boolean, edit: Boolean) {
        find<AddPluginViewModel>().apply {
            canRecord = record
            canEdit = edit
        }
        find<AddPluginView>().openModal()
    }

    private fun createFileNamer(recordable: Recordable) = WorkbookFileNamerBuilder
        .createFileNamer(
            workbookViewModel.workbook,
            workbookViewModel.chapter,
            workbookViewModel.chunk,
            recordable,
            workbookViewModel.resourceSlug
        )

    fun edit(take: Take) = editTake.edit(take)

    fun record(recordable: Recordable) = recordTake
        .record(
            recordable.audio,
            workbookViewModel.projectAudioDirectory,
            createFileNamer(recordable)
        )
}