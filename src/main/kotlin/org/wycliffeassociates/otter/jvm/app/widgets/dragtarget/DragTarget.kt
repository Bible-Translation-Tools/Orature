package org.wycliffeassociates.otter.jvm.app.widgets.dragtarget

import javafx.beans.binding.BooleanBinding
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Node
import javafx.scene.control.Control

class DragTarget private constructor(val dragBinding: BooleanBinding) : Control() {
    enum class Type {
        RESOURCE_TAKE,
        SCRIPTURE_TAKE
    }

    constructor(
        type: DragTarget.Type,
        dragBinding: BooleanBinding
    ) : this(dragBinding) {
        this.skin = DragTargetSkinSelector.selectSkin(type, this)
    }

    val selectedNodeProperty = SimpleObjectProperty<Node>()
}
