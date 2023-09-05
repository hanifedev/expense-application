package com.kiliccambaz.expenseapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kiliccambaz.expenseapp.databinding.ActivityMainBinding
import com.kiliccambaz.expenseapp.ui.login.LoginFragment

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        if (savedInstanceState == null) {
            val fragment = LoginFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }
    }
}