package com.nonnonstop.apimate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import com.nonnonstop.apimate.databinding.EditFragmentBinding
import timber.log.Timber

class EditFragment : Fragment() {
    private val viewModel: EditViewModel by activityViewModels()
    private var binding: EditFragmentBinding? = null
    private lateinit var scripts: Scripts

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scripts = Scripts(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.edit_fragment,
            container,
            false
        )
        binding!!.viewModel = viewModel
        binding!!.lifecycleOwner = viewLifecycleOwner
        return binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val scriptName = viewModel.scriptName.value?.let {
            Scripts.Name.valueOf(it)
        } ?: return
        viewModel.script.value = scripts.readScript(scriptName)
        viewModel.save.observe(viewLifecycleOwner) {
            try {
                val script = binding?.editText?.text?.toString() ?: return@observe
                scripts.writeScript(scriptName, script)
                Snackbar.make(view, R.string.save_script_succeeded, Snackbar.LENGTH_SHORT).show()
            } catch (ex: Exception) {
                Timber.e(ex, "Failed to save script")
                Snackbar.make(view, R.string.save_script_failed, Snackbar.LENGTH_LONG).show()
            }
        }
        viewModel.revert.observe(viewLifecycleOwner) {
            try {
                scripts.revertScript(scriptName)
                val script = scripts.readScript(scriptName)
                viewModel.script.value = script
                Snackbar.make(
                    requireView(),
                    R.string.revert_script_succeeded,
                    Snackbar.LENGTH_SHORT
                ).show()
            } catch (ex: Exception) {
                Timber.e(ex, "Failed to revert script")
                Snackbar.make(
                    requireView(),
                    R.string.revert_script_failed,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
        viewModel.preset.observe(viewLifecycleOwner) {
            val dialog = ScriptPresetFragment.newInstance(scriptName)
            childFragmentManager.setFragmentResultListener(
                ScriptPresetFragment.RESULT_KEY,
                viewLifecycleOwner
            ) { _, data ->
                val filename = data.getString(ScriptPresetFragment.ARG_FILENAME_KEY)
                    ?: throw NullPointerException("arg not found")
                val script = scripts.readPreset(filename)
                viewModel.script.value = script
                Snackbar.make(
                    requireView(),
                    R.string.script_preset_succeeded,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            dialog.show(childFragmentManager, "ScriptPresetFragment")
        }
    }
}