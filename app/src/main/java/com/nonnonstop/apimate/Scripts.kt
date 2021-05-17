package com.nonnonstop.apimate

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import org.mozilla.javascript.ContextFactory
import org.mozilla.javascript.Function
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class Scripts(private val context: Context) {
    private companion object {
        const val TRANSLATOR_FILE = "translator.js"
        const val VIEWER_FILE = "viewer.js"
        const val PREFERENCE_FILE = "preference.js"

        val ALL_FILES = arrayOf(
            TRANSLATOR_FILE,
            VIEWER_FILE,
            PREFERENCE_FILE,
        )
    }

    init {
        val filesDir = context.filesDir
        ALL_FILES.forEach { fileName ->
            val file = File(filesDir, fileName)
            if (!file.exists()) {
                revertScript(fileName)
            }
        }
    }

    private fun execute(fileName: String, functionName: String, vararg args: Any?): Any? {
        val file = File(context.filesDir, fileName)
        val contextFactory = ContextFactory()
        val context = contextFactory.enterContext()
        try {
            context.optimizationLevel = -1
            val scope = context.initStandardObjects()
            Files.newBufferedReader(file.toPath()).use { reader ->
                context.evaluateReader(scope, reader, file.name, 1, null)
            }
            val callable = scope.get(functionName, scope) as Function
            return callable.call(context, scope, scope, args)
        } finally {
            org.mozilla.javascript.Context.exit()
        }
    }

    fun revertScript(fileName: String) {
        val file = File(context.filesDir, fileName)
        context.assets.open(fileName).use { stream ->
            Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    }

    fun revertAllScripts() {
        ALL_FILES.forEach { fileName ->
            revertScript(fileName)
        }
    }

    fun readScript(fileName: String): String {
        val file = File(context.filesDir, fileName)
        Files.newBufferedReader(file.toPath()).use { reader ->
            return reader.readText()
        }
    }

    fun writeScript(fileName: String, script: String) {
        val file = File(context.filesDir, fileName)
        Files.newBufferedWriter(file.toPath()).use { writer ->
            writer.write(script)
        }
    }

    fun translate(context: Context, htmlUrl: String) {
        execute(TRANSLATOR_FILE, "translate", context, htmlUrl)
    }

    fun onCreateDatPreference(fragment: Fragment, preference: Preference): Any? {
        return execute(PREFERENCE_FILE, "onCreateDatPreference", fragment, preference)
    }

    fun onClickDatPreference(fragment: Fragment, state: Any?) {
        execute(PREFERENCE_FILE, "onClickDatPreference", fragment, state)
    }

    fun view(activity: Activity, htmlUrl: String) {
        execute(VIEWER_FILE, "view", activity, htmlUrl)
    }
}