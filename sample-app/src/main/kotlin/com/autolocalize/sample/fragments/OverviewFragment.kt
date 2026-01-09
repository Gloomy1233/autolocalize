package com.autolocalize.sample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.autolocalize.android.AutoLocalize
import com.autolocalize.core.TranslationContext
import com.autolocalize.sample.R
import com.autolocalize.sample.databinding.FragmentOverviewBinding
import com.autolocalize.views.AutoLocalizeViews
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Overview fragment showing all basic features.
 */
class OverviewFragment : Fragment() {
    
    private var _binding: FragmentOverviewBinding? = null
    private val binding get() = _binding!!
    
    private val backendText = "This is sample backend content that needs runtime translation."
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupLanguagePicker()
        setupResourceStrings()
        setupRuntimeTranslation()
        setupModelStatus()
        observeLocaleChanges()
    }
    
    private fun setupLanguagePicker() {
        binding.languagePicker.setOnLanguageChangedListener {
            updateAllTranslations()
        }
    }
    
    private fun setupResourceStrings() {
        updateResourceStrings()
    }
    
    private fun updateResourceStrings() {
        binding.textPlaceholder1.text = getString(R.string.test_placeholder_simple, 5)
        binding.textPlaceholder2.text = getString(R.string.test_placeholder_string, "John")
        binding.textPlaceholder3.text = getString(
            R.string.test_placeholder_positional, "John", 3, "$50"
        )
        binding.textCurrentLocale.text = getString(
            R.string.test_current_locale,
            AutoLocalize.getLocale().displayName
        )
    }
    
    private fun setupRuntimeTranslation() {
        binding.textBackendOriginal.text = backendText
        binding.buttonTranslate.setOnClickListener {
            val userInput = binding.editUserInput.text?.toString() ?: ""
            if (userInput.isNotBlank()) {
                translateUserInput(userInput)
            }
        }
        translateBackendText()
    }
    
    private fun translateBackendText() {
        binding.textBackendTranslated.text = "🔄 Translating..."
        lifecycleScope.launch {
            try {
                val isReady = AutoLocalize.isTranslatorReady()
                if (!isReady) {
                    binding.textBackendTranslated.text = "⚠️ Models not ready. Tap 'Download Models' first."
                    return@launch
                }
                val translated = AutoLocalize.translate(backendText, TranslationContext.BACKEND)
                if (translated != backendText) {
                    binding.textBackendTranslated.text = "→ $translated"
                } else {
                    binding.textBackendTranslated.text = "⚠️ Translation returned original text."
                }
            } catch (e: Exception) {
                binding.textBackendTranslated.text = "⚠️ Translation failed: ${e.message}"
            }
        }
    }
    
    private fun translateUserInput(text: String) {
        binding.buttonTranslate.isEnabled = false
        binding.textUserTranslated.text = "🔄 Translating..."
        lifecycleScope.launch {
            try {
                val isReady = AutoLocalize.isTranslatorReady()
                if (!isReady) {
                    binding.textUserTranslated.text = "⚠️ Models not ready."
                    binding.buttonTranslate.isEnabled = true
                    return@launch
                }
                val translated = AutoLocalize.translate(text, TranslationContext.USER_CONTENT)
                binding.textUserTranslated.text = "→ $translated"
            } catch (e: Exception) {
                binding.textUserTranslated.text = "⚠️ Translation failed: ${e.message}"
            } finally {
                binding.buttonTranslate.isEnabled = true
            }
        }
    }
    
    private fun setupModelStatus() {
        binding.buttonDownloadModels.setOnClickListener {
            downloadModels()
        }
        checkModelStatus()
    }
    
    private fun checkModelStatus() {
        lifecycleScope.launch {
            val isReady = AutoLocalize.isTranslatorReady()
            updateModelStatusUI(isReady)
        }
    }
    
    private fun downloadModels() {
        binding.progressDownload.visibility = View.VISIBLE
        binding.buttonDownloadModels.isEnabled = false
        binding.textModelStatus.text = getString(R.string.status_downloading)
        lifecycleScope.launch {
            try {
                val result = AutoLocalize.prepareTranslator()
                when (result) {
                    is com.autolocalize.core.PrepareResult.Ready -> {
                        binding.textModelStatus.text = "✅ ${getString(R.string.status_ready)}"
                        binding.textModelStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.success))
                        updateAllTranslations()
                    }
                    is com.autolocalize.core.PrepareResult.Downloading -> {
                        binding.textModelStatus.text = "⏳ ${getString(R.string.status_downloading)}"
                    }
                    is com.autolocalize.core.PrepareResult.Failed -> {
                        binding.textModelStatus.text = "❌ ${getString(R.string.status_error, result.error.message)}"
                        binding.textModelStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.error))
                    }
                }
            } catch (e: Exception) {
                binding.textModelStatus.text = "❌ ${getString(R.string.status_error, e.message)}"
                binding.textModelStatus.setTextColor(ContextCompat.getColor(requireContext(),R.color.error))
            } finally {
                binding.progressDownload.visibility = View.GONE
                binding.buttonDownloadModels.isEnabled = true
            }
        }
    }
    
    private fun updateModelStatusUI(isReady: Boolean) {
        if (isReady) {
            binding.textModelStatus.text = "✅ ${getString(R.string.status_ready)}"
            binding.textModelStatus.setTextColor(ContextCompat.getColor(requireContext(),R.color.success))
        } else {
            binding.textModelStatus.text = "⚠️ ${getString(R.string.status_not_ready)}"
            binding.textModelStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.warning))
        }
    }
    
    private fun observeLocaleChanges() {
        lifecycleScope.launch {
            AutoLocalize.observeLocale().collectLatest {
                updateAllTranslations()
            }
        }
    }
    
    private fun updateAllTranslations() {
        updateResourceStrings()
        translateBackendText()
        checkModelStatus()
        if (AutoLocalize.config.enableViewTreeTranslation) {
            lifecycleScope.launch {
                delay(100)
                AutoLocalizeViews.translateViewTree(binding.root, AutoLocalize.config.viewTreeMode)
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

