package org.wycliffeassociates.otter.jvm.markerapp.app.view

import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.Priority
import javafx.scene.layout.RowConstraints
import javafx.stage.Screen
import org.wycliffeassociates.otter.jvm.markerapp.app.view.layers.PlaceMarkerLayer
import org.wycliffeassociates.otter.jvm.markerapp.app.view.layers.WaveformOverlay
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.PluginEntrypoint
import tornadofx.*

const val WINDOW_OFFSET = 50.0

class MarkerView : PluginEntrypoint() {

    val viewModel: VerseMarkerViewModel by inject()

    val titleFragment = TitleFragment()
    val minimap = MinimapFragment()
    val waveformContainer = WaveformContainer()
    val playbackControls = PlaybackControlsFragment()

    init {
        runLater {
            val css = this@MarkerView.javaClass.getResource("/css/verse-marker-app.css")
                .toExternalForm()
                .replace(" ", "%20")
            importStylesheet(css)

            FX.stylesheets.addAll(
                javaClass.getResource("/css/button.css").toExternalForm(),
                css
            )
        }
        viewModel.initializeAudioController(minimap.slider)
    }

    override val root = gridpane {
        prefHeight = Screen.getPrimary().visualBounds.height - WINDOW_OFFSET
        prefWidth = Screen.getPrimary().visualBounds.width - WINDOW_OFFSET

        val emptyConstraint = RowConstraints()
        val growConstraint = RowConstraints()
        val columnConstraint = ColumnConstraints()

        columnConstraint.hgrow = Priority.ALWAYS
        growConstraint.vgrow = Priority.ALWAYS

        rowConstraints.setAll(
            emptyConstraint,
            emptyConstraint,
            growConstraint,
            emptyConstraint
        )
        columnConstraints.setAll(
            columnConstraint
        )

        add(titleFragment.root, 0, 0)
        add(minimap.root, 0, 1)
        add(waveformContainer.root, 0, 2)
        add(WaveformOverlay(viewModel), 0, 2)
        add(PlaceMarkerLayer(viewModel), 0, 2)
        add(playbackControls.root, 0, 3)
    }
}
