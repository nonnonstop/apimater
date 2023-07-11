package com.nonnonstop.apimate.translator

import okhttp3.Request
import timber.log.Timber

@Suppress("unused")
class BuiltinDatTranslator : IDatTranslator {
    private fun request(info: IDatInfo): String {
        val request = Request.Builder()
            .url("https://${info.server}.${info.domain}/${info.board}/dat/${info.thread}.dat")
            .build()
        HttpClients.client.newCall(request).execute().use { response ->
            if (response.code != 200) {
                throw Exception("Failed to connect (${response.code})")
            }
            val body = response.body?.source()?.readString(Charsets.MS932)
            body ?: throw Exception("Body not found")
            return body
        }
    }

    override fun translate(info: IDatInfo, writer: IDatWriter): Boolean {
        Timber.i("Start translation")
        val data = request(info)
        writer.use {
            writer.open(info)
            writer.write(data)
        }
        return true
    }
}