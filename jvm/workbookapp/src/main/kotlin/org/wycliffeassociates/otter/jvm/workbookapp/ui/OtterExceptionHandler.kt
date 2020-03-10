package org.wycliffeassociates.otter.jvm.workbookapp.ui

import javafx.application.Platform
import javafx.application.Platform.runLater
import javafx.scene.control.Dialog
import javafx.stage.Modality
import javafx.stage.StageStyle
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.jvm.controls.exception.exceptionDialog
import java.io.ByteArrayOutputStream
import java.io.PrintWriter
import tornadofx.*

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
                    if(sendReportProperty().get()) {

                    } else {
                        Platform.exit()
                    }
                }
            }

            dialogPane.stylesheets.addAll(listOf(
                javaClass.getResource("/css/root.css").toExternalForm(),
                javaClass.getResource("/css/button.css").toExternalForm(),
                javaClass.getResource("/css/exception-dialog.css").toExternalForm()
            ))

            initModality(Modality.APPLICATION_MODAL)
            initStyle(StageStyle.TRANSPARENT)

            show()
        }
    }
}

private fun stringFromError(e: Throwable): String {
    val out = ByteArrayOutputStream()
    val writer = PrintWriter(out)
    e.printStackTrace(writer)
    writer.close()
    return out.toString()
}