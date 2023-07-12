package com.nonnonstop.apimate

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult

class ScriptPresetFragment : DialogFragment() {
    companion object {
        const val ARG_SCRIPT_NAME_KEY = "script_name"
        const val ARG_FILENAME_KEY = "filename"
        const val RESULT_KEY = "script_preset_fragment_result"

        fun newInstance(scriptName: Scripts.Name): ScriptPresetFragment {
            val fragment = ScriptPresetFragment()
            fragment.arguments = bundleOf(
                ARG_SCRIPT_NAME_KEY to scriptName.name,
            )
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val scriptName = arguments?.getString(ARG_SCRIPT_NAME_KEY)?.let {
            Scripts.Name.valueOf(it)
        } ?: throw NullPointerException("arguments is null")
        val context = requireContext()
        val scripts = Scripts(context)
        val presets = scripts.getPresets(scriptName)
        val presetDescriptions = presets.map { preset ->
            context.getString(preset.descriptionResId)
        }.toTypedArray()
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle(R.string.preset_script)
                .setItems(presetDescriptions) { _, which ->
                    val preset = presets[which]
                    setFragmentResult(
                        RESULT_KEY, bundleOf(
                            ARG_FILENAME_KEY to preset.filename,
                        )
                    )
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}