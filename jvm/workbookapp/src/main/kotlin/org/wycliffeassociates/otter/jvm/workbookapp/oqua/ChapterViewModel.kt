package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import javafx.beans.property.SimpleObjectProperty
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.DirectoryProvider
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

class ChapterViewModel : ViewModel() {
    private val directoryProvider = DirectoryProvider("oqua")
    private val wbDataStore: WorkbookDataStore by inject()
    private lateinit var gradeRepo: GradeRepository

    val settingsViewModel: SettingsViewModel by inject()

    val questions = observableListOf<Question>()
    val audioPlayerProperty = SimpleObjectProperty<IAudioPlayer?>(null)

    lateinit var workbook: Workbook
    var chapterNumber = 0

    fun dock() {
        gradeRepo = GradeRepository(directoryProvider, wbDataStore)

        workbook = wbDataStore.workbook
        chapterNumber = wbDataStore.chapter.sort

        updatePlayer()

        val newQuestions = getQuestions()

        gradeRepo.readGradeFile(chapterNumber)?.grades?.let { grades ->
            loadResults(newQuestions, grades)
        }
        questions.setAll(newQuestions)
    }

    fun undock() {
        gradeRepo.writeGradeFile(workbook, chapterNumber, questions)
    }

    private fun updatePlayer() {
        wbDataStore.chapter.audio.selected.value?.value?.let { take ->
            val audioPlayer = (app as IDependencyGraphProvider).dependencyGraph.injectPlayer()
            audioPlayer.load(take.file)
            audioPlayerProperty.set(audioPlayer)
        }
    }

    private fun getQuestions(): MutableList<Question> {
        return wbDataStore.getSourceChapter().blockingGet()?.let { chapter ->
            questionsDedup(chapter.chunks.toList().blockingGet().mapNotNull(::questionFromChunk)).toMutableList()
        } ?: mutableListOf<Question>()
    }

    private fun loadResults(questions: MutableList<Question>, grades: List<Grade>) {
        grades.forEach { grade ->
            questions.find { question ->
                (question.question == grade.question) && (question.answer == grade.answer)
            }?.run {
                result = grade.result
            }
        }
    }
}