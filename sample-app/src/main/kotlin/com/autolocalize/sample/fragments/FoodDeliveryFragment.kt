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
import com.autolocalize.sample.databinding.FragmentFoodDeliveryBinding
import com.autolocalize.sample.databinding.ItemFoodBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

/**
 * Food delivery app demo showing menu items with translated descriptions.
 */
class FoodDeliveryFragment : Fragment() {
    
    private var _binding: FragmentFoodDeliveryBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var foodAdapter: FoodAdapter
    private val foodItems = mutableListOf<FoodItem>()
    
    data class FoodItem(
        val name: String,
        val description: String,
        val price: Double,
        val rating: Double,
        var translatedName: String? = null,
        var translatedDescription: String? = null
    )
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFoodDeliveryBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupLanguagePicker()
        loadFoodItems()
        observeLocaleChanges()
    }
    
    private fun setupRecyclerView() {
        foodAdapter = FoodAdapter()
        binding.recyclerViewFood.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewFood.adapter = foodAdapter
    }
    
    private fun setupLanguagePicker() {
        binding.languagePicker.setOnLanguageChangedListener {
            translateAllFoodItems()
        }
    }
    
    private fun loadFoodItems() {
        val items = listOf(
            FoodItem(
                name = "Margherita Pizza",
                description = "Classic pizza with tomato sauce, mozzarella cheese, and fresh basil. A timeless favorite.",
                price = 12.99,
                rating = 4.5
            ),
            FoodItem(
                name = "Chicken Burger",
                description = "Juicy grilled chicken breast with lettuce, tomato, and special sauce on a toasted bun.",
                price = 9.99,
                rating = 4.7
            ),
            FoodItem(
                name = "Caesar Salad",
                description = "Fresh romaine lettuce with Caesar dressing, parmesan cheese, and croutons.",
                price = 8.99,
                rating = 4.3
            ),
            FoodItem(
                name = "Spaghetti Carbonara",
                description = "Creamy pasta with bacon, eggs, parmesan cheese, and black pepper.",
                price = 14.99,
                rating = 4.8
            ),
            FoodItem(
                name = "Chocolate Cake",
                description = "Rich chocolate cake with chocolate frosting. Perfect for dessert lovers.",
                price = 6.99,
                rating = 4.9
            ),
            FoodItem(
                name = "Sushi Platter",
                description = "Assorted fresh sushi including salmon, tuna, and California rolls.",
                price = 18.99,
                rating = 4.6
            )
        )
        
        foodItems.clear()
        foodItems.addAll(items)
        foodAdapter.notifyDataSetChanged()
        translateAllFoodItems()
    }
    
    private fun translateAllFoodItems() {
        foodItems.forEachIndexed { index, item ->
            translateFoodItem(item, index)
        }
    }
    
    private fun translateFoodItem(item: FoodItem, position: Int) {
        lifecycleScope.launch {
            try {
                val translatedName = AutoLocalize.translate(
                    text = item.name,
                    context = TranslationContext.BACKEND
                )
                val translatedDescription = AutoLocalize.translate(
                    text = item.description,
                    context = TranslationContext.BACKEND
                )
                item.translatedName = translatedName
                item.translatedDescription = translatedDescription
                foodAdapter.notifyItemChanged(position)
            } catch (e: Exception) {
                // Keep original on failure
                item.translatedName = null
                item.translatedDescription = null
            }
        }
    }
    
    private fun observeLocaleChanges() {
        lifecycleScope.launch {
            AutoLocalize.observeLocale().collectLatest {
                translateAllFoodItems()
            }
        }
    }
    
    inner class FoodAdapter : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
            val binding = ItemFoodBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return FoodViewHolder(binding)
        }
        
        override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
            holder.bind(foodItems[position])
        }
        
        override fun getItemCount() = foodItems.size
        
        inner class FoodViewHolder(
            private val binding: ItemFoodBinding
        ) : RecyclerView.ViewHolder(binding.root) {
            
            fun bind(item: FoodItem) {
                binding.textFoodName.text = item.translatedName ?: item.name
                binding.textFoodDescription.text = item.translatedDescription ?: item.description
                binding.textFoodPrice.text = getString(R.string.food_price, item.price)
                binding.textFoodRating.text = getString(R.string.food_rating, item.rating)
                
                binding.buttonAddToCart.setOnClickListener {
                    // Handle add to cart
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

