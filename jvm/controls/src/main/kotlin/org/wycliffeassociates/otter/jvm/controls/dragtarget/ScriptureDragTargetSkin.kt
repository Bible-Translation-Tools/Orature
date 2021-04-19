package org.wycliffeassociates.otter.jvm.controls.dragtarget

import tornadofx.*

typealias ScriptureStyles = DragTargetStyles.Scripture

class ScriptureDragTargetSkin(control: DragTarget) : DragTargetSkin(
    control = control,
    dragTargetSize = ScriptureStyles.dragTargetSize,
    placeholder = ScriptureStyles.selectedTakePlaceHolder,
    borderGlow = ScriptureStyles.borderGlow,
    dragTargetOverlay = ScriptureStyles.dragTargetOverlay
) {
    init {
        importStylesheet<ScriptureStyles>()
    }
}
