package com.nonnonstop.apimate

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import okio.use
import org.mozilla.javascript.ContextFactory
import org.mozilla.javascript.Function
import java.io.File

class Scripts(private val context: Context) {
    enum class Name {
        TRANSLATOR,
        VIEWER,
        PREFERENCE
    }

    private companion object {
        val INFO_LIST = mapOf(
            Name.TRANSLATOR to Script(
                "translator.js",
                arrayOf(
                    ScriptPreset(
                        "translator.js",
                        R.string.script_preset_translator_900,
                    ),
                    ScriptPreset(
                        "preset/translator_800.js",
                        R.string.script_preset_translator_800,
                    ),
                    ScriptPreset(
                        "preset/translator_700.js",
                        R.string.script_preset_translator_700,
                    ),
                ),
            ),
            Name.VIEWER to Script(
                "viewer.js",
                arrayOf(
                    ScriptPreset(
                        "viewer.js",
                        R.string.script_preset_viewer_600,
                    ),
                    ScriptPreset(
                        "preset/viewer_400.js",
                        R.string.script_preset_viewer_400,
                    ),
                ),
            ),
            Name.PREFERENCE to Script(
                "preference.js",
                arrayOf(
                    ScriptPreset(
                        "preference.js",
                        R.string.script_preset_preference_400,
                    ),
                ),
            ),
        )
    }

    init {
        val filesDir = context.filesDir
        INFO_LIST.forEach { (name, info) ->
            val fileName = info.filename
            val file = File(filesDir, fileName)
            if (!file.exists()) {
                revertScript(name)
            }
        }
    }

    private fun execute(name: Name, functionName: String, vararg args: Any?): Any? {
        val file = File(context.filesDir, INFO_LIST[name]!!.filename)
        val contextFactory = ContextFactory()
        val context = contextFactory.enterContext()
        try {
            context.optimizationLevel = -1
            val scope = context.initStandardObjects()
            file.reader().use { reader ->
                context.evaluateReader(scope, reader, file.name, 1, null)
            }
            val callable = scope.get(functionName, scope) as Function
            return callable.call(context, scope, scope, args)
        } finally {
            org.mozilla.javascript.Context.exit()
        }
    }

    fun revertScript(name: Name) {
        val info = INFO_LIST[name]!!
        val file = File(context.filesDir, info.filename)
        context.assets.open(info.presets[0].filename).use { inputStream ->
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    fun revertAllScripts() {
        INFO_LIST.forEach { (name, _) ->
            revertScript(name)
        }
    }

    fun readScript(name: Name): String {
        val info = INFO_LIST[name]!!
        return File(context.filesDir, info.filename).readText()
    }

    fun writeScript(name: Name, script: String) {
        val info = INFO_LIST[name]!!
        File(context.filesDir, info.filename).writeText(script)
    }

    fun getPresets(name: Name): Array<ScriptPreset> {
        val info = INFO_LIST[name]!!
        return info.presets
    }

    fun readPreset(filename: String): String {
        context.assets.open(filename).use { inputStream ->
            inputStream.bufferedReader().use { reader ->
                return reader.readText()
            }
        }
    }

    fun translate(context: Context, htmlUrl: String) {
        execute(Name.TRANSLATOR, "translate", context, htmlUrl)
    }

    fun onCreateDatPreference(fragment: Fragment, preference: Preference): Any? {
        return execute(Name.PREFERENCE, "onCreateDatPreference", fragment, preference)
    }

    fun onClickDatPreference(fragment: Fragment, state: Any?) {
        execute(Name.PREFERENCE, "onClickDatPreference", fragment, state)
    }

    fun view(activity: Activity, htmlUrl: String) {
        execute(Name.VIEWER, "view", activity, htmlUrl)
    }
}