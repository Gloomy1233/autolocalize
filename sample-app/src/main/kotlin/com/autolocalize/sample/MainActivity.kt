package com.autolocalize.sample

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.autolocalize.android.AutoLocalize
import com.autolocalize.sample.databinding.ActivityMainBinding
import com.autolocalize.sample.fragments.*
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Main activity with navigation drawer showing different demo screens.
 */
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupNavigation()
        observeLocaleChanges()
        
        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(OverviewFragment())
        }
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
        
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }
    
    private fun setupNavigation() {
        binding.navigationView.setNavigationItemSelectedListener(this)
    }
    
    private fun observeLocaleChanges() {
        lifecycleScope.launch {
            AutoLocalize.observeLocale().collectLatest { locale ->
                supportActionBar?.subtitle = "🌍 ${locale.displayName}"
                // Update nav header locale text
                val navHeader = binding.navigationView.getHeaderView(0)
                navHeader.findViewById<android.widget.TextView>(R.id.textCurrentLocaleNav)?.text = locale.displayName
            }
        }
    }
    
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_overview -> loadFragment(OverviewFragment())
            R.id.nav_messaging -> loadFragment(MessagingFragment())
            R.id.nav_food_delivery -> loadFragment(FoodDeliveryFragment())
            R.id.nav_news -> loadFragment(NewsFragment())
            R.id.nav_ecommerce -> loadFragment(EcommerceFragment())
            R.id.nav_settings -> {
                // Could open settings dialog or fragment
                return true
            }
        }
        
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
    
    private fun loadFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, fragment)
        }
    }
}
