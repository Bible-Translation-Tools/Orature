/**
 * Copyright (C) 2020-2022 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking

import javafx.scene.layout.Region
import javafx.scene.shape.Rectangle
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ChunkingViewModel
import tornadofx.*

private const val STEP_WIDTH = 80.0
private const val STEP_HEIGHT = 8.0

class ChunkingWizard : Wizard() {

    val vm: ChunkingViewModel by inject()

    val consumeStep = Rectangle(STEP_WIDTH, STEP_HEIGHT).apply {
        addClass("chunking-wizard__step")
        style {
            fillProperty().bind(vm.consumeStepColor)
        }
    }
    val verbalizeStep = Rectangle(STEP_WIDTH, STEP_HEIGHT).apply {
        addClass("chunking-wizard__step")
        style {
            fillProperty().bind(vm.verbalizeStepColor)
        }
    }
    val chunkStep = Rectangle(STEP_WIDTH, STEP_HEIGHT).apply {
        addClass("chunking-wizard__step")
        style {
            fillProperty().bind(vm.chunkStepColor)
        }
    }

    override fun onDock() {
        importStylesheet(resources["/css/chunking-wizard.css"])
        val top = vbox {
            removeClass(WizardStyles.content)
            addClass("chunking-wizard")
            label {
                addClass("chunking-wizard__title")
                textProperty().bind(vm.titleProperty)
            }
            label {
                addClass("chunking-wizard__subtitle")
                textProperty().bind(vm.stepProperty)
            }
            hbox {
                addClass(WizardStyles.buttons)
                addClass("chunking-wizard__step-container")
                spacer()
                button {
                    styleClass.addAll("btn", "btn--secondary")
                    textProperty().bind(backButtonTextProperty)
                    runLater {
                        enableWhen(canGoBack)
                    }
                    action { back() }
                }

                add(consumeStep)
                add(verbalizeStep)
                add(chunkStep)

                button() {
                    styleClass.addAll("btn", "btn--secondary")
                    textProperty().bind(nextButtonTextProperty)
                    runLater {
                        enableWhen(canGoNext.and(hasNext).and(currentPageComplete))
                    }
                    action { next() }
                }
                spacer()
            }
        }
        root.bottom.getChildList()!!.clear()
        root.top.replaceWith(top)
        root.bottom.replaceWith(Region())
        root.left.replaceWith(Region())
        root.center.style {
            padding = box(0.px)
        }
    }

    init {
        add<Verbalize>()
    }
}
