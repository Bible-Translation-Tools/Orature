package org.wycliffeassociates.otter.jvm.workbookapp.ui.report

import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class Github(private val apiUrl: String) {
    /**
     * Performs a post request against the github api.
     * @param apiMethod
     * @param headers
     * @param payload the data to submit to the server
     * @return null if the postRequest fails or the response
     */
    @Throws(IOException::class)
    fun postRequest(
        apiMethod: String,
        headers: List<Pair<String?, String?>>?,
        payload: String?
    ): String? {
        val urlString = if (apiMethod.isEmpty()) apiUrl else "$apiUrl/$apiMethod"
        val url = URL(urlString)
        var urlConnection: HttpURLConnection? = null

        return try {
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.requestMethod = "POST"
            // headers
            if (headers != null) {
                for ((first, second) in headers) {
                    urlConnection.setRequestProperty(first, second)
                }
            }
            // payload
            if (payload != null) {
                urlConnection.doOutput = true
                DataOutputStream(urlConnection.outputStream).use {
                    it.writeBytes(payload)
                    it.flush()
                }
            }
            // response
            val bis = BufferedInputStream(urlConnection.inputStream)
            val baos = ByteArrayOutputStream()
            var current: Int
            while (bis.read().also { current = it } != -1) {
                baos.write(current)
            }
            baos.toString("UTF-8")
        } finally {
            urlConnection?.disconnect()
        }
    }
}