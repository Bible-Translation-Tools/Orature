package org.wycliffeassociates.otter.jvm.workbookplugin.plugin

import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadCrumb
import tornadofx.Fragment

abstract class PluginEntrypoint : Fragment() {
    abstract val breadCrumb: BreadCrumb?
}
