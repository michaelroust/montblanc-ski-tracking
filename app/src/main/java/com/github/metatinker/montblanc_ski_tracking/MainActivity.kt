package com.github.metatinker.montblanc_ski_tracking

import android.app.Activity
import android.os.Bundle
import com.github.metatinker.montblanc_ski_tracking.databinding.ActivityMainBinding

class MainActivity : Activity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}