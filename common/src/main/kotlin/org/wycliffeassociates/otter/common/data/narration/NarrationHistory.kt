package org.wycliffeassociates.otter.common.data.narration

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.wycliffeassociates.otter.common.data.primitives.VerseNode
import java.io.File
import java.util.*

class NarrationHistory {
    private val undoStack = ArrayDeque<NarrationAction>()
    private val redoStack = ArrayDeque<NarrationAction>()

    private var saveHistoryFile: File? = null

    fun execute(action: NarrationAction) {
        action.execute()
        undoStack.addLast(action)
        redoStack.clear()
    }

    fun undo() {
        if (undoStack.isEmpty()) return

        val action = undoStack.removeLast()

        action.undo()
        redoStack.addLast(action)
    }

    fun redo() {
        if (redoStack.isEmpty()) return

        val action = redoStack.removeLast()

        action.redo()
        undoStack.addLast(action)
    }

    fun hasUndo(): Boolean {
        return undoStack.isNotEmpty()
    }

    fun hasRedo(): Boolean {
        return redoStack.isNotEmpty()
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }

    fun initSavedHistoryFile(parentDir: File) {
        saveHistoryFile = File(parentDir, "narration.json").also { file ->
            if (!file.exists()) {
                file.createNewFile()
                file.writeText("[]")
            }
        }
    }

    fun updateSavedHistoryFile(verses: List<VerseNode>) {
        val json = ObjectMapper().writeValueAsString(verses)
        saveHistoryFile?.writeText(json)
    }

    fun loadSavedHistoryFile(): List<VerseNode> {
        return saveHistoryFile?.let { file ->
            val json = file.readText()
            val mapper = ObjectMapper().registerKotlinModule()

            val reference = object: TypeReference<List<VerseNode>>(){}
            mapper.readValue(json, reference)
        } ?: listOf()
    }
}