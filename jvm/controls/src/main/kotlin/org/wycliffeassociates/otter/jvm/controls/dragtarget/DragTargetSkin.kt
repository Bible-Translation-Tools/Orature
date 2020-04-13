package org.wycliffeassociates.otter.jvm.controls.dragtarget

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.SkinBase
import javafx.scene.layout.StackPane
import org.wycliffeassociates.otter.jvm.utils.bindSingleChild
import tornadofx.*

abstract class DragTargetSkin(
    control: DragTarget,
    dragTargetSize: CssRule,
    placeholder: CssRule,
    borderGlow: CssRule,
    dragTargetOverlay: CssRule
) : SkinBase<DragTarget>(control) {
    protected var selectedTakePlaceholder: Node by singleAssign()

    init {
        val root = StackPane().apply {
            addClass(dragTargetSize)
            selectedTakePlaceholder = vbox {
                addClass(placeholder)
                toggleClass(borderGlow, skinnable.dragBinding)
            }
            vbox {
                bindSingleChild(skinnable.selectedNodeProperty)
            }
            vbox {
                addClass(dragTargetOverlay)
                alignment = Pos.CENTER
                add(MaterialIconView(MaterialIcon.ADD, "30px"))
                visibleProperty().bind(skinnable.dragBinding)
            }
        }
        children.addAll(root)
    }
}