package com.nonnonstop.apimate.preflaunch

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

class BuiltinPrefLauncher {
    fun onCreateDatPreference(fragment: Fragment): ActivityResultLauncher<Uri?> {
        val context = fragment.requireContext().applicationContext
        return fragment.registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            uri ?: return@registerForActivityResult
            val flags =
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)

            val pref = context.getSharedPreferences("DatWriter", Context.MODE_PRIVATE)
            with(pref.edit()) {
                putString("documentTreeUri", uri.toString())
                apply()
            }
        }
    }

    @Suppress("unused")
    fun onClickDatPreference(
        launcher: ActivityResultLauncher<Uri>,
        authority: String,
        documentId: String
    ) {
        val documentUri = DocumentsContract.buildDocumentUri(authority, documentId)
        launcher.launch(documentUri)
    }
}