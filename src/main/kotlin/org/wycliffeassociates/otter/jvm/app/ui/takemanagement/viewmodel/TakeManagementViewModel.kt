package org.wycliffeassociates.otter.jvm.app.ui.takemanagement.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.model.*
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.workbook.AssociatedAudio
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.domain.content.*
import org.wycliffeassociates.otter.common.domain.plugins.LaunchPlugin
import org.wycliffeassociates.otter.jvm.app.ui.addplugin.view.AddPluginView
import org.wycliffeassociates.otter.jvm.app.ui.addplugin.viewmodel.AddPluginViewModel
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.TakeContext
import org.wycliffeassociates.otter.jvm.app.ui.workbook.viewmodel.WorkbookViewModel
import org.wycliffeassociates.otter.jvm.persistence.WaveFileCreator
import tornadofx.*
import java.io.File

class TakeManagementViewModel: ViewModel() {
    private val injector: Injector by inject()
    private val pluginRepository = injector.pluginRepository
    private val directoryProvider = injector.directoryProvider
    private val collectionRepository = injector.collectionRepo
    private val contentRepository = injector.contentRepository
    private val takeRepository = injector.takeRepository

    private val workbookViewModel: WorkbookViewModel by inject()

    private val launchPlugin = LaunchPlugin(pluginRepository)

    private val recordTake = RecordTake(
        WaveFileCreator(),
        LaunchPlugin(pluginRepository)
    )

    private val editTake = EditTake(launchPlugin)

    private val accessTakes = AccessTakes(
        contentRepository,
        takeRepository
    )

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

    fun record(recordable: Recordable) = recordTake
        .record(
            recordable.audio,
            workbookViewModel.projectAudioDirectory,
            createFileNamer(recordable)
        )

    fun edit(take: Take) = editTake.edit(take)

    fun recordNewTake(recordable: Recordable) {
        recordTake.record(
            recordable.audio,
            workbookViewModel.projectAudioDirectory,
            createFileNamer(recordable)
        ).observeOnFx()
            // Subscribing on an I/O thread is not completely necessary but it is is safer
            .subscribeOn(Schedulers.io())
            .subscribe()
    }
}