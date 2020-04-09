package org.wycliffeassociates.otter.jvm.device.system

abstract class Environment {
    abstract fun getSystemData(): List<Pair<String, String>>
    abstract fun getVersion(): String?
}