package org.wycliffeassociates.otter.jvm.app.ui.resourcetakes.view

import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.scene.layout.GridPane
import javafx.scene.layout.RowConstraints
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.jvm.app.ui.resourcetakes.viewmodel.RecordResourceViewModel
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.viewmodel.AudioPluginViewModel
import tornadofx.*

class RecordableTabContent(
    takesList: ObservableList<Take>
) : Fragment() {

    private val audioPluginViewModel: AudioPluginViewModel by inject()
    private val recordResourceViewModel: RecordResourceViewModel by inject()

    val formattedTextProperty = SimpleStringProperty()

    init {
        importStylesheet<RecordResourceStyles>()
    }

    private fun GridPane.setFillHeightSingleRow() {
        val rc = RowConstraints()
        rc.percentHeight = 100.0
        rowConstraints.addAll(rc)
    }

    override val root = gridpane {
        addClass(RecordResourceStyles.takesTab)
        setFillHeightSingleRow()

        row {
            vbox {
                gridpaneColumnConstraints {
                    percentWidth = 50.0
                }
                addClass(RecordResourceStyles.leftRegionContainer)
                add(
                    TabContentLeftRegion(formattedTextProperty, recordResourceViewModel::recordNewTake)
                )
            }
            vbox(20.0) {
                gridpaneColumnConstraints {
                    percentWidth = 50.0
                }
                addClass(RecordResourceStyles.rightRegion)
                add(
                    TakesListView(takesList, audioPluginViewModel::audioPlayer)
                )
            }
        }
    }
}