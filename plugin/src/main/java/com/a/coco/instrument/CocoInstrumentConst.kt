package com.a.coco.instrument

class CocoInstrumentConst {

    companion object {

        const val ExtensionName = "cocoInstrument"
        const val TaskAllClassSuffix = "CocoInstrumentAllClass"

        val ignoreClasses = arrayOf<String>(
            ".*databinding.*Binding",
            ".*databinding.*BindingImpl",
            ".*.R",
            ".*.R\\$.*",
            ".*BuildConfig.*",
            ".*Manifest.*",
            ".*DataBinderMapperImpl",
            ".*DataBinderMapperImpl\\$.*",
            ".*DataBindingTriggerClass",
            ".*ViewInjector.*",
            ".*ViewBinder.*",
            ".*BuildConfig.*",
            ".*BR",
            ".*Manifest*.*",
            ".*_Factory.*",
            ".*_Provide.*Factory.*"
        )
    }
}