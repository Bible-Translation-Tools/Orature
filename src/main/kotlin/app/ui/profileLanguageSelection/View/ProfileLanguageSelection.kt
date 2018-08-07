package app.ui.profileLanguageSelection.View

import app.ui.languageSelectorFragment.LanguageSelector
import app.ui.languageSelectorFragment.LanguageSelectorStyle
import app.ui.profileLanguageSelection.ViewModel.ProfileLanguageSelectionViewModel
import app.ui.welcomeScreen.WelcomeScreen
import app.UIColorsObject.Colors
import app.widgets.WidgetsStyles
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

    private val rightArrow = MaterialIconView(MaterialIcon.ARROW_FORWARD, "25px")
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
                    viewModel.updateSelectedSourceLanguages(it)
                },
                viewModel.updateSelectedSources.subscribe {
                    viewModel.updatePreferredTargetLanguages(it)
                },
                viewModel.updatePreferredSource.subscribe {
                    viewModel.updatePreferredSourceLanguages(it)
                }
        )

        with(root) {
            importStylesheet(LanguageSelectorStyle::class)

            bottom = hbox {
                alignment = Pos.TOP_RIGHT
                button(messages["next"], rightArrow) {
                    addClass(WidgetsStyles.rectangleButtonDefault)
                    /*style {
                        backgroundColor = multi(c(Colors["primaryShade"]), c(Colors["primary"]))
                        textFill = c(Colors["base"])
                        alignment = Pos.CENTER
                        rightArrow.fill = c(Colors["base"])
                        minWidth = 200.px
                    }*/
                }
                style {
                    alignment = javafx.geometry.Pos.TOP_RIGHT
                    paddingRight = 40.0
                    paddingBottom = 40.0
                    paddingTop = 40.0
                }
            }
            top = hbox {
                alignment = Pos.BOTTOM_RIGHT
                button(messages["close"], closeIcon) {
                    addClass(WidgetsStyles.rectangleButtonDefault)
                    style {
                        alignment = Pos.CENTER
                        closeIcon.fill = c(Colors["primary"])
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
