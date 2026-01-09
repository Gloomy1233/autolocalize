package com.autolocalize.sample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.autolocalize.android.AutoLocalize
import com.autolocalize.core.TranslationContext
import com.autolocalize.sample.R
import com.autolocalize.sample.databinding.FragmentMessagingBinding
import com.autolocalize.sample.databinding.ItemMessageBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Messaging app demo showing real-time message translation.
 */
class MessagingFragment : Fragment() {
    
    private var _binding: FragmentMessagingBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var messagesAdapter: MessagesAdapter
    private val messages = mutableListOf<Message>()
    
    data class Message(
        val text: String,
        val timestamp: Long = System.currentTimeMillis(),
        var translatedText: String? = null
    )
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessagingBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupLanguagePicker()
        setupSendButton()
        loadSampleMessages()
        observeLocaleChanges()
    }
    
    private fun setupRecyclerView() {
        messagesAdapter = MessagesAdapter()
        binding.recyclerViewMessages.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewMessages.adapter = messagesAdapter
    }
    
    private fun setupLanguagePicker() {
        binding.languagePicker.setOnLanguageChangedListener {
            translateAllMessages()
        }
    }
    
    private fun setupSendButton() {
        binding.buttonSend.setOnClickListener {
            val text = binding.editMessage.text?.toString() ?: ""
            if (text.isNotBlank()) {
                sendMessage(text)
                binding.editMessage.text?.clear()
            }
        }
    }
    
    private fun sendMessage(text: String) {
        val message = Message(text)
        messages.add(message)
        messagesAdapter.notifyItemInserted(messages.size - 1)
        binding.recyclerViewMessages.smoothScrollToPosition(messages.size - 1)
        
        // Translate the new message
        translateMessage(message, messages.size - 1)
    }
    
    private fun loadSampleMessages() {
        val sampleMessages = listOf(
            "Hello! How are you today?",
            "The weather is beautiful today!",
            "I'm learning about localization and translation.",
            "This is a sample message that will be translated.",
            "AutoLocalize makes it easy to support multiple languages."
        )
        
        sampleMessages.forEach { text ->
            messages.add(Message(text, System.currentTimeMillis() - (sampleMessages.indexOf(text) * 60000)))
        }
        
        messagesAdapter.notifyDataSetChanged()
        translateAllMessages()
    }
    
    private fun translateAllMessages() {
        messages.forEachIndexed { index, message ->
            translateMessage(message, index)
        }
    }
    
    private fun translateMessage(message: Message, position: Int) {
        lifecycleScope.launch {
            try {
                val translated = AutoLocalize.translate(
                    text = message.text,
                    context = TranslationContext.USER_CONTENT
                )
                message.translatedText = translated
                messagesAdapter.notifyItemChanged(position)
            } catch (e: Exception) {
                // Keep original text on failure
                message.translatedText = null
            }
        }
    }
    
    private fun observeLocaleChanges() {
        lifecycleScope.launch {
            AutoLocalize.observeLocale().collectLatest {
                translateAllMessages()
            }
        }
    }
    
    inner class MessagesAdapter : RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
            val binding = ItemMessageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return MessageViewHolder(binding)
        }
        
        override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
            holder.bind(messages[position])
        }
        
        override fun getItemCount() = messages.size
        
        inner class MessageViewHolder(
            private val binding: ItemMessageBinding
        ) : RecyclerView.ViewHolder(binding.root) {
            
            fun bind(message: Message) {
                // Show translated text if available, otherwise original
                val displayText = message.translatedText ?: message.text
                binding.textMessage.text = displayText
                
                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                binding.textTimestamp.text = timeFormat.format(Date(message.timestamp))
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

