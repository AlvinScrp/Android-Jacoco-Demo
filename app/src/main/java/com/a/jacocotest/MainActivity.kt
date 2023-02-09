package com.a.jacocotest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.a.jacocotest.databinding.ActivityMainBinding
import com.a.lib.ComposeActivity
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btn1.setOnClickListener { updateText2(true) }
        binding.btn2.setOnClickListener { updateText2(false) }
        binding.btnStartComposeActivity.setOnClickListener {
            startActivity(Intent(this@MainActivity,ComposeActivity::class.java))
        }
        binding.btnApkMd5.setOnClickListener {
            ApkUtil.getApkMD5(this@MainActivity)
        }
        binding.btnGenJacoco.setOnClickListener { JacocoHelper.generateCoverageFile(this@MainActivity) }


    }



    private fun updateText2(ok: Boolean) {
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