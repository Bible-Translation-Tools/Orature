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
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ChangeListener
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.Button
import javafx.scene.control.Control
import javafx.scene.effect.DropShadow
import javafx.scene.input.DragEvent
import javafx.scene.input.Dragboard
import javafx.scene.input.TransferMode
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.util.Duration
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.jvm.controls.button.highlightablebutton
import org.wycliffeassociates.otter.jvm.controls.card.ResourceTakeCard
import org.wycliffeassociates.otter.jvm.controls.card.events.DeleteTakeEvent
import org.wycliffeassociates.otter.jvm.controls.card.events.TakeEvent
import org.wycliffeassociates.otter.jvm.controls.dialog.PluginOpenedPage
import org.wycliffeassociates.otter.jvm.controls.dialog.confirmdialog
import org.wycliffeassociates.otter.jvm.controls.dragtarget.DragTargetBuilder
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.SnackbarHandler
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginClosedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginOpenedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppTheme
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.TakeCardModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.AudioPluginViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.RecordResourceViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.RecordableViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

class RecordResourceFragment(private val recordableViewModel: RecordableViewModel) : Fragment() {
    private val logger = LoggerFactory.getLogger(RecordResourceFragment::class.java)

    private val recordResourceViewModel: RecordResourceViewModel by inject()
    private val audioPluginViewModel: AudioPluginViewModel by inject()
    private val workbookDataStore: WorkbookDataStore by inject()

    val formattedTextProperty = SimpleStringProperty()
    private val isDraggingTakeProperty = SimpleBooleanProperty(false)
    private val isDraggingFileProperty = SimpleBooleanProperty(false)
    private val draggingNodeProperty = SimpleObjectProperty<Node>()

    private var importProgressListener: ChangeListener<Boolean>? = null
    private var importSuccessListener: ChangeListener<Boolean>? = null
    private var importFailListener: ChangeListener<Boolean>? = null

    val dragTarget =
        DragTargetBuilder(DragTargetBuilder.Type.RESOURCE_TAKE)
            .build(isDraggingTakeProperty.toBinding())
            .apply {
                addClass("card--resource-take--empty")
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

    private val pluginOpenedPage: PluginOpenedPage

    private val alternateTakesList = TakesListView(
        items = recordableViewModel.takeCardModels,
        createTakeNode = ::createTakeCard
    )

    private val mainContainer = VBox()
    private val fileDragTarget = VBox()

    override val root: Parent = anchorpane {
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

    private val newTakeButton =
        highlightablebutton {
            highlightColor = AppTheme.colors.appBlue
            secondaryColor = AppTheme.colors.white
            isHighlighted = true
            graphic = FontIcon("gmi-mic").apply { iconSize = 25 }
            maxWidth = 500.0
            text = messages["record"]

            action {
                recordableViewModel.recordNewTake()
            }
        }

    private val previousButton = Button().apply {
        addClass("btn", "btn--secondary", "card__navigate-button")
        text = messages["previousChunk"]
        graphic = FontIcon(MaterialDesign.MDI_ARROW_LEFT)
        action {
            recordableViewModel.stopPlayers()
            recordResourceViewModel.previousChunk()
        }
        enableWhen(recordResourceViewModel.hasPrevious)
    }

    private val nextButton = Button().apply {
        addClass("btn", "btn--secondary", "card__navigate-button")
        text = messages["nextChunk"]
        graphic = FontIcon(MaterialDesign.MDI_ARROW_RIGHT)
        action {
            recordableViewModel.stopPlayers()
            recordResourceViewModel.nextChunk()
        }
        enableWhen(recordResourceViewModel.hasNext)
    }

    private val leftRegion = VBox().apply {
        vgrow = Priority.ALWAYS

        hbox {
            addClass("card__dragtarget-region")
            add(
                dragTarget.apply {
                    hgrow = Priority.ALWAYS
                }
            )
        }

        scrollpane {
            addClass("card__content-scrollpane")
            isFitToWidth = true
            vgrow = Priority.ALWAYS
            label(formattedTextProperty) {
                isWrapText = true
                addClass("card__content-text")
            }
        }

        vbox {
            addClass("card__new-takes-region")
            add(newTakeButton).apply {
                effect = DropShadow(2.0, 0.0, 3.0, Color.valueOf("#0d4082"))
            }
        }
    }

    private val grid = gridpane {
        vgrow = Priority.ALWAYS
        addClass("card--takes-tab")

        constraintsForRow(0).percentHeight = 90.0
        constraintsForRow(1).percentHeight = 10.0

        row {
            vbox {
                gridpaneColumnConstraints {
                    percentWidth = 50.0
                }
                addClass("card--left-region-container")
                add(leftRegion)
            }
            vbox(20.0) {
                gridpaneColumnConstraints {
                    percentWidth = 50.0
                }
                addClass("card__right-region")
                add(alternateTakesList)
            }
        }

        row {
            vbox {
                addClass("card__button-cell")
                gridpaneColumnConstraints {
                    percentWidth = 50.0
                }
                add(previousButton)
            }

            vbox {
                addClass("card__button-cell")
                gridpaneColumnConstraints {
                    percentWidth = 50.0
                }
                add(nextButton)
            }
        }
    }

    init {
        importStylesheet(resources.get("/css/takecard.css"))
        importStylesheet(resources.get("/css/resourcetakecard.css"))

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
            }
        }
        workspace.subscribe<PluginClosedEvent> {
            (workspace.dockedComponentProperty.value as? PluginOpenedPage)?.let {
                workspace.navigateBack()
            }
            recordableViewModel.openPlayers()
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

            add(grid)
        }

        dragTarget.setOnDragDropped {
            val db: Dragboard = it.dragboard
            var success = false
            if (db.hasString()) {
                recordableViewModel.selectTake(db.string)
                success = true
            }
            (it.source as? ResourceTakeCard)?.let {
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

    private fun createTakeCard(take: TakeCardModel): Control {
        return ResourceTakeCard().apply {
            audioPlayerProperty().set(take.audioPlayer)
            takeProperty().set(take.take)
            takeNumberProperty().set(take.take.number.toString())
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
                            messages["addPlugin"].toUpperCase()
                        ) {
                            audioPluginViewModel.addPlugin(true, false)
                        },
                        Duration.millis(5000.0),
                        null
                    )
                )
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

    private fun Parent.addButtonEventHandlers() {
        addEventHandler(DeleteTakeEvent.DELETE_TAKE) {
            recordableViewModel.deleteTake(it.take)
        }
        addEventHandler(TakeEvent.EDIT_TAKE) {
            recordableViewModel.processTakeWithPlugin(it, PluginType.EDITOR)
        }
    }

    override fun onUndock() {
        removeDialogListeners()
    }

    override fun onDock() {
        initializeImportProgressDialog()
        initializeImportFailDialog()
        initializeImportSuccessDialog()
    }
}
