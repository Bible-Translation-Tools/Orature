package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.beans.binding.Bindings
import javafx.beans.property.BooleanProperty
import javafx.collections.ObservableList
import javafx.scene.control.ListCell
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material.Material
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.jvm.controls.card.LanguageCardCell
import tornadofx.*
import java.util.concurrent.Callable

enum class LanguageType {
    SOURCE,
    TARGET
}

class LanguageCell(
    private val type: LanguageType,
    private val anglicisedProperty: BooleanProperty,
    private val existingLanguages: ObservableList<Language> = observableListOf(),
    private val onSelected: (Language) -> Unit
) : ListCell<Language>() {

    private val view = LanguageCardCell()

    override fun updateItem(item: Language?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            graphic = null
            return
        }

        graphic = view.apply {
            iconProperty.value = when (type) {
                LanguageType.SOURCE -> FontIcon(Material.HEARING)
                LanguageType.TARGET -> FontIcon(MaterialDesign.MDI_VOICE)
            }

            languageNameProperty.bind(anglicisedProperty.stringBinding {
                it?.let {
                    when {
                        it && item.anglicizedName.isNotBlank() -> item.anglicizedName
                        else -> item.name
                    }
                }
            })
            languageSlugProperty.set(item.slug)

            setOnMouseClicked {
                onSelected(item)
            }

            disableProperty().bind(Bindings.createBooleanBinding(
                Callable {
                    existingLanguages.contains(item)
                },
                existingLanguages
            ))
        }
    }
}
