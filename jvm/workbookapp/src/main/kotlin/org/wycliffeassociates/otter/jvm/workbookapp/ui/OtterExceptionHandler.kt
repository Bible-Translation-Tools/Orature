package org.wycliffeassociates.otter.jvm.workbookapp.ui

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import javafx.application.Platform
import javafx.application.Platform.runLater
import javafx.scene.control.Dialog
import javafx.scene.paint.Color
import javafx.stage.Modality
import javafx.stage.StageStyle
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.OratureInfo
import org.wycliffeassociates.otter.jvm.controls.exception.exceptionDialog
import org.wycliffeassociates.otter.jvm.workbookapp.di.persistence.DaggerPersistenceComponent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.report.GithubReporter
import org.wycliffeassociates.otter.jvm.workbookapp.ui.system.AppInfo
import tornadofx.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintWriter
import java.util.*

class OtterExceptionHandler : Thread.UncaughtExceptionHandler {
    val log = LoggerFactory.getLogger(DefaultErrorHandler::class.java)

    class ErrorEvent(val thread: Thread, val error: Throwable) {
        internal var consumed = false
        fun consume() {
            consumed = true
        }
    }

    companion object {
        // By default, all error messages are shown. Override to decide if certain errors should be handled another way.
        // Call consume to avoid error dialog.
        var filter: (ErrorEvent) -> Unit = { }
    }

    override fun uncaughtException(t: Thread, error: Throwable) {
        log.error("Uncaught error", error)

        if (isCycle(error)) {
            log.info("Detected cycle handling error, aborting.", error)
        } else {
            val event = ErrorEvent(t, error)
            filter(event)

            if (!event.consumed) {
                event.consume()
                runLater {
                    showErrorDialog(error)
                }
            }
        }
    }

    private fun isCycle(error: Throwable) = error.stackTrace.any {
        it.className.startsWith("${javaClass.name}\$uncaughtException$")
    }

    private fun showErrorDialog(error: Throwable) {
        Dialog<Unit>().apply {
            dialogPane.content = exceptionDialog {
                titleTextProperty().set(FX.messages["needsRestart"])
                headerTextProperty().set(FX.messages["yourWorkSaved"])
                showMoreTextProperty().set(FX.messages["showMore"])
                showLessTextProperty().set(FX.messages["showLess"])
                sendReportTextProperty().set(FX.messages["sendErrorReport"])
                stackTraceProperty().set(stringFromError(error))
                closeTextProperty().set(FX.messages["closeApp"])
                onCloseAction {
                    if (sendReportProperty().get()) {
                        sendReport(error)
                            .doOnSubscribe { sendingReportProperty().set(true) }
                            .doOnComplete {
                                sendingReportProperty().set(false)
                                Platform.exit()
                            }
                            .subscribeOn(Schedulers.computation())
                            .subscribe()
                    } else {
                        Platform.exit()
                    }
                }
            }

            dialogPane.stylesheets.addAll(
                listOf(
                    javaClass.getResource("/css/root.css").toExternalForm(),
                    javaClass.getResource("/css/button.css").toExternalForm(),
                    javaClass.getResource("/css/exception-dialog.css").toExternalForm()
                )
            )

            initModality(Modality.APPLICATION_MODAL)
            initStyle(StageStyle.TRANSPARENT)
            dialogPane.scene.fill = Color.TRANSPARENT

            show()
        }
    }
}

private fun sendReport(error: Throwable): Completable {
    return Completable.fromAction {
        val props = githubProperties()
        if (props?.getProperty("repo-url") != null && props.getProperty("oauth-token") != null) {
            val githubReporter = GithubReporter(
                props.getProperty("repo-url"),
                props.getProperty("oauth-token")
            )
            githubReporter.reportCrash(
                getEnvironment(),
                stringFromError(error),
                getLog()
            )
        }
    }
}

private fun getEnvironment(): AppInfo {
    return AppInfo()
}

private fun getLog(): String? {
    val logFileName = OratureInfo.SUITE_NAME.toLowerCase()
    val logExt = ".log"
    val persistenceComponent = DaggerPersistenceComponent.builder().build()
    val directoryProvider = persistenceComponent.injectDirectoryProvider()
    val logFile = StringBuilder()
        .append(directoryProvider.logsDirectory.absolutePath)
        .append("/")
        .append(logFileName)
        .append(logExt)
        .toString()

    return try {
        File(logFile).inputStream().readBytes().toString(Charsets.UTF_8)
    } catch (e: Exception) {
        null
    }
}

private fun githubProperties(): Properties? {
    val prop = Properties()
    val inputStream = OtterExceptionHandler::class.java.classLoader.getResourceAsStream("github.properties")

    if (inputStream != null) {
        prop.load(inputStream)
        return prop
    }

    return null
}

private fun stringFromError(e: Throwable): String {
    val out = ByteArrayOutputStream()
    val writer = PrintWriter(out)
    e.printStackTrace(writer)
    writer.close()
    return out.toString()
}