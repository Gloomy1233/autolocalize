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
import com.autolocalize.sample.databinding.FragmentNewsBinding
import com.autolocalize.sample.databinding.ItemNewsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * News feed demo showing articles with translated titles and content.
 */
class NewsFragment : Fragment() {
    
    private var _binding: FragmentNewsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var newsAdapter: NewsAdapter
    private val newsArticles = mutableListOf<NewsArticle>()
    
    data class NewsArticle(
        val title: String,
        val content: String,
        val publishDate: Date,
        var translatedTitle: String? = null,
        var translatedContent: String? = null
    )
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupLanguagePicker()
        loadNewsArticles()
        observeLocaleChanges()
    }
    
    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter()
        binding.recyclerViewNews.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewNews.adapter = newsAdapter
    }
    
    private fun setupLanguagePicker() {
        binding.languagePicker.setOnLanguageChangedListener {
            translateAllArticles()
        }
    }
    
    private fun loadNewsArticles() {
        val calendar = Calendar.getInstance()
        val articles = listOf(
            NewsArticle(
                title = "Breaking: New Technology Revolutionizes Mobile Development",
                content = "A new framework has been released that makes it easier than ever to build cross-platform mobile applications. Developers are excited about the possibilities this brings to the industry.",
                publishDate = calendar.apply { add(Calendar.HOUR, -2) }.time
            ),
            NewsArticle(
                title = "Global Climate Summit Reaches Historic Agreement",
                content = "World leaders have come together to sign a landmark agreement on climate change. The deal includes commitments to reduce carbon emissions by 50% by 2030.",
                publishDate = calendar.apply { add(Calendar.HOUR, -5) }.time
            ),
            NewsArticle(
                title = "Sports: Local Team Wins Championship",
                content = "In an exciting match that went into overtime, the local team secured their first championship in over a decade. Fans celebrated throughout the city.",
                publishDate = calendar.apply { add(Calendar.DAY_OF_YEAR, -1) }.time
            ),
            NewsArticle(
                title = "Tech Industry Sees Record Growth",
                content = "The technology sector has reported unprecedented growth this quarter, with many companies seeing profits increase by over 30%. Analysts predict this trend will continue.",
                publishDate = calendar.apply { add(Calendar.DAY_OF_YEAR, -2) }.time
            ),
            NewsArticle(
                title = "Health: New Treatment Shows Promise",
                content = "Medical researchers have announced promising results from clinical trials of a new treatment. Early data suggests significant improvements in patient outcomes.",
                publishDate = calendar.apply { add(Calendar.DAY_OF_YEAR, -3) }.time
            )
        )
        
        newsArticles.clear()
        newsArticles.addAll(articles)
        newsAdapter.notifyDataSetChanged()
        translateAllArticles()
    }
    
    private fun translateAllArticles() {
        newsArticles.forEachIndexed { index, article ->
            translateArticle(article, index)
        }
    }
    
    private fun translateArticle(article: NewsArticle, position: Int) {
        lifecycleScope.launch {
            try {
                val translatedTitle = AutoLocalize.translate(
                    text = article.title,
                    context = TranslationContext.BACKEND
                )
                val translatedContent = AutoLocalize.translate(
                    text = article.content,
                    context = TranslationContext.BACKEND
                )
                article.translatedTitle = translatedTitle
                article.translatedContent = translatedContent
                newsAdapter.notifyItemChanged(position)
            } catch (e: Exception) {
                article.translatedTitle = null
                article.translatedContent = null
            }
        }
    }
    
    private fun observeLocaleChanges() {
        lifecycleScope.launch {
            AutoLocalize.observeLocale().collectLatest {
                translateAllArticles()
            }
        }
    }
    
    inner class NewsAdapter : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
            val binding = ItemNewsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return NewsViewHolder(binding)
        }
        
        override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
            holder.bind(newsArticles[position])
        }
        
        override fun getItemCount() = newsArticles.size
        
        inner class NewsViewHolder(
            private val binding: ItemNewsBinding
        ) : RecyclerView.ViewHolder(binding.root) {
            
            fun bind(article: NewsArticle) {
                binding.textNewsTitle.text = article.translatedTitle ?: article.title
                binding.textNewsContent.text = article.translatedContent ?: article.content
                
                val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
                binding.textNewsDate.text = getString(R.string.news_published, dateFormat.format(article.publishDate))
                
                binding.buttonReadMore.setOnClickListener {
                    // Handle read more
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

