package com.nonnonstop.apimate.translator

import okhttp3.Request
import org.json.JSONObject
import timber.log.Timber

@Suppress("unused")
class BuiltinItestTranslator : IDatTranslator {
    private companion object {
        fun getRandomString(): String {
            val charset = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz0123456789"
            return (1..10)
                .map { charset.random() }
                .joinToString("")
        }
    }

    private fun request(info: IDatInfo): String {
        val request = Request.Builder()
            .url("https://itest.${info.domain}/public/newapi/client.php?subdomain=${info.server}&board=${info.board}&dat=${info.thread}&rand=${getRandomString()}")
            .build()
        HttpClients.client.newCall(request).execute().use { response ->
            if (response.code != 200) {
                throw Exception("Failed to connect (${response.code})")
            }
            val body = response.body?.source()?.readString(Charsets.UTF8)
            body ?: throw Exception("Body not found")
            return body
        }
    }

    override fun translate(info: IDatInfo, writer: IDatWriter): Boolean {
        Timber.i("Start translation")
        val sb = StringBuilder()
        Timber.i("data" + request(info))
        val data = JSONObject(request(info))
        var title = data.getJSONArray("thread").getString(5)
        val comments = data.getJSONArray("comments")
        for (i in 0 until comments.length()) {
            val comment = comments.getJSONArray(i)
            sb.append(comment.getString(1))
            sb.append("<>")
            sb.append(comment.getString(2))
            sb.append("<>")
            sb.append(comment.getString(3))
            comment.getString(4).let { id ->
                if (!id.isNullOrEmpty()) {
                    if (id.contains("ID:") || id.contains("発信元:")) {
                        sb.append(" ")
                        sb.append(id)
                    } else {
                        sb.append(" ID:")
                        sb.append(id)
                    }
                }
            }
            comment.getString(5).let { id ->
                if (!id.isNullOrEmpty()) {
                    sb.append(" BE:")
                    sb.append(id)
                }
            }
            sb.append("<>")
            sb.append(comment.getString(6))
            sb.append("<>")
            if (title != null) {
                sb.append(title)
                title = null
            }
            sb.append("\n")
        }
        writer.use {
            writer.open(info)
            writer.write(sb.toString())
        }
        return true
    }
}