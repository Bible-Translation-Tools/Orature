package org.wycliffeassociates.otter.common.domain.chunking

interface IChunkOperation {
    fun apply()
    fun undo()
    fun redo()
}