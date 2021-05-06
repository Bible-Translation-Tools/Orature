package org.wycliffeassociates.otter.jvm.workbookapp.ui

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import io.sentry.Sentry
import javafx.application.Platform
import javafx.application.Platform.runLater
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.OratureInfo
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.controls.dialog.ExceptionDialog
import org.wycliffeassociates.otter.jvm.workbookapp.ui.report.GithubReporter
import org.wycliffeassociates.otter.jvm.workbookapp.ui.system.AppInfo
import tornadofx.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintWriter
import java.util.*

class OtterExceptionHandler(val directoryProvider: IDirectoryProvider) : Thread.UncaughtExceptionHandler {
    val logger = LoggerFactory.getLogger(DefaultErrorHandler::class.java)

    class ErrorEvent(val thread: Thread, val error: Throwable) {
        internal var consumed = false
        fun consume() {
            consumed = true
        }
    }

    init {
        Sentry.init()
    }

    companion object {
        // By default, all error messages are shown. Override to decide if certain errors should be handled another way.
        // Call consume to avoid error dialog.
        var filter: (ErrorEvent) -> Unit = { }
    }

    override fun uncaughtException(t: Thread, error: Throwable) {
        logger.error("Uncaught error", error)

        if (isCycle(error)) {
            logger.info("Detected cycle handling error, aborting.", error)
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
        ExceptionDialog().apply {
            titleTextProperty.set(FX.messages["needsRestart"])
            headerTextProperty.set(FX.messages["yourWorkSaved"])
            showMoreTextProperty.set(FX.messages["showMore"])
            showLessTextProperty.set(FX.messages["showLess"])
            sendReportTextProperty.set(FX.messages["sendErrorReport"])
            stackTraceProperty.set(stringFromError(error))
            closeTextProperty.set(FX.messages["closeApp"])

            onCloseAction {
                if (sendReportProperty.value) {
                    sendReport(error)
                        .doOnSubscribe { sendingReportProperty.set(true) }
                        .doOnComplete {
                            sendingReportProperty.set(false)
                            Platform.exit()
                        }
                        .subscribeOn(Schedulers.computation())
                        .doOnError { e -> logger.error("Error in showErrorDialog", e) }
                        .subscribe()
                } else {
                    Platform.exit()
                }
            }

            open()
        }
    }

    private fun sendReport(error: Throwable): Completable {
        return Completable.fromAction {
            sendGithubReport(error)
            sendSentryReport(error)
        }
    }

    private fun sendGithubReport(error: Throwable) {
        val props = githubProperties()
        if (props?.getProperty("repo-url") != null && props.getProperty("oauth-token") != null) {
            val githubReporter = GithubReporter(
                props.getProperty("repo-url"),
                props.getProperty("oauth-token")
            )
            githubReporter.reportCrash(
                getEnvironment(),
                stringFromError(error),
                getLog(),
                error.message
            )
        }
    }

    private fun sendSentryReport(error: Throwable) {
        val environment = getEnvironment()
        val sentryContext = Sentry.getContext()

        sentryContext.addTag("app version", environment.getVersion())
        environment.getSystemData().forEach {
            sentryContext.addTag(it.first, it.second)
        }

        Sentry.capture(error)
        Sentry.clearContext()
    }

    private fun getEnvironment(): AppInfo {
        return AppInfo()
    }

    private fun getLog(): String? {
        val logFileName = OratureInfo.SUITE_NAME.toLowerCase()
        val logExt = ".log"
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
}
