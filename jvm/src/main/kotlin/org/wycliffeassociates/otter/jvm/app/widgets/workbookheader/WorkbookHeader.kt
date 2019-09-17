package org.wycliffeassociates.otter.jvm.app.widgets.workbookheader

import com.jfoenix.controls.JFXCheckBox
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.statusindicator.control.statusindicator
import tornadofx.*

class WorkbookHeader : VBox() {

    val labelTextProperty = SimpleStringProperty()
    var labelText by labelTextProperty

    val filterTextProperty = SimpleStringProperty()
    var filterText by filterTextProperty

    val isFilterOnProperty = SimpleBooleanProperty()

    val progressBarTrackFillProperty = SimpleObjectProperty<Color>(Color.ORANGE)
    var progressBarTrackFill by progressBarTrackFillProperty

    val workbookProgressProperty = SimpleDoubleProperty(0.5)
    var workbookProgress by workbookProgressProperty

    init {
        importStylesheet<WorkbookHeaderStyles>()
        addClass(WorkbookHeaderStyles.workbookHeader)
        spacing = 10.0
        hbox {
            label(labelTextProperty) {
                managedProperty().bind(!labelTextProperty.isEmpty)
            }
            region {
                hgrow = Priority.ALWAYS
            }
            add(
                JFXCheckBox().apply {
                    isDisableVisualFocus = true
                    textProperty().bind(filterTextProperty)
                    managedProperty().bind(filterTextProperty.isNotNull)
                    visibleProperty().bind(filterTextProperty.isNotNull)
                    isFilterOnProperty.bind(selectedProperty())
                }
            )
        }
        add(
            statusindicator {
                hgrow = Priority.ALWAYS
                primaryFillProperty.bind(progressBarTrackFillProperty)
                accentFillProperty.bind(progressBarTrackFillProperty)
                trackFill = Color.LIGHTGRAY
                trackHeight = 15.0
                barHeight = 18.0
                indicatorRadius = 10.0
                progressProperty.bind(workbookProgressProperty)
                barBorderStyle = BorderStrokeStyle.SOLID
                barBorderWidth = 1.0
                barBorderRadius = 10.0
                textFill = Color.WHITE
                showText = true
            }
        )
    }
}

fun workbookheader(init: WorkbookHeader.() -> Unit = {}): WorkbookHeader {
    val wh = WorkbookHeader()
    wh.init()
    return wh
}