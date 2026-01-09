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
import com.autolocalize.sample.databinding.FragmentEcommerceBinding
import com.autolocalize.sample.databinding.ItemProductBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * E-commerce demo showing products with translated names, descriptions, and reviews.
 */
class EcommerceFragment : Fragment() {
    
    private var _binding: FragmentEcommerceBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var productAdapter: ProductAdapter
    private val products = mutableListOf<Product>()
    
    data class Product(
        val name: String,
        val description: String,
        val price: Double,
        val reviewCount: Int,
        var translatedName: String? = null,
        var translatedDescription: String? = null
    )
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEcommerceBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupLanguagePicker()
        loadProducts()
        observeLocaleChanges()
    }
    
    private fun setupRecyclerView() {
        productAdapter = ProductAdapter()
        binding.recyclerViewProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewProducts.adapter = productAdapter
    }
    
    private fun setupLanguagePicker() {
        binding.languagePicker.setOnLanguageChangedListener {
            translateAllProducts()
        }
    }
    
    private fun loadProducts() {
        val items = listOf(
            Product(
                name = "Wireless Headphones",
                description = "Premium wireless headphones with noise cancellation and 30-hour battery life. Perfect for music lovers and professionals.",
                price = 199.99,
                reviewCount = 1234
            ),
            Product(
                name = "Smart Watch",
                description = "Feature-rich smartwatch with health tracking, GPS, and water resistance. Stay connected and monitor your fitness.",
                price = 299.99,
                reviewCount = 856
            ),
            Product(
                name = "Laptop Backpack",
                description = "Durable laptop backpack with padded compartments, USB charging port, and water-resistant material. Ideal for students and professionals.",
                price = 79.99,
                reviewCount = 2341
            ),
            Product(
                name = "Wireless Mouse",
                description = "Ergonomic wireless mouse with precision tracking and long battery life. Comfortable for extended use.",
                price = 49.99,
                reviewCount = 567
            ),
            Product(
                name = "USB-C Hub",
                description = "Multi-port USB-C hub with HDMI, USB 3.0, and SD card reader. Expand your laptop's connectivity options.",
                price = 39.99,
                reviewCount = 892
            ),
            Product(
                name = "Mechanical Keyboard",
                description = "RGB backlit mechanical keyboard with customizable keys and premium switches. Perfect for gaming and typing.",
                price = 129.99,
                reviewCount = 1456
            )
        )
        
        products.clear()
        products.addAll(items)
        productAdapter.notifyDataSetChanged()
        translateAllProducts()
    }
    
    private fun translateAllProducts() {
        products.forEachIndexed { index, product ->
            translateProduct(product, index)
        }
    }
    
    private fun translateProduct(product: Product, position: Int) {
        lifecycleScope.launch {
            try {
                val translatedName = AutoLocalize.translate(
                    text = product.name,
                    context = TranslationContext.BACKEND
                )
                val translatedDescription = AutoLocalize.translate(
                    text = product.description,
                    context = TranslationContext.BACKEND
                )
                product.translatedName = translatedName
                product.translatedDescription = translatedDescription
                productAdapter.notifyItemChanged(position)
            } catch (e: Exception) {
                product.translatedName = null
                product.translatedDescription = null
            }
        }
    }
    
    private fun observeLocaleChanges() {
        lifecycleScope.launch {
            AutoLocalize.observeLocale().collectLatest {
                translateAllProducts()
            }
        }
    }
    
    inner class ProductAdapter : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
            val binding = ItemProductBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ProductViewHolder(binding)
        }
        
        override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
            holder.bind(products[position])
        }
        
        override fun getItemCount() = products.size
        
        inner class ProductViewHolder(
            private val binding: ItemProductBinding
        ) : RecyclerView.ViewHolder(binding.root) {
            
            fun bind(product: Product) {
                binding.textProductName.text = product.translatedName ?: product.name
                binding.textProductDescription.text = product.translatedDescription ?: product.description
                binding.textProductPrice.text = "$${String.format("%.2f", product.price)}"
                binding.textProductReviews.text = getString(R.string.ecommerce_reviews, product.reviewCount)
                
                binding.buttonBuyNow.setOnClickListener {
                    // Handle buy now
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

