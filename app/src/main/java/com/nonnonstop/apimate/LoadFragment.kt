package com.nonnonstop.apimate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.nonnonstop.apimate.databinding.LoadFragmentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class LoadFragment : Fragment() {
    private val viewModel: LoadViewModel by activityViewModels()
    private var binding: LoadFragmentBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.htmlUrl.observe(this) { htmlUrl ->
            if (htmlUrl.isNullOrEmpty())
                return@observe
            viewLifecycleOwner.lifecycleScope.launch {
                val context = requireActivity().applicationContext
                viewModel.isRunning.value = true
                try {
                    val scripts = Scripts(context)
                    try {
                        withContext(Dispatchers.IO) {
                            scripts.translate(context, htmlUrl)
                        }
                    } catch (ex: Exception) {
                        Timber.e(ex, "Failed to execute script (translate)")
                        Snackbar.make(
                            requireView(),
                            R.string.translate_failed,
                            Snackbar.LENGTH_LONG
                        ).show()
                        return@launch
                    }
                    try {
                        scripts.view(requireActivity(), htmlUrl)
                    } catch (ex: Exception) {
                        Timber.e(ex, "Failed to execute script (view)")
                        Snackbar.make(
                            requireView(),
                            R.string.view_failed,
                            Snackbar.LENGTH_LONG
                        ).show()
                        return@launch
                    }
                } finally {
                    viewModel.isRunning.value = false
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.load_fragment,
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
}