package org.wycliffeassociates.otter.jvm.workbookapp.ui.resourcetakes.view

import com.github.thomasnield.rxkotlinfx.toObservable
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppTheme
import org.wycliffeassociates.otter.jvm.controls.highlightablebutton.highlightablebutton
import javafx.beans.property.SimpleStringProperty
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.RowConstraints
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.view.RecordableFragment
import org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.viewmodel.RecordableViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.controls.dragtarget.DragTargetBuilder
import org.wycliffeassociates.otter.jvm.workbookapp.controls.takecard.TakeCard
import org.wycliffeassociates.otter.jvm.workbookapp.controls.takecard.resourcetakecard
import tornadofx.*

class RecordResourceFragment(
    recordableViewModel: RecordableViewModel
) : RecordableFragment(
    recordableViewModel,
    DragTargetBuilder(DragTargetBuilder.Type.RESOURCE_TAKE)
) {
    val formattedTextProperty = SimpleStringProperty()

    val alternateTakesList = TakesListView(
        items = recordableViewModel.alternateTakes,
        createTakeNode = ::createTakeCard
    )

    private val newTakeButton =
        highlightablebutton {
            highlightColor = Color.ORANGE
            secondaryColor = AppTheme.colors.white
            isHighlighted = true
            graphic = MaterialIconView(MaterialIcon.MIC_NONE, "25px")
            maxWidth = 500.0
            text = messages["newTake"]
            action {
                closePlayers()
                recordableViewModel.recordNewTake()
            }
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
            add(newTakeButton)
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

    init {
        importStylesheet<RecordResourceStyles>()

        mainContainer.apply {
            add(grid)
        }
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