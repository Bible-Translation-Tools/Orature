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
import org.wycliffeassociates.otter.jvm.controls.narration.stickyVerse
import org.wycliffeassociates.otter.jvm.controls.narration.narrationrecordlistview
import org.wycliffeassociates.otter.jvm.controls.narration.narrationTextListview
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import tornadofx.*
import java.text.MessageFormat

class NarrationFragment : Fragment() {
    private val viewModel: DemoViewModel by inject()

    private val chunks = FXCollections.observableArrayList(
        Chunk(
            sort = 1,
            label = "chunk",
            audio = AssociatedAudio(ReplayRelay.create()),
            resources = listOf(),
            textItem = TextItem("Then Jonah prayed to Yahweh his God from the fish's stomach.", MimeType.USFM),
            start = 1,
            end = 1,
            draftNumber = 1,
            contentType = ContentType.TEXT
        ),
        Chunk(
            sort = 2,
            label = "chunk",
            audio = AssociatedAudio(ReplayRelay.create()),
            resources = listOf(),
            textItem = TextItem("He said, I called out to Yahweh about my distress and he answered me;", MimeType.USFM),
            start = 2,
            end = 2,
            draftNumber = 1,
            contentType = ContentType.TEXT
        ),Chunk(
            sort = 3,
            label = "chunk",
            audio = AssociatedAudio(ReplayRelay.create()),
            resources = listOf(),
            textItem = TextItem("You had thrown me into the depths, into the heart of the seas, and the currents " +
                    "surrounded me; all your waves and billows passed over me.", MimeType.USFM),
            start = 3,
            end = 3,
            draftNumber = 1,
            contentType = ContentType.TEXT
        ),
        Chunk(
            sort = 4,
            label = "chunk",
            audio = AssociatedAudio(ReplayRelay.create()),
            resources = listOf(),
            textItem = TextItem("I said, 'I am driven out from before your eyes; yet I will again " +
                    "look toward your holy temple.'", MimeType.USFM),
            start = 4,
            end = 4,
            draftNumber = 1,
            contentType = ContentType.TEXT
        ),
        Chunk(
            sort = 5,
            label = "chunk",
            audio = AssociatedAudio(ReplayRelay.create()),
            resources = listOf(),
            textItem = TextItem("The waters closed around me up to my neck; the deep was all around me; " +
                    "seaweed wrapped around my head.", MimeType.USFM),
            start = 5,
            end = 5,
            draftNumber = 1,
            contentType = ContentType.TEXT
        ),
        Chunk(
            sort = 6,
            label = "chunk",
            audio = AssociatedAudio(ReplayRelay.create()),
            resources = listOf(),
            textItem = TextItem("I went down to the bases of the mountains; the earth with its bars closed " +
                    "upon me forever. Yet you brought up my life from the pit, Yahweh, my God!", MimeType.USFM),
            start = 6,
            end = 6,
            draftNumber = 1,
            contentType = ContentType.TEXT
        ),
        Chunk(
            sort = 7,
            label = "chunk",
            audio = AssociatedAudio(ReplayRelay.create()),
            resources = listOf(),
            textItem = TextItem("When my soul fainted within me, I called Yahweh to mind; then my prayer came to " +
                    "you to your holy temple.", MimeType.USFM),
            start = 7,
            end = 7,
            draftNumber = 1,
            contentType = ContentType.TEXT
        ),
        Chunk(
            sort = 8,
            label = "chunk",
            audio = AssociatedAudio(ReplayRelay.create()),
            resources = listOf(),
            textItem = TextItem("They give attention to meaningless gods while they abandon covenant " +
                    "faithfulness.", MimeType.USFM),
            start = 8,
            end = 8,
            draftNumber = 1,
            contentType = ContentType.TEXT
        ),
        Chunk(
            sort = 9,
            label = "chunk",
            audio = AssociatedAudio(ReplayRelay.create()),
            resources = listOf(),
            textItem = TextItem("But as for me, I will sacrifice to you with a voice of thanksgiving; I will fulfill " +
                    "that which I have vowed. Salvation comes from Yahweh!\"", MimeType.USFM),
            start = 9,
            end = 9,
            draftNumber = 1,
            contentType = ContentType.TEXT
        ),
        Chunk(
            sort = 10,
            label = "chunk",
            audio = AssociatedAudio(ReplayRelay.create()),
            resources = listOf(),
            textItem = TextItem("Then Yahweh spoke to the fish, and it vomited up Jonah upon the dry land.", MimeType.USFM),
            start = 10,
            end = 10,
            draftNumber = 1,
            contentType = ContentType.TEXT
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

                        setCellFactory {
                            NarrationRecordCell(
                                "Open In...",
                                "Record Again",
                                "Loading...",
                                "Go to Verse {0}",
                                viewModel::onChunkOpenIn,
                                viewModel::onRecordChunkAgain
                            )
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

                narrationTextListview(chunkDataList) {
                    addClass("narration__list")

                    initialSelectedItemProperty.set(chunkDataList[0])

                    viewModel.onCurrentVerseActionProperty.bind(onSelectedVerseActionProperty)
                    viewModel.floatingCardVisibleProperty.bind(cardIsOutOfViewProperty)

                    viewModel.currentVerseLabelProperty.bind(selectionModel.selectedItemProperty().stringBinding {
                        it?.title
                    })

                    setCellFactory {
                        NarrationTextCell(
                            "Next Verse",
                            viewModel::onRecord
                        )
                    }
                }

//                floatingnarrationcard {
//                    floatingLabelProperty.bind(viewModel.currentVerseLabelProperty)
//                    floatingCardVisibleProperty.bind(viewModel.floatingCardVisibleProperty)
//                    onFloatingChunkActionProperty.bind(viewModel.onCurrentVerseActionProperty)
//
//                    currentChunkTextProperty.set("Current: {0} {1}")
//                    currentVerseTextProperty.set("Verse")
//                    resumeTextProperty.set("Resume")
//                }
            }
        }
    }

    init {
        tryImportStylesheet(resources["/css/narration.css"])
        tryImportStylesheet(resources["/css/chapter-selector.css"])
    }
}
