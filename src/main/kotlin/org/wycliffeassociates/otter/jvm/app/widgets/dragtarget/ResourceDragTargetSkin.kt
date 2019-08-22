package org.wycliffeassociates.otter.jvm.app.widgets.dragtarget

import tornadofx.*
import tornadofx.FX.Companion.messages

typealias ResourceStyles = DragTargetStyles.Resource

class ResourceDragTargetSkin(control: DragTarget) : DragTargetSkin(
    control = control,
    dragTargetSize = ResourceStyles.dragTargetSize,
    placeholder = ResourceStyles.selectedTakePlaceHolder,
    borderGlow = ResourceStyles.borderGlow,
    dragTargetOverlay = ResourceStyles.dragTargetOverlay
) {
    init {
        importStylesheet<ResourceStyles>()

        selectedTakePlaceholder.apply {
            text(messages["dragTakeHere"])
        }
    }
}