package com.nonnonstop.apimate.translator

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.BufferedWriter
import java.io.IOException
import java.io.OutputStream
import java.io.OutputStreamWriter

class BuiltinDatWriter(context: Context) : IDatWriter {
    private val baseDir: DocumentFile
    private val contentResolver: ContentResolver
    private var datInfo: IDatInfo? = null
    private var outputStream: OutputStream? = null
    private var outputStreamWriter: OutputStreamWriter? = null
    private var bufferedWriter: BufferedWriter? = null

    init {
        val baseUri = context
            .getSharedPreferences("DatWriter", Context.MODE_PRIVATE)
            .getString("documentTreeUri", null)
            ?.let {
                Uri.parse(it)
            }
            ?: throw IOException("Dat directory is not initialized")
        baseDir = DocumentFile
            .fromTreeUri(context, baseUri)
            ?: throw IOException("Failed to open dat directory")
        contentResolver = context.contentResolver
    }

    override fun open(info: IDatInfo) {
        datInfo = info
    }

    private fun ensureOpen() {
        if (bufferedWriter != null)
            return
        val filename = datInfo?.filename ?: throw IOException("Failed to get dat info")
        val file = baseDir.findFile(filename)
            ?: baseDir.createFile("application/octet-stream", filename)
            ?: throw IOException("Failed to create dat file")
        try {
            outputStream = contentResolver.openOutputStream(file.uri, "wt")
                ?: throw IOException("Failed to open dat file")
            outputStreamWriter = outputStream!!.writer(Charsets.MS932)
            bufferedWriter = outputStreamWriter!!.buffered()
        } catch (ex: Exception) {
            close()
            throw ex
        }
    }

    override fun write(str: String) {
        ensureOpen()
        bufferedWriter?.write(str) ?: throw IOException("Failed to write dat file")
    }

    override fun close() {
        datInfo = null
        bufferedWriter?.close()
        bufferedWriter = null
        outputStreamWriter?.close()
        outputStreamWriter = null
        outputStream?.close()
        outputStream = null
    }
}