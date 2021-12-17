/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.workbookapp.ui

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import io.sentry.Attachment
import io.sentry.Sentry
import javafx.application.Platform
import javafx.application.Platform.runLater
import javafx.geometry.NodeOrientation
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.OratureInfo
import org.wycliffeassociates.otter.common.data.ErrorReportException
import org.wycliffeassociates.otter.common.domain.languages.LocaleLanguage
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.controls.dialog.ExceptionDialog
import org.wycliffeassociates.otter.jvm.workbookapp.ui.report.GithubReporter
import org.wycliffeassociates.otter.jvm.workbookapp.ui.system.AppInfo
import tornadofx.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintWriter
import java.util.*

class OtterExceptionHandler(
    val directoryProvider: IDirectoryProvider,
    val localeLanguage: LocaleLanguage
) : Thread.UncaughtExceptionHandler {
    val logger = LoggerFactory.getLogger(DefaultErrorHandler::class.java)

    class ErrorEvent(val thread: Thread, val error: Throwable) {
        internal var consumed = false
        fun consume() {
            consumed = true
        }
    }

    init {
        initializeSentry()
    }

    private fun initializeSentry() {
        // Empty string for dsn disables the sentry SDK, used for running in the IDE
        var sentryDsn = ""
        try {
            // This file is configured in build.gradle, set via github actions
            val sentryProperties = ResourceBundle.getBundle("sentry")
            sentryDsn = sentryProperties["dsn"]
        } catch (e: MissingResourceException) {
            logger.info("Sentry disabled due to missing sentry.properties file")
        }
        Sentry.init {
            it.dsn = sentryDsn
        }
    }

    // By default, all error messages are shown. Override to decide if certain errors should be handled another way.
    // Call consume to avoid error dialog.
    private val filter: (ErrorEvent) -> Unit = { event ->
        if (event.error is ErrorReportException) {
            event.consume()
            logger.info("A custom exception was reported: ${event.error.message}")
            runLater {
                sendReport(event.error)
                    .subscribeOn(Schedulers.io())
                    .doOnError { e -> logger.error("Error while processing custom exception", e) }
                    .subscribe()
            }
        }
    }

    override fun uncaughtException(t: Thread, error: Throwable) {
        if (isCycle(error)) {
            logger.info("Detected cycle handling error, aborting.", error)
        } else {
            val event = ErrorEvent(t, error)
            filter(event)

            if (!event.consumed) {
                logger.error("Uncaught error", error)
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
        val orientation = when (localeLanguage.preferredLanguage?.direction) {
            "rtl" -> NodeOrientation.RIGHT_TO_LEFT
            else -> NodeOrientation.LEFT_TO_RIGHT
        }
        ExceptionDialog().apply {
            titleTextProperty.set(FX.messages["needsRestart"])
            headerTextProperty.set(FX.messages["yourWorkSaved"])
            showMoreTextProperty.set(FX.messages["showMore"])
            showLessTextProperty.set(FX.messages["showLess"])
            sendReportTextProperty.set(FX.messages["sendErrorReport"])
            stackTraceProperty.set(stringFromError(error))
            closeTextProperty.set(FX.messages["closeApp"])
            orientationProperty.set(orientation)

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
        Sentry.withScope { scope ->
            scope.setTag("app version", environment.getVersion() ?: "")
            environment.getSystemData().forEach {
                scope.setTag(it.first, it.second)
            }
            scope.addAttachment(Attachment(File(directoryProvider.logsDirectory,"orature.log").absolutePath))

            Sentry.captureException(error)
        }
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
