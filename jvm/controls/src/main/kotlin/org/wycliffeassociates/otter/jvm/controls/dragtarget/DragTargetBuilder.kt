package org.wycliffeassociates.otter.jvm.controls.dragtarget

import javafx.beans.binding.BooleanBinding

class DragTargetBuilder(private val type: Type) {
    enum class Type {
        RESOURCE_TAKE,
        SCRIPTURE_TAKE
    }

    fun build(dragBinding: BooleanBinding): DragTarget {
        val skinFactory = when (type) {
            Type.RESOURCE_TAKE -> ::ResourceDragTargetSkin
            Type.SCRIPTURE_TAKE -> ::ScriptureDragTargetSkin
        }
        return DragTarget(dragBinding, skinFactory)
    }
}