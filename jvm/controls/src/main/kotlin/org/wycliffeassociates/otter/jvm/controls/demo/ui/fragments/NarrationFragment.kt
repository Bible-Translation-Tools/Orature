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
package org.wycliffeassociates.otter.jvm.controls.demo.ui.fragments

import com.jakewharton.rxrelay2.ReplayRelay
import javafx.collections.FXCollections
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.data.primitives.MimeType
import org.wycliffeassociates.otter.common.data.workbook.AssociatedAudio
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.TextItem
import org.wycliffeassociates.otter.jvm.controls.chapterselector.ChapterSelector
import org.wycliffeassociates.otter.jvm.controls.demo.ui.components.NarrationRecordCell
import org.wycliffeassociates.otter.jvm.controls.demo.ui.components.NarrationTextCell
import org.wycliffeassociates.otter.jvm.controls.demo.ui.models.ChunkData
import org.wycliffeassociates.otter.jvm.controls.demo.ui.viewmodels.DemoViewModel
import org.wycliffeassociates.otter.jvm.controls.narration.floatingnarrationcard
import org.wycliffeassociates.otter.jvm.controls.narration.narrationrecordlistview
import org.wycliffeassociates.otter.jvm.controls.narration.narrationtextlistview
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import tornadofx.*
import java.text.MessageFormat

class NarrationFragment : Fragment() {
    private val viewModel: DemoViewModel by inject()

    private val chunks = FXCollections.observableArrayList(
        Chunk(
            1,
            "chunk",
            AssociatedAudio(ReplayRelay.create()),
            listOf(),
            TextItem("Then Jonah prayed to Yahweh his God from the fish's stomach.", MimeType.USFM),
            1,
            1,
            1,
            ContentType.TEXT
        ),
        Chunk(
            2,
            "chunk",
            AssociatedAudio(ReplayRelay.create()),
            listOf(),
            TextItem("He said, I called out to Yahweh about my distress and he answered me;", MimeType.USFM),
            2,
            2,
            1,
            ContentType.TEXT
        ),Chunk(
            3,
            "chunk",
            AssociatedAudio(ReplayRelay.create()),
            listOf(),
            TextItem("You had thrown me into the depths, into the heart of the seas, and the currents " +
                    "surrounded me; all your waves and billows passed over me.", MimeType.USFM),
            3,
            3,
            1,
            ContentType.TEXT
        ),
        Chunk(
            4,
            "chunk",
            AssociatedAudio(ReplayRelay.create()),
            listOf(),
            TextItem("I said, 'I am driven out from before your eyes; yet I will again " +
                    "look toward your holy temple.'", MimeType.USFM),
            4,
            4,
            1,
            ContentType.TEXT
        ),
        Chunk(
            5,
            "chunk",
            AssociatedAudio(ReplayRelay.create()),
            listOf(),
            TextItem("The waters closed around me up to my neck; the deep was all around me; " +
                    "seaweed wrapped around my head.", MimeType.USFM),
            5,
            5,
            1,
            ContentType.TEXT
        ),
        Chunk(
            6,
            "chunk",
            AssociatedAudio(ReplayRelay.create()),
            listOf(),
            TextItem("I went down to the bases of the mountains; the earth with its bars closed " +
                    "upon me forever. Yet you brought up my life from the pit, Yahweh, my God!", MimeType.USFM),
            6,
            6,
            1,
            ContentType.TEXT
        ),
        Chunk(
            7,
            "chunk",
            AssociatedAudio(ReplayRelay.create()),
            listOf(),
            TextItem("When my soul fainted within me, I called Yahweh to mind; then my prayer came to " +
                    "you to your holy temple.", MimeType.USFM),
            7,
            7,
            1,
            ContentType.TEXT
        ),
        Chunk(
            8,
            "chunk",
            AssociatedAudio(ReplayRelay.create()),
            listOf(),
            TextItem("They give attention to meaningless gods while they abandon covenant " +
                    "faithfulness.", MimeType.USFM),
            8,
            8,
            1,
            ContentType.TEXT
        ),
        Chunk(
            9,
            "chunk",
            AssociatedAudio(ReplayRelay.create()),
            listOf(),
            TextItem("But as for me, I will sacrifice to you with a voice of thanksgiving; I will fulfill " +
                    "that which I have vowed. Salvation comes from Yahweh!\"", MimeType.USFM),
            9,
            9,
            1,
            ContentType.TEXT
        ),
        Chunk(
            10,
            "chunk",
            AssociatedAudio(ReplayRelay.create()),
            listOf(),
            TextItem("Then Yahweh spoke to the fish, and it vomited up Jonah upon the dry land.", MimeType.USFM),
            10,
            10,
            1,
            ContentType.TEXT
        )
    )

    private val chunkDataList = chunks.map { ChunkData(it) }.toObservable()

    override val root = stackpane {
        vbox {
            hbox {
                addClass("narration__header")

                label("Jonah") {
                    addClass("narration__header-title")
                }
                region {
                    hgrow = Priority.ALWAYS
                }
                hbox {
                    addClass("narration__header-controls")

                    button {
                        addClass("btn", "btn--primary", "btn--borderless")
                        graphic = FontIcon(MaterialDesign.MDI_UNDO)

                        action {
                            println("Chapter has been reset")
                        }
                    }
                    add(
                        ChapterSelector().apply {
                            chapterTitleProperty.bind(viewModel.currentChapterProperty.stringBinding {
                                it?.let {
                                    MessageFormat.format("Chapter {0}", it)
                                } ?: ""
                            })

                            prevDisabledProperty.set(true)

                            setOnPreviousChapter {
                                println("Previous chapter selected")
                            }
                            setOnNextChapter {
                                println("Next chapter selected")
                            }
                        }
                    )
                }
            }
            stackpane {
                addClass("narration__recording")
                alignment = Pos.CENTER

                hbox {
                    narrationrecordlistview(chunkDataList) {
                        hgrow = Priority.ALWAYS

                        openInTextProperty.set("Open In...")
                        recordAgainTextProperty.set("Record Again")

                        setCellFactory {
                            NarrationRecordCell().apply {
                                openInTextCellProperty.bind(openInTextProperty)
                                recordAgainTextCellProperty.bind(recordAgainTextProperty)

                                onOpenAppActionCellProperty.bind(onOpenAppActionProperty)
                                onRecordAgainActionCellProperty.bind(onRecordAgainActionProperty)
                            }
                        }

                        setOnOpenApp {
                            println("Opening verse ${it.title} in external app...")
                        }

                        setOnRecordAgain {
                            println("Recording verse ${it.title} again")
                        }
                    }

                    stackpane {
                        addClass("narration__volume-bar")

                        vbox {
                            addClass("narration__volume-bar__value")

                            maxHeight = 50.0
                        }
                    }
                }

                vbox {
                    addClass("narration__recording-tip")
                    alignment = Pos.CENTER_LEFT

                    label("Tip") {
                        addClass("narration__recording-tip-title")
                        style = "-fx-font-weight: bold;"
                    }
                    label("Press the down key on your keyboard to navigate to the next verse.")

                    isVisible = false
                }
            }
            stackpane {
                addClass("narration__verses")

                narrationtextlistview(chunkDataList) {
                    addClass("narration__list")

                    initialSelectedItemProperty.set(chunkDataList[0])

                    viewModel.onCurrentVerseActionProperty.bind(onSelectedVerseActionProperty)
                    viewModel.floatingCardVisibleProperty.bind(cardIsOutOfViewProperty)

                    viewModel.currentVerseLabelProperty.bind(selectionModel.selectedItemProperty().stringBinding {
                        it?.title
                    })

                    // Maybe instead of having 3 properties for recording status
                    // it's better to have only one property and change text according to the state
                    // in a view model???
                    beginRecordingTextProperty.set("Begin Recording")
                    pauseRecordingTextProperty.set("Pause Recording")
                    resumeRecordingTextProperty.set("Resume Recording")
                    nextChunkTextProperty.set("Next Verse")

                    setCellFactory {
                        NarrationTextCell().apply {
                            beginRecordingTextCellProperty.bind(beginRecordingTextProperty)
                            pauseRecordingTextCellProperty.bind(pauseRecordingTextProperty)
                            resumeRecordingTextCellProperty.bind(resumeRecordingTextProperty)
                            nextChunkTextCellProperty.bind(nextChunkTextProperty)

                            onRecordActionCellProperty.bind(onRecordActionProperty)
                        }
                    }

                    setOnRecord {
                        println("Recording verse ${it.title}")
                    }
                }

                floatingnarrationcard {
                    floatingLabelProperty.bind(viewModel.currentVerseLabelProperty)
                    floatingCardVisibleProperty.bind(viewModel.floatingCardVisibleProperty)
                    onFloatingChunkActionProperty.bind(viewModel.onCurrentVerseActionProperty)

                    currentChunkTextProperty.set("Current: Verse {0}")
                    resumeTextProperty.set("Resume")
                }
            }
        }
    }

    init {
        tryImportStylesheet(resources["/css/narration.css"])
        tryImportStylesheet(resources["/css/chapter-selector.css"])
    }
}
