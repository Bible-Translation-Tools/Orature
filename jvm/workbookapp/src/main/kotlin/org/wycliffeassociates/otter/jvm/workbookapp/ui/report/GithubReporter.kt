package org.wycliffeassociates.otter.jvm.workbookapp.ui.report

import java.io.IOException
import java.util.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.wycliffeassociates.otter.jvm.device.system.Environment

private const val DEFAULT_CRASH_TITLE = "crash report"

class GithubReporter(
    private val repositoryUrl: String,
    private val githubOauth2Token: String
) {
    /**
     * Creates a crash issue on github.
     * @param stacktrace the stracktrace
     */
    @Throws(IOException::class)
    fun reportCrash(environment: Environment, stacktrace: String?, log: String?) {
        val title = DEFAULT_CRASH_TITLE
        val bodyBuf = StringBuffer()
        bodyBuf.append(getEnvironmentBlock(environment))
        bodyBuf.append(getStacktraceBlock(stacktrace))
        bodyBuf.append(getLogBlock(log))
        val labels = arrayOf("crash report")
        submit(generatePayload(title, bodyBuf.toString(), labels))
    }

    /**
     * Generates the json payload that will be set to the github server.
     * @param title the issue title
     * @param body the issue body
     * @param labels the issue labels. These will be created automatically went sent to github
     * @return JSONObject
     */
    private fun generatePayload(
        title: String,
        body: String,
        labels: Array<String>
    ): JSONObject {
        val json = JSONObject()
        try {
            json.put("title", title)
            json.put("body", body)
            val labelsJson = JSONArray()
            for (label in labels) {
                labelsJson.put(label)
            }
            json.put("labels", labelsJson)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return json
    }

    /**
     * Sends the new issue request to github
     * @param json the payload
     * @return
     */
    @Throws(IOException::class)
    private fun submit(json: JSONObject): String? {
        val github = Github(repositoryUrl)
        val headers: MutableList<Pair<String?, String?>> =
            ArrayList()
        headers.add(Pair("Authorization", "token $githubOauth2Token"))
        headers.add(Pair("Content-Type", "application/json"))
        return github.postRequest("", headers, json.toString())
    }

    /**
     * Generates the environment block
     * @return
     */
    private fun getEnvironmentBlock(environment: Environment): String? {
        val environmentBuf = StringBuffer()
        environmentBuf.append("\nEnvironment\n======\n")
        environmentBuf.append("Environment Key | Value" + "\n")
        environmentBuf.append(":----: | :----:" + "\n")
        environmentBuf.append("app version | ${environment.getVersion()}\n")
        for (data in environment.getSystemData()) {
            environmentBuf.append("${data.first} | ${data.second}\n")
        }
        return environmentBuf.toString()
    }

    /**
     * Generates the stacktrace block
     * @param stacktrace the stacktrace text
     * @return
     */
    private fun getStacktraceBlock(stacktrace: String?): String? {
        val stacktraceBuf = StringBuffer()
        if (stacktrace != null && !stacktrace.isEmpty()) {
            stacktraceBuf.append("\nStack trace\n======\n")
            stacktraceBuf.append("```java\n")
            stacktraceBuf.append(stacktrace + "\n")
            stacktraceBuf.append("```\n")
        }
        return stacktraceBuf.toString()
    }

    /**
     * Generates the notes block.
     * @param log
     * @return
     */
    private fun getLogBlock(log: String?): String? {
        val logBuf = StringBuffer()
        if (log != null && log.isNotEmpty()) {
            logBuf.append("\nLog history\n======\n")
            logBuf.append("```java\n")
            logBuf.append(log + "\n")
            logBuf.append("```\n")
        }
        return logBuf.toString()
    }
}
