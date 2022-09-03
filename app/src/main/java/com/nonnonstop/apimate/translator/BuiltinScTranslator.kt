package com.nonnonstop.apimate.translator

import okhttp3.Request
import timber.log.Timber

@Suppress("unused")
class BuiltinScTranslator : IDatTranslator {
    private companion object {
        val subTranslator by lazy {
            GenericRegexTranslator(
                titleExtract = Pair(
                    """^.*?<>.*?<>.*?<>.*?<>(.*)$""".toRegex(RegexOption.MULTILINE),
                    "%2\$s\t"
                ),
                titleRegex = null,
                resRegex = """^(.*?)<>(.*?)<>(.*?)<>(.*?)<>.*$""".toRegex(RegexOption.MULTILINE),
                mailExtract = "%3\$s",
                mailRegex = null,
                nameExtract = "%2\$s",
                nameRegex = arrayOf(
                    Pair(
                        """アフィサイトへの＼\(\^o\^\)／です""".toRegex(),
                        "アフィサイトへの転載は禁止です"
                    ),
                    Pair(
                        """＠＼\(\^o\^\)／""".toRegex(),
                        "＠無断転載禁止"
                    ),
                ),
                dateExtract = "%4\$s",
                dateRegex = arrayOf(
                    Pair(
                        """<a href="http://be\.[25]ch\.net/user/(\d+)"[^>]*>\?([^<]+)</a>""".toRegex(),
                        "BE:$1-$2"
                    ),
                    Pair(
                        """( ID:[^ .]+)($| )""".toRegex(),
                        "$1.sc$2"
                    ),
                    Pair(
                        """(\.\d+)($| (?!ID:)(?!.*\.net))""".toRegex(),
                        "$1 .sc$2"
                    ),
                    Pair(
                        """\.net""".toRegex(),
                        ""
                    ),
                ),
                messageExtract = "%5\$s",
                messageRegex = arrayOf(
                    Pair(
                        """sssp://img\.2ch\.sc/ico/""".toRegex(),
                        "sssp://img.5ch.net/ico/"
                    ),
                    Pair(
                        """<img src="//([^"]+)">""".toRegex(),
                        "sssp://$1"
                    ),
                ),
            )
        }
    }

    private fun request(info: IDatInfo): String {
        val request = Request.Builder()
            .url("http://${info.server}.2ch.sc/${info.board}/dat/${info.thread}.dat")
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

    private fun convert(htmlData: String, writer: IDatWriter): Boolean {
        return subTranslator.translate(writer, htmlData)
    }

    override fun translate(info: IDatInfo, writer: IDatWriter): Boolean {
        Timber.i("Start translation")
        val data = request(info)
        return convert(data, writer)
    }
}