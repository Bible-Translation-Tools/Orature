package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import com.jfoenix.controls.JFXTabPane
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadCrumb
import org.wycliffeassociates.otter.jvm.utils.getNotNull
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginClosedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.RecordResourceViewModel
import tornadofx.*

class RecordResourcePage : Fragment() {
    private val viewModel: RecordResourceViewModel by inject()
    private val navigator: NavigationMediator by inject()

    val tabPane = JFXTabPane().apply {
        importStylesheet(resources.get("/css/tab-pane.css"))
    }

    override val root = tabPane

    private val tabs: List<RecordableTab> = listOf(
        recordableTab(ContentType.TITLE),
        recordableTab(ContentType.BODY)
    )

    private val breadCrumb = BreadCrumb().apply {
        titleProperty.bind(viewModel.breadcrumbTitleBinding)
        iconProperty.set(FontIcon(MaterialDesign.MDI_LINK_OFF))
        onClickAction {
            navigator.dock(this@RecordResourcePage)
        }
    }

    init {
        navigator.subscribe<PluginClosedEvent> {
            viewModel.currentTakeNumberProperty.set(null)
        }
    }

    private fun recordableTab(contentType: ContentType): RecordableTab {
        return RecordableTab(
            viewModel = viewModel.contentTypeToViewModelMap.getNotNull(contentType),
            onTabSelect = viewModel::onTabSelect
        )
    }

    override fun onDock() {
        tabs.forEach { recordableTab ->
            recordableTab.bindProperties()
            recordableTab.recordableProperty.onChangeAndDoNow { rec ->
                rec?.let {
                    if (!tabPane.tabs.contains(recordableTab)) tabPane.tabs.add(recordableTab)
                } ?: tabPane.tabs.remove(recordableTab)
            }
            recordableTab.currentTakeNumberProperty.onChangeAndDoNow {
                it?.let { take ->
                    if (take.toInt() > 0) viewModel.currentTakeNumberProperty.set(it.toInt())
                }
            }
        }
        viewModel.currentTakeNumberProperty.set(null)
        navigator.dock(this, breadCrumb)
    }

    override fun onUndock() {
        tabs.forEach { recordableTab ->
            recordableTab.unbindProperties()
        }
    }
}
