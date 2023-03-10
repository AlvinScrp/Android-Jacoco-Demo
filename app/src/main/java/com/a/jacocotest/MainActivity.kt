package com.a.jacocotest

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import com.a.jacocotest.databinding.ActivityMainBinding
import com.a.other.OtherActivity
//import com.a.lib.ComposeActivity
import com.a.privacy_sample.PrivacyVisitor
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btn1.setOnClickListener { valueClick.onOkClick() }
        binding.btn2.setOnClickListener { valueClick.onBadClick() }
        binding.btnStartComposeActivity.setOnClickListener {
//            startActivity(Intent(this@MainActivity,ComposeActivity::class.java))
        }
        binding.btnStartOtherActivity.setOnClickListener {
            startActivity(Intent(this@MainActivity, OtherActivity::class.java))
        }

        binding.btnApkMd5.setOnClickListener(listener)
        binding.btnGenJacoco.setOnClickListener { JacocoHelper.generateCoverageFile(this@MainActivity) }
        binding.btnBuildNum.setOnClickListener { getMetaBuildNum() }
        Log.d("alvin", "oncreate")
    }

    override fun onResume() {
        super.onResume()
        Log.d("alvin", "onResume")
    }

    private fun getMetaBuildNum() {
        val context = this
        val appInfo = context.packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA
        );
        val buildNum: String = appInfo.metaData.get("JENKINS_BUILD_NUM")?.toString() ?: "sdsdsd";
        binding.tv.text = "buildNum:${buildNum}"
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

    val listener = OnClickListener {
        ApkUtil.getApkMD5(this@MainActivity)
        PrivacyVisitor.visitPrivacy(this@MainActivity)
    }
    val valueClick = object : OnEventListener {
        override fun onOkClick() {
            updateText(true)
        }

        override fun onBadClick() {
            updateText(false)
        }

    }

    interface OnEventListener {
        fun onOkClick()
        fun onBadClick()
    }

}