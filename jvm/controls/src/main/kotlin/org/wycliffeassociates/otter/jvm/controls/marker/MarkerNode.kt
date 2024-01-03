package org.wycliffeassociates.otter.jvm.controls.marker

import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.jvm.controls.skins.MarkerNodeSkin
import tornadofx.*

class MarkerNode : MarkerControl() {
    override val markerNumberProperty = SimpleStringProperty("1")

    init {
        addClass("marker-node")
        isPickOnBounds = false

        focusTraversableProperty().bind(canBeMovedProperty)
        visibleProperty().bind(isPlacedProperty)
    }

    override fun createDefaultSkin(): Skin<MarkerNode> {
        return MarkerNodeSkin(this)
    }
}
