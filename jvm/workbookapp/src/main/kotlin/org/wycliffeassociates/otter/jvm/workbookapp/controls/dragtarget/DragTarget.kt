package org.wycliffeassociates.otter.jvm.workbookapp.controls.dragtarget

import javafx.beans.binding.BooleanBinding
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Node
import javafx.scene.control.Control
import javafx.scene.control.Skin

class DragTarget private constructor(val dragBinding: BooleanBinding) : Control() {
    constructor(
        dragBinding: BooleanBinding,
        skinFactory: (DragTarget) -> Skin<DragTarget>
    ) : this(dragBinding) {
        this.skin = skinFactory(this)
    }

    val selectedNodeProperty = SimpleObjectProperty<Node>()
}
