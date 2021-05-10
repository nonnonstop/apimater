package com.nonnonstop.apimate

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import timber.log.Timber
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption

class FileTree(context: Context) : Timber.Tree() {
    private val baseDir: File? = context.externalCacheDir

    @SuppressLint("LogNotTimber")
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        baseDir ?: return
        try {
            val file = File(baseDir, "exception.txt")
            Files.newBufferedWriter(
                file.toPath(),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
            ).use { writer ->
                writer.write("----------------------")
                writer.newLine()
                writer.write(message)
                writer.newLine()
            }
        } catch (ex: Exception) {
            Log.e("FileTree", ex.toString())
        }
    }
}