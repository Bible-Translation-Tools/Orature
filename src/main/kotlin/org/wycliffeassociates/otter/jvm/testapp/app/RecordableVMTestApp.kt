package org.wycliffeassociates.otter.jvm.testapp.app

import tornadofx.*

class RecordableVMTestApp: App(RecordableVMTestView::class) {
}
fun main(args: Array<String>) {
    launch<RecordableVMTestApp>(args)
}
