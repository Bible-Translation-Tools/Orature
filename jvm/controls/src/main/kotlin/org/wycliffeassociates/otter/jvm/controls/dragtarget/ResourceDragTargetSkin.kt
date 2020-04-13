package org.wycliffeassociates.otter.jvm.controls.dragtarget

import tornadofx.*
import tornadofx.FX.Companion.messages

typealias ResourceStyles = DragTargetStyles.Resource

class ResourceDragTargetSkin(control: DragTarget) : DragTargetSkin(
    control = control,
    dragTargetSize = ResourceStyles.resourceDragTargetSize,
    placeholder = ResourceStyles.selectedResourceTakePlaceHolder,
    borderGlow = ResourceStyles.borderGlow,
    dragTargetOverlay = ResourceStyles.resourceDragTargetOverlay
) {
    init {
        importStylesheet<ResourceStyles>()

        selectedTakePlaceholder.apply {
            text(messages["dragTakeHere"])
        }
    }
}