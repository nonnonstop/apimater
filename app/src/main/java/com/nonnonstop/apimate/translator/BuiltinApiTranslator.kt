package com.nonnonstop.apimate.translator

import okhttp3.FormBody
import okhttp3.Request
import org.json.JSONObject
import timber.log.Timber
import java.math.BigInteger
import java.nio.charset.Charset
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class BuiltinApiTranslator(
    hmKey: String,
    private val appKey: String,
    private val ua: String,
    private val xua: String,
    private val sid: String,
    private val sleepMillis: Long
) : IDatTranslator {
    private val mac: Mac

    init {
        val secret = SecretKeySpec(hmKey.toByteArray(), "HmacSHA256")
        mac = Mac.getInstance("HmacSHA256")
        mac.init(secret)
    }

    override fun translate(info: IDatInfo, writer: IDatWriter): Boolean {
        Timber.i("Start translation")
        if (appKey.length != 30) {
            Timber.i("Invalid app key")
            return false
        }
        val id = JSONObject(
            request(
                "/api/v1/prepare/${info.server}/${info.board}/${info.thread}",
                Charsets.MS932
            )
        )["id"]
        Thread.sleep(sleepMillis)
        val dat = request("/api/v1/get/$id", Charsets.MS932)
        writer.use {
            writer.open(info)
            writer.write(dat)
        }
        return true
    }

    fun request(path: String, charset: Charset): String {
        val message = (path + sid + appKey).toByteArray()
        val hobo = "%64x".format(BigInteger(1, mac.doFinal(message)))

        val formBody = FormBody.Builder()
            .add("sid", sid)
            .add("hobo", hobo)
            .add("appkey", appKey)
            .build()
        val request = Request.Builder()
            .url("https://kg.dev5ch.net$path")
            .addHeader("User-Agent", ua)
            .addHeader("X-2ch-UA", xua)
            .post(formBody)
            .build()
        HttpClients.client.newCall(request).execute().use { response ->
            if (response.code != 200) {
                throw Exception("Failed to connect (${response.code})")
            }
            val body = response.body?.source()?.readString(charset)
            body ?: throw Exception("Body not found")
            return body
        }
    }
}