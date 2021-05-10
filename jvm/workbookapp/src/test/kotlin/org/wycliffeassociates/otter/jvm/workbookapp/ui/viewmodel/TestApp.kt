package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import javafx.scene.Parent
import javafx.scene.layout.Region
import org.wycliffeassociates.otter.jvm.workbookapp.di.AppDependencyGraph
import org.wycliffeassociates.otter.jvm.workbookapp.di.DaggerAppDependencyGraph
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import tornadofx.*

private class TestView(override val root: Parent = Region()) : Fragment()

internal class TestApp : App(TestView::class), IDependencyGraphProvider {
    override val dependencyGraph: AppDependencyGraph = DaggerAppDependencyGraph.builder().build()
}
