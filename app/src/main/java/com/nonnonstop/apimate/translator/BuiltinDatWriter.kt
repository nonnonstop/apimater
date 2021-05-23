package com.nonnonstop.apimate.translator

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import java.io.BufferedWriter
import java.io.IOException
import java.io.OutputStream
import java.io.OutputStreamWriter

class BuiltinDatWriter(private val context: Context) : IDatWriter {
    private val treeUri = context
        .getSharedPreferences("DatWriter", Context.MODE_PRIVATE)
        .getString("documentTreeUri", null)
        ?.let {
            Uri.parse(it)
        }
        ?: throw IOException("Dat directory is not initialized")
    private var datInfo: IDatInfo? = null
    private var outputStream: OutputStream? = null
    private var outputStreamWriter: OutputStreamWriter? = null
    private var bufferedWriter: BufferedWriter? = null

    override fun open(info: IDatInfo) {
        datInfo = info
    }

    private fun ensureOpen() {
        if (bufferedWriter != null)
            return
        val filename = datInfo?.filename ?: throw IOException("Failed to get dat info")
        val documentUri = DocumentsContract.buildDocumentUriUsingTree(
            treeUri, "${DocumentsContract.getTreeDocumentId(treeUri)}/${filename}"
        )
        val documentFile = DocumentFile.fromSingleUri(context, documentUri)
            ?: throw IOException("Failed to get dat file")
        if (!documentFile.exists()) {
            val documentDir = DocumentFile.fromTreeUri(context, treeUri)
                ?: throw IOException("Failed to get dat directory")
            documentDir.createFile("application/octet-stream", filename)
                ?: throw IOException("Failed to create dat file")
        }
        try {
            outputStream = context.contentResolver.openOutputStream(documentUri, "wt")
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