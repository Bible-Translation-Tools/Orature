package org.wycliffeassociates.otter.jvm.app.ui.resourcetakes.view

import com.github.thomasnield.rxkotlinfx.toObservable
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.app.theme.AppTheme
import org.wycliffeassociates.otter.jvm.app.widgets.highlightablebutton.highlightablebutton
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.RowConstraints
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.view.DragTakeFragment
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.viewmodel.AudioPluginViewModel
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.viewmodel.RecordableViewModel
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.TakeCard
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.events.DeleteTakeEvent
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.events.EditTakeEvent
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.events.PlayOrPauseEvent
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.resourcetakecard
import tornadofx.*

class RecordableTabContentFragment(
    override val recordableViewModel: RecordableViewModel
) : DragTakeFragment() {

    private val audioPluginViewModel: AudioPluginViewModel by inject()

    val formattedTextProperty = SimpleStringProperty()

    private val newTakeButton =
        highlightablebutton {
            highlightColor = Color.ORANGE
            secondaryColor = AppTheme.colors.white
            isHighlighted = true
            graphic = MaterialIconView(MaterialIcon.MIC_NONE, "25px")
            maxWidth = 500.0
            text = messages["newTake"]
            action {
                recordableViewModel.recordNewTake()
            }
        }

    private val leftRegion = VBox().apply {
        vgrow = Priority.ALWAYS

        //selected take and drag target
        stackpane {
            // drag target glow
            add(dragComponents
                .dragTargetBottom {
                    addClass(RecordResourceStyles.dragTarget, RecordResourceStyles.glow)
                })

            add(dragComponents
                .selectedTakeContainer {
                    addClass(RecordResourceStyles.selectedTakePlaceholder)
                    vgrow = Priority.NEVER
                    text(messages["dragTakeHere"])
                })

            add(dragComponents
                .dragTargetTop {
                    addClass(RecordResourceStyles.dragTarget)
                    alignment = Pos.CENTER
                    add(MaterialIconView(MaterialIcon.ADD, "30px"))
                })
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
                add(
                    TakesListView(
                        recordableViewModel.alternateTakes,
                        audioPluginViewModel::audioPlayer,
                        recordableViewModel.lastPlayOrPauseEvent)
                )
            }
        }
    }

    init {
        importStylesheet<RecordResourceStyles>()

        root.apply {
            addEventHandler(PlayOrPauseEvent.PLAY) {
                recordableViewModel.lastPlayOrPauseEvent.set(it)
            }
            addEventHandler(DeleteTakeEvent.DELETE_TAKE) {
                recordableViewModel.deleteTake(it.take)
            }
            addEventHandler(EditTakeEvent.EDIT_TAKE) {
                recordableViewModel.editTake(it)
            }
        }

        mainContainer.apply {
            add(grid)
        }
    }

    override fun createTakeCard(take: Take): TakeCard {
        return resourcetakecard(
            take,
            audioPluginViewModel.audioPlayer(),
            recordableViewModel.lastPlayOrPauseEvent.toObservable()
        )
    }

    private fun GridPane.setFillHeightSingleRow() {
        val rc = RowConstraints()
        rc.percentHeight = 100.0
        rowConstraints.addAll(rc)
    }
}