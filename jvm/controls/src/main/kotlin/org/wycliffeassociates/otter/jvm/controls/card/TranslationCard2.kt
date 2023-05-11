package org.wycliffeassociates.otter.jvm.controls.card

import com.sun.javafx.scene.control.behavior.ButtonBehavior
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.control.ButtonBase
import javafx.scene.control.Skin
import javafx.scene.control.SkinBase
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material.Material
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.jvm.controls.model.TranslationMode
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNowWithDisposer
import tornadofx.FX
import tornadofx.addClass
import tornadofx.addPseudoClass
import tornadofx.attachTo
import tornadofx.get
import tornadofx.hbox
import tornadofx.hgrow
import tornadofx.label
import tornadofx.region
import tornadofx.stringBinding
import tornadofx.vbox
import java.text.MessageFormat
// TODO: remove number "2" suffix after deleting the original control. Same for css named translation-card-2.css
class TranslationCard2(
    sourceLanguage: Language,
    targetLanguage: Language,
    mode: TranslationMode
) : ButtonBase() {

    val cardTitleProperty = SimpleStringProperty(
        MessageFormat.format(FX.messages["translationMode"], FX.messages[mode.titleKey])
    )
    val sourceLanguageProperty = SimpleObjectProperty(sourceLanguage)
    val targetLanguageProperty = SimpleObjectProperty(targetLanguage)
    val activeProperty = SimpleBooleanProperty()

    init {
        activeProperty.onChangeAndDoNow {
            skin = if (it == true) {
                ActiveTranslationCardSkin(this)
            } else {
                TranslationCardSkin2(this)
            }
        }
    }

    override fun createDefaultSkin(): Skin<*> {
        return TranslationCardSkin2(this)
    }

    override fun fire() {
        activeProperty.set(true)
    }
}

class TranslationCardSkin2(card: TranslationCard2) : SkinBase<TranslationCard2>(card) {
    private val behavior = ButtonBehavior(card)

    private val titleProperty = SimpleStringProperty()
    private val sourceLanguageProperty = SimpleStringProperty()
    private val targetLanguageProperty = SimpleStringProperty()

    private val graphic = VBox().apply {
        addClass("translation-card", "translation-card--selectable")

        hbox {
            addClass("translation-card__header")
            label(titleProperty) {
                addClass("h5", "translation-card__header__text")
            }
        }
        hbox {
            addClass("translation-card__body")

            label(sourceLanguageProperty) {
                addClass("translation-card__language")
            }
            hbox {
                hgrow = Priority.ALWAYS
                alignment = Pos.CENTER
                label {
                    addClass("translation-card__divider")
                    graphic = FontIcon(MaterialDesign.MDI_MENU_RIGHT)
                }
            }
            label(targetLanguageProperty) {
                addClass("translation-card__language")
            }
        }
    }
    init {
        titleProperty.bind(card.cardTitleProperty)
        sourceLanguageProperty.bind(card.sourceLanguageProperty.stringBinding { it?.slug })
        targetLanguageProperty.bind(card.targetLanguageProperty.stringBinding { it?.slug })

        if (skinnable != null) {
            children.clear()
        }
        children.add(graphic)
    }

    override fun dispose() {
        super.dispose()
        behavior.dispose()
    }
}

class ActiveTranslationCardSkin(card: TranslationCard2) : SkinBase<TranslationCard2>(card) {
    private val behavior = ButtonBehavior(card)

    private val titleProperty = SimpleStringProperty()
    private val sourceLanguageProperty = SimpleStringProperty()
    private val targetLanguageProperty = SimpleStringProperty()

    private val graphic = VBox().apply {
        addClass("translation-card")
        addPseudoClass("active")

        hbox {
            addClass("translation-card__header")
            label(titleProperty) {
                addClass("h5", "translation-card__header__text")
            }
            region { hgrow = Priority.ALWAYS }
            label {
                graphic = FontIcon(MaterialDesign.MDI_INFORMATION_OUTLINE)
            }
        }
        vbox {
            addClass("translation-card__body")
            label(sourceLanguageProperty) {
                addClass("translation-card__language")
                graphic = FontIcon(Material.HEARING)
            }
            label {
                addClass("translation-card__divider")
                graphic = FontIcon(MaterialDesign.MDI_MENU_DOWN)
            }
            label(targetLanguageProperty) {
                addClass("translation-card__language")
                graphic = FontIcon(MaterialDesign.MDI_VOICE)
            }
        }
    }
    init {
        titleProperty.bind(card.cardTitleProperty)
        sourceLanguageProperty.bind(card.sourceLanguageProperty.stringBinding { it?.name })
        targetLanguageProperty.bind(card.targetLanguageProperty.stringBinding { it?.name })

        if (skinnable != null) {
            children.clear()
        }
        children.add(graphic)
    }

    override fun dispose() {
        super.dispose()
        behavior.dispose()
    }
}

fun EventTarget.translationCard(
    sourceLanguage: Language,
    targetLanguage: Language,
    mode: TranslationMode,
    op: TranslationCard2.() -> Unit = {}
) = TranslationCard2(sourceLanguage, targetLanguage, mode).attachTo(this, op)
