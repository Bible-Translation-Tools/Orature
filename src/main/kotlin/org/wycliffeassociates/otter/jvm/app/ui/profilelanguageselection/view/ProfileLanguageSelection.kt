package org.wycliffeassociates.otter.jvm.app.ui.profilelanguageselection.view

import org.wycliffeassociates.otter.jvm.app.ui.languageselectorfragment.LanguageSelector
import org.wycliffeassociates.otter.jvm.app.ui.languageselectorfragment.LanguageSelectorStyle
import org.wycliffeassociates.otter.jvm.app.ui.profilelanguageselection.viewmodel.ProfileLanguageSelectionViewModel
import org.wycliffeassociates.otter.jvm.app.ui.welcomescreen.WelcomeScreen
import org.wycliffeassociates.otter.jvm.app.UIColorsObject.Colors
import org.wycliffeassociates.otter.jvm.app.widgets.WidgetsStyles
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import io.reactivex.disposables.CompositeDisposable
import javafx.geometry.Pos
import javafx.scene.effect.DropShadow
import javafx.scene.layout.BorderPane
import tornadofx.*

class ProfileLanguageSelection : View() {

    private val compositeDisposable = CompositeDisposable()
    private val viewModel = ProfileLanguageSelectionViewModel()

    private val rightArrow = MaterialIconView(MaterialIcon.ARROW_FORWARD, "25px").apply {
        this.id = "nextArrow"
    }
    private val closeIcon = MaterialIconView(MaterialIcon.CLOSE, "25px")

    private val hint = messages["languageSelectorHint"]

    private val circleRadius = 120.0

    override val root = BorderPane()

    init {

        compositeDisposable.addAll(
                viewModel.updateSelectedTargets.subscribe {
                    viewModel.updateSelectedTargetLanguages(it)
                },
                viewModel.updatePreferredTarget.subscribe {
                    viewModel.updatePreferredTargetLanguage(it)
                },
                viewModel.updateSelectedSources.subscribe {
                    viewModel.updateSelectedSourceLanguages(it)
                },
                viewModel.updatePreferredSource.subscribe {
                    viewModel.updatePreferredSourceLanguage(it)
                }
        )

        with(root) {
            importStylesheet(LanguageSelectorStyle::class)

            // close button
            top = hbox {
                alignment = Pos.BOTTOM_RIGHT
                button(messages["close"], closeIcon) {
                    importStylesheet(WidgetsStyles::class)
                    addClass(WidgetsStyles.rectangleButtonDefault)
                    style {
                        alignment = Pos.CENTER
                        closeIcon.fill =c(Colors["primary"])
                        effect = DropShadow(10.0, c(Colors["baseMedium"]))
                    }
                    action {
                        find(ProfileLanguageSelection::class).replaceWith(WelcomeScreen::class)
                    }
                }

                style {
                    alignment = javafx.geometry.Pos.BOTTOM_RIGHT
                    paddingRight = 40.0
                    paddingTop = 40.0
                }
            }

            // next button
            bottom = hbox {
                alignment = Pos.TOP_RIGHT
                button(messages["next"], rightArrow) {
                    disableProperty().bind(!viewModel.isNextAvailable)
                    importStylesheet(WidgetsStyles::class)
                    addClass(WidgetsStyles.nextButtonNotReady)

                }.apply {
                    disabledProperty().onChange {
                        if (it) {
                            removeClass(WidgetsStyles.nextButtonReady)
                            addClass(WidgetsStyles.nextButtonNotReady)
                        } else {
                            removeClass(WidgetsStyles.nextButtonNotReady)
                            addClass(WidgetsStyles.nextButtonReady)
                        }
                    }
                }

                style {
                    alignment = javafx.geometry.Pos.TOP_RIGHT
                    paddingRight = 40.0
                    paddingBottom = 40.0
                    paddingTop = 40.0
                }
            }

            // target languages
            left = LanguageSelector(
                    viewModel.getTargetLanguageOptions(),
                    messages["targetLanguages"],
                    MaterialIconView(MaterialIcon.RECORD_VOICE_OVER, "20px"),
                    hint,
                    c(Colors["primary"]),
                    viewModel.updateSelectedTargets,
                    viewModel.updatePreferredTarget
            ).apply {
                root.paddingTopProperty.bind((root.heightProperty() - circleRadius) / 2)
            }.root

            center = stackpane {
                circle {
                    style {
                        radius = circleRadius
                        fill = c(Colors["baseLight"])
                    }
                }
            }

            // source languages
            right = LanguageSelector(
                    viewModel.getSourceLanguageOptions(),
                    messages["sourceLanguages"],
                    MaterialIconView(MaterialIcon.HEARING, "20px"),
                    hint,
                    c(Colors["secondary"]),
                    viewModel.updateSelectedSources,
                    viewModel.updatePreferredSource
            ).apply {
                root.paddingTopProperty.bind((root.heightProperty() - circleRadius) / 2)
            }.root

        }
    }
}
