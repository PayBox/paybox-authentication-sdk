package money.paybox.authentication_sdk.api

import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.ConnectException
import java.net.URL
import java.net.URLEncoder
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException
import javax.net.ssl.HttpsURLConnection
import kotlin.collections.HashMap


internal class APIHandler(private val slug: String, private val secretKey: String, private var language: Language, private val listener: ApiListener) {
    private fun request(requestData: () -> RequestData) {
        val thread = Thread {
            val result = connection(requestData())
            resolveResponse(result)
        }

        thread.start()
    }

    private fun connection(requestData: RequestData): ResponseData {
        try {
            val credentials = "$slug:$secretKey".toByteArray(Charsets.UTF_8)
            val base64 = android.util.Base64.encodeToString(credentials, android.util.Base64.NO_WRAP)

            val url = if (requestData.url.startsWith("http")) requestData.url
            else Constants.SERVER_URL + requestData.url

            val urlCon = URL(url)
            HttpsURLConnection.setDefaultSSLSocketFactory(TLSSocketFactory())
            val connection = urlCon.openConnection() as HttpsURLConnection
            connection.connectTimeout = 25000
            connection.connectTimeout = 25000
            connection.requestMethod = requestData.method.name
            connection.setRequestProperty("Content-Language", language.name)
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", requestData.accessType)

            if(slug.isNotEmpty() && secretKey.isNotEmpty())
                connection.setRequestProperty("Authorization", "Basic $base64")

            connection.useCaches = false
            connection.allowUserInteraction = false
            connection.doInput = true
            connection.doOutput = true

            val stream = connection.outputStream
            val writer = BufferedWriter(
                OutputStreamWriter(stream, "UTF-8")
            )

            if(requestData.method == RequestMethod.POST)
                writer.write(makeBody(requestData.params))
            else
                writer.write(makeParams(requestData.params))

            writer.flush()
            writer.close()
            stream.close()
            connection.connect()
            val statusCode = connection.responseCode
            var line = ""
            if (statusCode == HttpsURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                val br = BufferedReader(InputStreamReader(inputStream))
                br.readLines().forEach {
                    line += it
                }
                br.close()
                inputStream.close()
            } else {
                line = connection.responseMessage
            }
            return ResponseData(statusCode, line, requestData.url)
        } catch (e: Exception) {
            if (e is UnknownHostException || e is ConnectException || e is TimeoutException) {
                return ResponseData(522, "Connection error", requestData.url, true)
            }
        }
        return ResponseData(520, "Unknown error", requestData.url, true)
    }

    private fun makeBody(params: HashMap<String, Any>): String {
        val json = JSONObject()
        for (param in params) {
            json.put(param.key, param.value)
        }

        return json.toString()
    }

    private fun makeParams(params: HashMap<String, Any>): String {
        val result = StringBuffer()
        var first = true
        for ((key, value) in params) {
            if (first) {
                first = false
            } else {
                result.append("&")
            }
            result.append(URLEncoder.encode(key, "UTF-8"))
            result.append("=")
            result.append(URLEncoder.encode(value.toString(), "UTF-8"))
        }

        return result.toString()
    }

    private fun resolveResponse(responseData: ResponseData?) {
        responseData?.let {
            when(it.url) {
                Constants.METHOD_AUTH -> {
                    listener.onAuth(it)
                }

                Constants.METHOD_CHECK -> {
                    listener.onCheck(it)
                }
            }
        }
    }

    fun auth(phone: String) {
        val map = HashMap<String, Any>()
        map["phone"] = phone
        map["language"] = language.name
        map["confirm_type"] = "otp"

        request {
            RequestData(map, RequestMethod.POST, Constants.METHOD_AUTH, "text/html")
        }
    }

    fun check(map: HashMap<String, Any>) {
        request {
            RequestData(map, RequestMethod.POST, Constants.METHOD_CHECK, "application/json")
        }
    }
}