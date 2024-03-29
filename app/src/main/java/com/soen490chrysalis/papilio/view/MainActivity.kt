package com.soen490chrysalis.papilio.view

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.navigation.NavigationBarView
import com.soen490chrysalis.papilio.*
import com.soen490chrysalis.papilio.databinding.ActivityMainBinding
import com.soen490chrysalis.papilio.view.chat.ChatFragment


class MainActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener {
    var bottomNavigationView: NavigationBarView? = null
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Setting the listener to detect when we press one of the button on the navigation bar
        bottomNavigationView = binding.bottomnav
        bottomNavigationView?.setOnItemSelectedListener(this)

        // Get the selected item id from the intent and set the selected item on the bottom navigation bar
        val selectedItemId = intent.getIntExtra("selectedItemId", R.id.home)
        bottomNavigationView?.selectedItemId = selectedItemId
    }

    // Function that loads in a page/fragment on the navigation bar when you tap the respective button
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainerView.id, HomeFragment()).commit()
            R.id.activities -> supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainerView.id, ActivitiesFragment()).commit()
            R.id.chats -> supportFragmentManager.beginTransaction()
                    .replace(binding.fragmentContainerView.id, ChatFragment()).commit()
            R.id.account -> supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainerView.id, AccountMenuFragment()).commit()
        }

        return true
    }
}