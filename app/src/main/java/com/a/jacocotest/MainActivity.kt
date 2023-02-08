package com.a.jacocotest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.a.jacocotest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btn1.setOnClickListener { updateText(true) }
        binding.btn2.setOnClickListener { updateText(false) }
        binding.btnGenJacoco.setOnClickListener { JacocoHelper.generateCoverageFile(this@MainActivity) }
    }

    private fun updateText(ok: Boolean) {
        val text = if (ok) {
            getOKText()
        } else {
            getBadCode()
        }
        binding.tv.text = text
    }

    private fun getOKText(): String = "OK!!!!"

    private fun getBadCode(): String {
        return "Bad!"
    }
}