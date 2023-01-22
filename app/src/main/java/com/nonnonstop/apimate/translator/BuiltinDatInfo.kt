package com.nonnonstop.apimate.translator

@Suppress("unused")
class BuiltinDatInfo(fullUrl: String) : IDatInfo {
    private companion object {
        val regexUrl = """https?://(\w+)\.([\w.]+)/test/read\.(?:cgi|php)/(\w+)/(\d+)""".toRegex()
    }

    override val htmlUrl: String
    override val server: String
    override val domain: String
    override val board: String
    override val thread: String
    override val filename: String

    init {
        val match = regexUrl.find(fullUrl)
        match ?: throw IllegalArgumentException("Unexpected URL")
        htmlUrl = match.value
        server = match.groupValues[1]
        domain = match.groupValues[2]
        board = match.groupValues[3]
        thread = match.groupValues[4]
        filename = "${board}_${thread}.dat"
    }
}
