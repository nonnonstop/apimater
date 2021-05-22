package com.nonnonstop.apimate.translator

class GenericRegexTranslator(
    private val titleExtract: Pair<Regex, String>,
    private val titleRegex: Array<Pair<Regex, String>>?,
    private val resRegex: Regex,
    private val mailExtract: String,
    private val mailRegex: Array<Pair<Regex, String>>?,
    private val nameExtract: String,
    private val nameRegex: Array<Pair<Regex, String>>?,
    private val dateExtract: String,
    private val dateRegex: Array<Pair<Regex, String>>?,
    private val messageExtract: String,
    private val messageRegex: Array<Pair<Regex, String>>?
) {
    fun translate(writer: IDatWriter, str: String): Boolean {
        var title: String? = titleExtract.first.find(str)?.groupValues?.toTypedArray()?.let {
            titleExtract.second.format(*it).replace(titleRegex).toString()
        } ?: ""
        resRegex.findAll(str).forEach { resMatch ->
            val res = resMatch.groupValues.toTypedArray()
            val mail = mailExtract.format(*res).replace(mailRegex)
            val name = nameExtract.format(*res).replace(nameRegex)
            val date = dateExtract.format(*res).replace(dateRegex)
            val message = messageExtract.format(*res).replace(messageRegex)
            if (title != null) {
                writer.write("$name<>$mail<>$date<>$message<>$title\n")
                title = null
            } else {
                writer.write("$name<>$mail<>$date<>$message<>\n")
            }
        }
        if (title != null)
            throw IllegalArgumentException("Res not found")
        return true
    }

    private fun CharSequence.replace(regexes: Array<Pair<Regex, String>>?): CharSequence {
        regexes ?: return this
        return regexes.fold(this) { s, (regex, replacement) ->
            s.replace(regex, replacement)
        }
    }
}
