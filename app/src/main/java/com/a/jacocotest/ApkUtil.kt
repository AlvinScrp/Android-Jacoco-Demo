package com.a.jacocotest

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import java.io.File

object ApkUtil {

    fun getApkMD5(context: Context) {
        val app = context.packageManager.getApplicationInfo(context.packageName, 0)
        val apkPath = app.publicSourceDir
     val md5 =    DigestUtils.md5(File(apkPath))
        Log.d("alvin","=================================")
        Log.d("alvin", "apkPath:${apkPath}")
        Log.d("alvin", "md5:${md5}")
    }
}