package org.wycliffeassociates.otter.jvm.app.widgets.dragtarget

object DragTargetSkinSelector {
    fun selectSkin(type: DragTarget.Type, dragTarget: DragTarget): DragTargetSkin {
        return when(type) {
            DragTarget.Type.RESOURCE_TAKE -> ResourceDragTargetSkin(dragTarget)
            DragTarget.Type.SCRIPTURE_TAKE -> ScriptureDragTargetSkin(dragTarget)
        }
    }
}