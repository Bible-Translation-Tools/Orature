package org.wycliffeassociates.otter.jvm.app.ui.resourcetakes.view

import javafx.beans.property.SimpleStringProperty
import javafx.scene.layout.GridPane
import javafx.scene.layout.RowConstraints
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.viewmodel.AudioPluginViewModel
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.viewmodel.RecordableViewModel
import tornadofx.*

class RecordableTabContent(
    private val recordableViewModel: RecordableViewModel
) : Fragment() {

    private val audioPluginViewModel: AudioPluginViewModel by inject()

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
                    TabContentLeftRegion(formattedTextProperty, recordableViewModel::recordNewTake)
                )
            }
            vbox(20.0) {
                gridpaneColumnConstraints {
                    percentWidth = 50.0
                }
                addClass(RecordResourceStyles.rightRegion)
                add(
                    TakesListView(recordableViewModel.alternateTakes, audioPluginViewModel::audioPlayer)
                )
            }
        }
    }
}