package org.wycliffeassociates.otter.jvm.workbookapp.ui.resourcetakes.view

import com.github.thomasnield.rxkotlinfx.toObservable
import com.jfoenix.controls.JFXButton
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppTheme
import org.wycliffeassociates.otter.jvm.controls.highlightablebutton.highlightablebutton
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.effect.DropShadow
import javafx.scene.layout.*
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.view.RecordableFragment
import org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.viewmodel.RecordableViewModel
import org.wycliffeassociates.otter.jvm.controls.dragtarget.DragTargetBuilder
import org.wycliffeassociates.otter.jvm.workbookapp.controls.takecard.TakeCard
import org.wycliffeassociates.otter.jvm.workbookapp.controls.takecard.resourcetakecard
import org.wycliffeassociates.otter.jvm.workbookapp.ui.resourcetakes.viewmodel.RecordResourceViewModel
import tornadofx.*

class RecordResourceFragment(
    recordableViewModel: RecordableViewModel
) : RecordableFragment(
    recordableViewModel,
    DragTargetBuilder(DragTargetBuilder.Type.RESOURCE_TAKE)
) {
    val recordResourceViewModel: RecordResourceViewModel by inject()
    val formattedTextProperty = SimpleStringProperty()

    val alternateTakesList = TakesListView(
        items = recordableViewModel.alternateTakes,
        createTakeNode = ::createTakeCard
    )

    val container = mainContainer

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
        addClass(RecordResourceStyles.navbarButton)
        text = messages["previousChunk"]
        graphic = MaterialIconView(MaterialIcon.ARROW_BACK, "26px")
        action {
            closePlayers()
            recordResourceViewModel.previousChunk()
        }
        enableWhen(recordResourceViewModel.hasPrevious)
    }

    private val nextButton = JFXButton().apply {
        addClass(RecordResourceStyles.navbarButton)
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
        setFillHeightSingleRow()

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
    }

    private val navbar = hbox {
        id = "navbar"
        addClass(RecordResourceStyles.navbar)
        anchorpaneConstraints {
            bottomAnchor = 0.0
            leftAnchor = 0.0
            rightAnchor = 0.0
        }
        vbox {
            hgrow = Priority.ALWAYS
            alignment = Pos.CENTER
            add(previousButton)
        }

        vbox {
            hgrow = Priority.ALWAYS
            alignment = Pos.CENTER
            add(nextButton)
        }
    }

    init {
        importStylesheet<RecordResourceStyles>()

        mainContainer.apply {
            navbar.heightProperty().onChange {
                anchorpaneConstraints {
                    bottomAnchor = it
                }
            }
            add(grid)
        }

        add(navbar)
    }

    override fun createTakeCard(take: Take): TakeCard {
        return resourcetakecard(
            take,
            audioPluginViewModel.audioPlayer(),
            lastPlayOrPauseEvent.toObservable()
        )
    }

    private fun GridPane.setFillHeightSingleRow() {
        val rc = RowConstraints()
        rc.percentHeight = 100.0
        rowConstraints.addAll(rc)
    }

    override fun closePlayers() {
        alternateTakesList.getChildList()?.forEach {
            (it as? TakeCard)?.simpleAudioPlayer?.close()
        }
    }
}