package com.a.jacocotest

import android.content.Context
import android.util.Log
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


object JacocoHelper {

    /**
     * 生成executionData
     */
    fun generateCoverageFile(context: Context) {
        val dir = context.externalCacheDir?.absolutePath ?: return

        var out: OutputStream? = null
        try {
            var file = File("$dir/aa.ec")
            Log.d("alvin", file.absolutePath)
            if (!file.exists()) {
                file.createNewFile()
            }
            out = FileOutputStream(file, false)
            val agent = Class.forName("org.jacoco.agent.rt.RT").getMethod("getAgent").invoke(null)
            out?.write(
                agent.javaClass.getMethod(
                    "getExecutionData",
                    Boolean::class.javaPrimitiveType
                ).invoke(agent, false) as ByteArray
            )
            Log.i("alvin", "generateCoverageFile write success")
            Toast.makeText(context,"success!!",Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.i("alvin", "generateCoverageFile Exception:$e")
            e.printStackTrace()
        } finally {
            if (out != null) {
                try {
                    out.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}