/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import com.jfoenix.controls.JFXSnackbar
import com.jfoenix.controls.JFXSnackbarLayout
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.geometry.HPos
import javafx.geometry.VPos
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.Control
import javafx.scene.input.DragEvent
import javafx.scene.input.Dragboard
import javafx.scene.input.TransferMode
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.RowConstraints
import javafx.scene.layout.VBox
import javafx.util.Duration
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadCrumb
import org.wycliffeassociates.otter.jvm.controls.card.ScriptureTakeCard
import org.wycliffeassociates.otter.jvm.controls.card.events.DeleteTakeEvent
import org.wycliffeassociates.otter.jvm.controls.card.events.TakeEvent
import org.wycliffeassociates.otter.jvm.controls.dialog.PluginOpenedPage
import org.wycliffeassociates.otter.jvm.controls.dialog.confirmdialog
import org.wycliffeassociates.otter.jvm.controls.dragtarget.DragTargetBuilder
import org.wycliffeassociates.otter.jvm.controls.media.SourceContent
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.SnackbarHandler
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginClosedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginOpenedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.TakeCardModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.AudioPluginViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.RecordScriptureViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*
import java.util.*

private const val TAKES_ROW_HEIGHT = 170.0

class RecordScripturePage : View() {
    private val logger = LoggerFactory.getLogger(RecordScripturePage::class.java)

    private val recordScriptureViewModel: RecordScriptureViewModel by inject()
    private val workbookDataStore: WorkbookDataStore by inject()
    private val navigator: NavigationMediator by inject()
    private val audioPluginViewModel: AudioPluginViewModel by inject()
    private val recordableViewModel = recordScriptureViewModel.recordableViewModel

    private val mainContainer = VBox()
    private val fileDragTarget = VBox()
    private val takesGrid = ScriptureTakesGridView(
        workbookDataStore.activeChunkProperty.isNull,
        recordableViewModel::recordNewTake
    )
    private val pluginOpenedPage: PluginOpenedPage

    private val isDraggingTakeProperty = SimpleBooleanProperty(false)
    private val isDraggingFileProperty = SimpleBooleanProperty(false)
    private val draggingNodeProperty = SimpleObjectProperty<Node>()

    private var importProgressListener: ChangeListener<Boolean>? = null
    private var importSuccessListener: ChangeListener<Boolean>? = null
    private var importFailListener: ChangeListener<Boolean>? = null

    private val dragTarget =
        DragTargetBuilder(DragTargetBuilder.Type.SCRIPTURE_TAKE)
            .build(isDraggingTakeProperty.toBinding())
            .apply {
                addClass("card--scripture-take--empty")
                recordableViewModel.selectedTakeProperty.onChangeAndDoNow { take ->
                    /* We can't just add the node being dragged, since the selected take might have just been
                        loaded from the database */
                    this.selectedNodeProperty.value = take?.let { createTakeCard(take) }
                }
            }

    private val dragContainer = VBox().apply {
        this.prefWidthProperty().bind(dragTarget.widthProperty())
        draggingNodeProperty.onChange { draggingNode ->
            clear()
            draggingNode?.let { add(draggingNode) }
        }
    }

    private val sourceContent =
        SourceContent().apply {
            sourceTextProperty.bind(workbookDataStore.sourceTextBinding())
            audioPlayerProperty.bind(recordableViewModel.sourceAudioPlayerProperty)

            audioNotAvailableTextProperty.set(messages["audioNotAvailable"])
            textNotAvailableTextProperty.set(messages["textNotAvailable"])
            playLabelProperty.set(messages["playSource"])
            pauseLabelProperty.set(messages["pauseSource"])

            contentTitleProperty.bind(workbookDataStore.activeChunkTitleBinding())
        }

    private val breadCrumb = BreadCrumb().apply {
        titleProperty.bind(recordScriptureViewModel.breadcrumbTitleBinding)
        iconProperty.set(FontIcon(MaterialDesign.MDI_LINK_OFF))
        onClickAction {
            navigator.dock(this@RecordScripturePage)
        }
    }

    override val root = anchorpane {
        addButtonEventHandlers()
        createSnackBar()

        add(mainContainer
            .apply {
                anchorpaneConstraints {
                    leftAnchor = 0.0
                    rightAnchor = 0.0
                    bottomAnchor = 0.0
                    topAnchor = 0.0
                }
            }
        )
        add(fileDragTarget
            .apply {
                anchorpaneConstraints {
                    leftAnchor = 0.0
                    rightAnchor = 0.0
                    bottomAnchor = 0.0
                    topAnchor = 0.0
                }
            }
        )
        add(dragContainer)
    }

    init {
        importStylesheet(resources.get("/css/record-scripture.css"))
        importStylesheet(resources.get("/css/audioplayer.css"))
        importStylesheet(resources.get("/css/takecard.css"))
        importStylesheet(resources.get("/css/scripturetakecard.css"))

        isDraggingTakeProperty.onChange {
            if (it) recordableViewModel.stopPlayers()
        }
        isDraggingFileProperty.onChange {
            if (it) recordableViewModel.stopPlayers()
        }

        pluginOpenedPage = createPluginOpenedPage()
        workspace.subscribe<PluginOpenedEvent> { pluginInfo ->
            if (!pluginInfo.isNative) {
                workspace.dock(pluginOpenedPage)
                recordableViewModel.openSourceAudioPlayer()
            }
        }
        workspace.subscribe<PluginClosedEvent> {
            (workspace.dockedComponentProperty.value as? PluginOpenedPage)?.let {
                workspace.navigateBack()
            }
            recordableViewModel.openPlayers()
        }

        recordableViewModel.takeCardModels.onChangeAndDoNow {
            takesGrid.gridItems.setAll(it)
        }

        recordableViewModel.selectedTakeProperty.onChangeAndDoNow {
            if (it != null) {
                dragTarget.selectedNodeProperty.set(createTakeCard(it))
            }
        }

        dragTarget.setOnDragDropped {
            val db: Dragboard = it.dragboard
            var success = false
            if (db.hasString()) {
                recordableViewModel.selectTake(db.string)
                success = true
            }
            (it.source as? ScriptureTakeCard)?.let {
                it.isDraggingProperty().value = false
            }
            it.isDropCompleted = success
            it.consume()
        }

        dragTarget.setOnDragOver {
            if (it.gestureSource != dragTarget && it.dragboard.hasString()) {
                it.acceptTransferModes(*TransferMode.ANY)
            }
            it.consume()
        }

        fileDragTarget.setOnDragOver {
            if (it.gestureSource != fileDragTarget && it.dragboard.hasFiles()) {
                it.acceptTransferModes(*TransferMode.ANY)
            }
            it.consume()
        }

        fileDragTarget.setOnDragDropped {
            val db: Dragboard = it.dragboard
            var success = false
            if (db.hasFiles()) {
                recordableViewModel.importTakes(db.files)
                success = true
            }
            it.isDropCompleted = success
            it.consume()
        }

        mainContainer.apply {
            addEventHandler(DragEvent.DRAG_ENTERED) {
                if (it.dragboard.hasFiles()) {
                    isDraggingFileProperty.value = true
                } else {
                    isDraggingTakeProperty.value = true
                }
            }
            addEventHandler(DragEvent.DRAG_EXITED) {
                isDraggingTakeProperty.value = false
            }

            addClass("card--main-container")

            hgrow = Priority.ALWAYS
            // Top items above the alternate takes
            // Drag target and/or selected take, Next Verse Button, Previous Verse Button
            hbox {
                addClass("record-scripture__top")

                // previous verse button
                button(messages["previousVerse"]) {
                    addClass("btn", "btn--secondary")
                    graphic = FontIcon(MaterialDesign.MDI_ARROW_LEFT)
                    action {
                        recordScriptureViewModel.previousChunk()
                    }
                    enableWhen(recordScriptureViewModel.hasPrevious)
                }
                vbox {
                    add(dragTarget)
                }

                // next verse button
                button(messages["nextVerse"]) {
                    addClass("btn", "btn--secondary")
                    graphic = FontIcon(MaterialDesign.MDI_ARROW_RIGHT)
                    action {
                        recordScriptureViewModel.nextChunk()
                    }
                    enableWhen(recordScriptureViewModel.hasNext)
                }
            }

            gridpane {
                vgrow = Priority.ALWAYS
                hgrow = Priority.ALWAYS

                add(takesGrid, 0, 0)
                add(sourceContent, 0, 1)

                columnConstraints.addAll(
                    ColumnConstraints(
                        0.0,
                        0.0,
                        Double.MAX_VALUE,
                        Priority.ALWAYS,
                        HPos.LEFT,
                        true
                    )
                )

                val takesRowConstraints = RowConstraints(
                    TAKES_ROW_HEIGHT,
                    TAKES_ROW_HEIGHT,
                    Double.MAX_VALUE,
                    Priority.ALWAYS,
                    VPos.CENTER,
                    true
                )

                val sourceContentRowConstraints = RowConstraints(
                    Region.USE_COMPUTED_SIZE,
                    Region.USE_COMPUTED_SIZE,
                    Region.USE_COMPUTED_SIZE,
                    Priority.NEVER,
                    VPos.CENTER,
                    false
                )

                rowConstraints.add(takesRowConstraints)
                rowConstraints.add(sourceContentRowConstraints)
            }
        }

        fileDragTarget.apply {
            visibleProperty().bind(isDraggingFileProperty)
            isDraggingFileProperty.onChange {
                toggleClass("card--container-dragover", it)
            }
            addEventHandler(DragEvent.DRAG_EXITED) {
                isDraggingFileProperty.value = false
            }
        }
    }

    private fun Parent.addButtonEventHandlers() {
        addEventHandler(DeleteTakeEvent.DELETE_TAKE) {
            recordableViewModel.deleteTake(it.take)
        }
        addEventHandler(TakeEvent.EDIT_TAKE) {
            recordableViewModel.processTakeWithPlugin(it, PluginType.EDITOR)
        }
        addEventHandler(TakeEvent.MARK_TAKE) {
            recordableViewModel.processTakeWithPlugin(it, PluginType.MARKER)
        }
    }

    private fun createTakeCard(take: TakeCardModel): Control {
        return ScriptureTakeCard().apply {
            audioPlayerProperty().set(take.audioPlayer)
            deleteTextProperty().set(take.deleteText)
            editTextProperty().set(take.editText)
            pauseTextProperty().set(take.playText)
            playTextProperty().set(take.playText)
            markerTextProperty().set(take.markerText)
            takeProperty().set(take.take)
            takeNumberProperty().set(take.take.number.toString())
            allowMarkerProperty().bind(recordableViewModel.workbookDataStore.activeChunkProperty.isNull)
        }
    }

    private fun createSnackBar() {
        recordableViewModel
            .snackBarObservable
            .doOnError { e ->
                logger.error("Error in creating no plugin snackbar", e)
            }
            .subscribe { pluginErrorMessage ->
                SnackbarHandler.enqueue(
                    JFXSnackbar.SnackbarEvent(
                        JFXSnackbarLayout(
                            pluginErrorMessage,
                            messages["addPlugin"].uppercase(Locale.getDefault())
                        ) {
                            audioPluginViewModel.addPlugin(true, false)
                        },
                        Duration.millis(5000.0),
                        null
                    )
                )
            }
    }

    private fun createPluginOpenedPage(): PluginOpenedPage {
        // Plugin active cover
        return PluginOpenedPage().apply {
            dialogTitleProperty.bind(recordableViewModel.dialogTitleBinding())
            dialogTextProperty.bind(recordableViewModel.dialogTextBinding())
            playerProperty.bind(recordableViewModel.sourceAudioPlayerProperty)
            audioAvailableProperty.bind(recordableViewModel.sourceAudioAvailableProperty)
            sourceTextProperty.bind(workbookDataStore.sourceTextBinding())
            sourceContentTitleProperty.bind(workbookDataStore.activeChunkTitleBinding())
        }
    }

    private fun initializeImportProgressDialog() {
        confirmdialog {
            titleTextProperty.set(messages["importTakesTitle"])
            messageTextProperty.set(messages["importTakesMessage"])

            importProgressListener = ChangeListener { _, _, value ->
                if (value) open() else close()
            }
            recordableViewModel.showImportProgressDialogProperty.addListener(importProgressListener)

            progressTitleProperty.set(messages["pleaseWait"])
            showProgressBarProperty.set(true)
        }
    }

    private fun initializeImportSuccessDialog() {
        confirmdialog {
            titleTextProperty.set(messages["importTakesTitle"])
            messageTextProperty.set(messages["importTakesSuccessMessage"])
            cancelButtonTextProperty.set(messages["close"])

            importSuccessListener = ChangeListener { _, _, value ->
                if (value) open() else close()
            }
            recordableViewModel.showImportSuccessDialogProperty.addListener(importSuccessListener)

            onCloseAction { recordableViewModel.showImportSuccessDialogProperty.set(false) }
            onCancelAction { recordableViewModel.showImportSuccessDialogProperty.set(false) }
        }
    }

    private fun initializeImportFailDialog() {
        confirmdialog {
            titleTextProperty.set(messages["importTakesTitle"])
            messageTextProperty.set(messages["importTakesFailMessage"])
            cancelButtonTextProperty.set(messages["close"])

            importFailListener = ChangeListener { _, _, value ->
                if (value) open() else close()
            }
            recordableViewModel.showImportFailDialogProperty.addListener(importFailListener)

            onCloseAction { recordableViewModel.showImportFailDialogProperty.set(false) }
            onCancelAction { recordableViewModel.showImportFailDialogProperty.set(false) }
        }
    }

    private fun removeDialogListeners() {
        recordableViewModel.showImportProgressDialogProperty.removeListener(importProgressListener)
        recordableViewModel.showImportFailDialogProperty.removeListener(importFailListener)
        recordableViewModel.showImportSuccessDialogProperty.removeListener(importSuccessListener)
    }

    override fun onUndock() {
        super.onUndock()
        recordableViewModel.closePlayers()
        removeDialogListeners()
    }

    override fun onDock() {
        super.onDock()
        recordableViewModel.openPlayers()
        recordableViewModel.currentTakeNumberProperty.set(null)
        navigator.dock(this, breadCrumb)

        initializeImportProgressDialog()
        initializeImportFailDialog()
        initializeImportSuccessDialog()
    }
}
