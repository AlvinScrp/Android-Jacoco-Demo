package com.a.jacocotest

import android.util.Log

object TrackUtil {

    @JvmStatic
    fun trackPage(activity: MainActivity){
        Log.d("alvin","trackPage:${activity.javaClass}")
    }
}