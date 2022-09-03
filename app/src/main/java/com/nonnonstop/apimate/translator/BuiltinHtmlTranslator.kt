package com.nonnonstop.apimate.translator

import okhttp3.Request
import timber.log.Timber

@Suppress("unused")
class BuiltinHtmlTranslator : IDatTranslator {
    private companion object {
        val subTranslatorV5 by lazy {
            GenericRegexTranslator(
                titleExtract = Pair(
                    """<title>(.*?) ?</title>""".toRegex(),
                    "%2\$s\t"
                ),
                titleRegex = null,
                resRegex = """<dt>\d+ ：(?:<a href="mailto:([^"]*)">(.*?)</a>|<font color=green>(.*?)</font>)：(.*?)<dd>(.*)<br><br>""".toRegex(),
                mailExtract = "%2\$s",
                mailRegex = null,
                nameExtract = "%3\$s%4\$s",
                nameRegex = arrayOf(
                    Pair(
                        """^<b>(.*)</b>$""".toRegex(),
                        "$1"
                    ),
                ),
                dateExtract = "%5\$s",
                dateRegex = arrayOf(
                    Pair(
                        """<a href="javascript:be\((\d+)\);">\?([^<]+)</a>""".toRegex(),
                        "BE:$1-$2"
                    ),
                ),
                messageExtract = "%6\$s",
                messageRegex = arrayOf(
                    Pair(
                        """<a [^>]*>(https?://[^<]*)</a>""".toRegex(),
                        "$1"
                    ),
                    Pair(
                        """(<br> )(?=<br>)""".toRegex(),
                        "$1"
                    ),
                    Pair(
                        """<img src="http://([^"]+)">""".toRegex(),
                        "sssp://$1"
                    ),
                ),
            )
        }
        val subTranslatorV6 by lazy {
            GenericRegexTranslator(
                titleExtract = Pair(
                    """<title>(.*?)\n</title>""".toRegex(),
                    "%2\$s\t"
                ),
                titleRegex = null,
                resRegex = """<div class="post"[^>]*><div class="number">(?!0)\d+ : </div><div class="name"><b>(?:<a href="mailto:([^"]*)">)?(.*?)(?:</a>)?</b></div><div class="date">(.*?)</div><div class="message">(.*?)</div></div>""".toRegex(),
                mailExtract = "%2\$s",
                mailRegex = null,
                nameExtract = "%3\$s",
                nameRegex = null,
                dateExtract = "%4\$s",
                dateRegex = arrayOf(
                    Pair(
                        """</div><div class="be [^"]+"><a href="http://be\.[25]ch\.net/user/(\d+)"[^>]*>\?([^<]+)</a>""".toRegex(),
                        "BE:$1-$2"
                    ),
                ),
                messageExtract = "%5\$s",
                messageRegex = arrayOf(
                    Pair(
                        """<a [^>]*>(https?://[^<]*)</a>""".toRegex(),
                        "$1"
                    ),
                    Pair(
                        """(<br> )(?=<br>)""".toRegex(),
                        "$1"
                    ),
                    Pair(
                        """<img src="//([^"]+)">""".toRegex(),
                        "sssp://$1"
                    ),
                ),
            )
        }
        val subTranslatorV7 by lazy {
            GenericRegexTranslator(
                titleExtract = Pair(
                    """<title>(.*?)\n</title>""".toRegex(),
                    "%2\$s\t"
                ),
                titleRegex = null,
                resRegex = """<div class="post"[^>]*><div class="meta"><span class="number">0*\d+</span><span class="name"><b>(?:<a href="mailto:([^"]*)">)?(.*?)(?:</a>)?</b></span><span class="date">(.*?)</span><span class="uid">(.*?)</span></div><div class="message"><span class="escaped">(.*?)</span></div></div>""".toRegex(),
                mailExtract = "%2\$s",
                mailRegex = null,
                nameExtract = "%3\$s",
                nameRegex = null,
                dateExtract = "%4\$s %5\$s",
                dateRegex = arrayOf(
                    Pair(
                        """</span><span class="be [^"]+"><a href="http://be\.[25]ch\.net/user/(\d+)"[^>]*>\?([^<]+)</a>""".toRegex(),
                        "BE:$1-$2"
                    ),
                ),
                messageExtract = "%6\$s",
                messageRegex = arrayOf(
                    Pair(
                        """<a [^>]*>(https?://[^<]*)</a>""".toRegex(),
                        "$1"
                    ),
                    Pair(
                        """(<br> )(?=<br>)""".toRegex(),
                        "$1"
                    ),
                    Pair(
                        """<img src="//([^"]+)">""".toRegex(),
                        "sssp://$1"
                    ),
                ),
            )
        }
        val subTranslatorPink by lazy {
            GenericRegexTranslator(
                titleExtract = Pair(
                    """<title>(.*?)\n</title>""".toRegex(),
                    "%2\$s\t"
                ),
                titleRegex = null,
                resRegex = """<dl class="post"[^>]*><dt class=""><span class="number">(?!0)(\d+) : </span><span class="name"><b>(?:<a href="mailto:([^"]*)">(.*?)</a>|<font color="green">(.*?)</font>)</b></span><span class="date">(.*?)</(?:span|div)></dt><dd class="thread_in">(.*?)</dd></dl>""".toRegex(),
                mailExtract = "%2\$s",
                mailRegex = null,
                nameExtract = "%3\$s%4\$s",
                nameRegex = null,
                dateExtract = "%5\$s",
                dateRegex = arrayOf(
                    Pair(
                        """</span><div class="be [^"]+"><a href="https://be\.[25]ch\.net/user/(\d+)"[^>]*>\?([^<]+)</a>""".toRegex(),
                        "BE:$1-$2"
                    ),
                ),
                messageExtract = "%6\$s",
                messageRegex = arrayOf(
                    Pair(
                        """<a [^>]*>(https?://[^<]*)</a>""".toRegex(),
                        "$1"
                    ),
                    Pair(
                        """(<br> )(?=<br>)""".toRegex(),
                        "$1"
                    ),
                    Pair(
                        """<img src="http://([^"]+)">""".toRegex(),
                        "sssp://$1"
                    ),
                ),
            )
        }
    }

    private fun request(info: IDatInfo): String {
        val request = Request.Builder()
            .url(info.htmlUrl)
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
        val translator = when {
            htmlData.contains("""<div class="topmenu">""") -> subTranslatorV6
            htmlData.contains("""<div class="topmenu centered">""") -> subTranslatorV7
            htmlData.contains("""<ul class="topmenu">""") -> subTranslatorPink
            else -> subTranslatorV5
        }
        return translator.translate(writer, htmlData)
    }

    override fun translate(info: IDatInfo, writer: IDatWriter): Boolean {
        Timber.i("Start translation")
        val data = request(info)
        return convert(data, writer)
    }
}