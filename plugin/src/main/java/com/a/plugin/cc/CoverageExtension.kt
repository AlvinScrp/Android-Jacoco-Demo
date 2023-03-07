package com.a.plugin.cc

open class CoverageExtension {

    var enable: Boolean? = null
    //不支持的variants
    var unSupportVariants :Array<String>? = arrayOf()

    var includes: Array<String>? = arrayOf()
    var excludes: Array<String>? = arrayOf()

    var instrumentType: Int? = null


}