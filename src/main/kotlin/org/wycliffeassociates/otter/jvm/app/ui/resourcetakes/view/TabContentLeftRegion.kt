package org.wycliffeassociates.otter.jvm.app.ui.resourcetakes.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.beans.property.StringProperty
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.app.theme.AppTheme
import org.wycliffeassociates.otter.jvm.app.widgets.highlightablebutton.highlightablebutton
import tornadofx.*
import tornadofx.FX.Companion.messages

class TabContentLeftRegion(
    formattedTextProperty: StringProperty,
    internal val newTakeAction: () -> Unit
): VBox() {
    private val newTakeButton =
        highlightablebutton {
            highlightColor = Color.ORANGE
            secondaryColor = AppTheme.colors.white
            isHighlighted = true
            graphic = MaterialIconView(MaterialIcon.MIC_NONE, "25px")
            maxWidth = 500.0
            text = messages["newTake"]
            action {
                newTakeAction()
            }
        }
    init {
        vgrow = Priority.ALWAYS
        vbox {
            addClass(RecordResourceStyles.dragTarget)
            text(messages["dragTakeHere"])
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
}