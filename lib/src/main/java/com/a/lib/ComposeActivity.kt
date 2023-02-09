package com.a.lib

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.tencent.smtt.export.external.TbsCoreSettings
import com.tencent.smtt.sdk.QbSdk

class ComposeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HelloView()
        }
    }
}

@Composable
fun HelloView() {
    var count by remember {
        mutableStateOf(0)
    }
    Column {
        val odd = isOdd(count)
        Text("Hello Compose! ${count}  ${odd}")
        Button(onClick = { count += 1 }) {
            Text(text = "increment")
        }
        Button(onClick = { qbsdkInit() }) {
            Text(text = "腾讯x5sdk初始化")
        }
    }

}

fun qbsdkInit() {
    val map = HashMap<String, Any>(2)
    map[TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER] = true
    map[TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE] = true
    map[TbsCoreSettings.TBS_SETTINGS_USE_PRIVATE_CLASSLOADER] = true
    QbSdk.initTbsSettings(map)
}

fun isOdd(num: Int): String {
    val res = if (num % 2 == 0) {
        "even"
    } else {
        "odd"
    }
    return res
}

@Preview
@Composable
fun preview() {
    HelloView()
}