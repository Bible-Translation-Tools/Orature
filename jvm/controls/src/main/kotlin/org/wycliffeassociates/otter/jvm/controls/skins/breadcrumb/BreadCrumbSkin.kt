package org.wycliffeassociates.otter.jvm.controls.skins.breadcrumb

import com.sun.javafx.scene.control.behavior.ButtonBehavior
import javafx.beans.binding.Bindings
import javafx.scene.control.SkinBase
import javafx.scene.layout.HBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadCrumb
import tornadofx.*

class BreadCrumbSkin(private val button: BreadCrumb) : SkinBase<BreadCrumb>(button) {
    private val behavior = ButtonBehavior(button)

    init {
        children.addAll(
            HBox().apply {
                addClass("breadcrumb__root")

                label {
                    graphicProperty().bind(button.iconProperty)
                    textProperty().bind(button.titleProperty)
                    tooltip { textProperty().bind(button.titleProperty) }

                    addClass("breadcrumb__content")
                }

                label {
                    addClass("breadcrumb__separator")
                    graphic = FontIcon(MaterialDesign.MDI_MENU_RIGHT).apply {
                        scaleXProperty().bind(button.orientationScaleProperty)
                    }
                    hiddenWhen(button.isActiveProperty)
                    managedWhen(visibleProperty())
                }

                label {
                    addClass("breadcrumb__help")

                    graphic = FontIcon(MaterialDesign.MDI_HELP_CIRCLE)
                    visibleWhen(
                        Bindings.and(button.isActiveProperty, button.tooltipTextProperty.isNotEmpty)
                    )
                    managedWhen(visibleProperty())

                    tooltip {
                        textProperty().bind(button.tooltipTextProperty)
                        prefWidth = 256.0
                    }
                }
            }
        )
    }

    override fun dispose() {
        super.dispose()
        behavior.dispose()
    }
}
