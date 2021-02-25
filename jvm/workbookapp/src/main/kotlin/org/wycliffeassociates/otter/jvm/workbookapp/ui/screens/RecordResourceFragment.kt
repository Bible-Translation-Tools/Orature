package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import com.github.thomasnield.rxkotlinfx.toObservable
import com.jfoenix.controls.JFXButton
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.effect.DropShadow
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.RowConstraints
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.controls.button.highlightablebutton
import org.wycliffeassociates.otter.jvm.controls.dragtarget.DragTargetBuilder
import org.wycliffeassociates.otter.jvm.workbookapp.controls.takecard.TakeCard
import org.wycliffeassociates.otter.jvm.workbookapp.controls.takecard.resourcetakecard
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppTheme
import org.wycliffeassociates.otter.jvm.workbookapp.ui.styles.RecordResourceStyles
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.TakeCardModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.RecordResourceViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.RecordableViewModel
import tornadofx.*

private class RecordableViewModelProvider : Component() {
    private val resourceViewModel: RecordResourceViewModel by inject()
    fun get() = resourceViewModel.recordableViewModel
}

class RecordResourceFragment(
    recordableViewModel: RecordableViewModel = RecordScriptureViewModelProvider().get()
) : RecordableFragment(
    recordableViewModel,
    DragTargetBuilder(DragTargetBuilder.Type.RESOURCE_TAKE)
) {
    val recordResourceViewModel: RecordResourceViewModel by inject()
    val formattedTextProperty = SimpleStringProperty()

    val alternateTakesList = TakesListView(
        items = recordableViewModel.takeCardModels,
        createTakeNode = ::createTakeCard
    )

    private val newTakeButton =
        highlightablebutton {
            highlightColor = AppTheme.colors.appBlue
            secondaryColor = AppTheme.colors.white
            isHighlighted = true
            graphic = MaterialIconView(MaterialIcon.MIC, "25px")
            maxWidth = 500.0
            text = messages["record"]

            action {
                closePlayers()
                recordableViewModel.recordNewTake()
            }
        }

    private val previousButton = JFXButton().apply {
        addClass(RecordResourceStyles.bottomButton)
        text = messages["previousChunk"]
        graphic = MaterialIconView(MaterialIcon.ARROW_BACK, "26px")
        action {
            closePlayers()
            recordResourceViewModel.previousChunk()
        }
        enableWhen(recordResourceViewModel.hasPrevious)
    }

    private val nextButton = JFXButton().apply {
        addClass(RecordResourceStyles.bottomButton)
        text = messages["nextChunk"]
        graphic = MaterialIconView(MaterialIcon.ARROW_FORWARD, "26px")
        action {
            closePlayers()
            recordResourceViewModel.nextChunk()
        }
        enableWhen(recordResourceViewModel.hasNext)
    }

    private val leftRegion = VBox().apply {
        vgrow = Priority.ALWAYS

        hbox {
            region {
                hgrow = Priority.ALWAYS
            }
            add(
                dragTarget.apply {
                    hgrow = Priority.ALWAYS
                }
            )
            region {
                hgrow = Priority.ALWAYS
            }
        }

        scrollpane {
            addClass(RecordResourceStyles.contentScrollPane)
            isFitToWidth = true
            vgrow = Priority.ALWAYS
            label(formattedTextProperty) {
                isWrapText = true
                addClass(RecordResourceStyles.contentText)
            }
        }

        vbox {
            addClass(RecordResourceStyles.newTakeRegion)
            add(newTakeButton).apply {
                effect = DropShadow(2.0, 0.0, 3.0, Color.valueOf("#0d4082"))
            }
        }
    }

    private val grid = gridpane {
        vgrow = Priority.ALWAYS
        addClass(RecordResourceStyles.takesTab)

        constraintsForRow(0).percentHeight = 90.0
        constraintsForRow(1).percentHeight = 10.0

        row {
            vbox {
                gridpaneColumnConstraints {
                    percentWidth = 50.0
                }
                addClass(RecordResourceStyles.leftRegionContainer)
                add(leftRegion)
            }
            vbox(20.0) {
                gridpaneColumnConstraints {
                    percentWidth = 50.0
                }
                addClass(RecordResourceStyles.rightRegion)
                add(alternateTakesList)
            }
        }

        row {
            vbox {
                alignment = Pos.CENTER
                gridpaneColumnConstraints {
                    percentWidth = 50.0
                }
                add(previousButton)
            }

            vbox {
                alignment = Pos.CENTER
                gridpaneColumnConstraints {
                    percentWidth = 50.0
                }
                add(nextButton)
            }
        }
    }

    init {
        importStylesheet<RecordResourceStyles>()

        mainContainer.apply {
            add(grid)
        }
    }

    override fun createTakeCard(take: TakeCardModel): TakeCard {
        return resourcetakecard(
            take,
            lastPlayOrPauseEvent.toObservable()
        )
    }

    private fun GridPane.setFillHeightSingleRow() {
        val rc = RowConstraints()
        rc.percentHeight = 100.0
        rowConstraints.addAll(rc)
    }

    override fun closePlayers() {
        recordableViewModel.takeCardModels.forEach { it.audioPlayer.close() }
    }

    override fun openPlayers() {
        recordableViewModel.takeCardModels.forEach { it.audioPlayer.load(it.take.file) }
    }
}
