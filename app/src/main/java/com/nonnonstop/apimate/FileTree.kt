package com.nonnonstop.apimate

import android.annotation.SuppressLint
import android.util.Log
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.*
import java.util.logging.Formatter

@SuppressLint("LogNotTimber")
class FileTree(baseDir: File) : Timber.Tree() {
    companion object {
        private const val LOG_LIMIT = 512 * 1024
        private const val LOG_COUNT = 2
    }

    private var logger: Logger

    init {
        val path = File(baseDir, "exception%g.txt").absolutePath
        val formatter = MyFormatter()
        val handler = FileHandler(path, LOG_LIMIT, LOG_COUNT, true)
        handler.formatter = formatter
        logger = MyLogger("apimater").apply {
            level = Level.ALL
            addHandler(handler)
        }
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val level = when (priority) {
            Log.VERBOSE -> Level.FINER
            Log.DEBUG -> Level.FINE
            Log.INFO -> Level.INFO
            Log.WARN -> Level.WARNING
            Log.ERROR -> Level.SEVERE
            Log.ASSERT -> Level.SEVERE
            else -> Level.FINEST
        }
        logger.log(level, message, t)
    }

    private class MyLogger(name: String) : Logger(name, null)

    private class MyFormatter : Formatter() {
        companion object {
            private val dateFormatter = SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm'Z'",
                Locale.ENGLISH
            )

            init {
                dateFormatter.timeZone = TimeZone.getTimeZone("UTC")
            }
        }

        override fun format(record: LogRecord): String {
            return StringBuilder()
                .appendLine("----------------------")
                .append(dateFormatter.format(Date(record.millis)))
                .append(" ")
                .append(record.level.name)
                .append(": ")
                .appendLine(record.message)
                .toString()
        }
    }
}