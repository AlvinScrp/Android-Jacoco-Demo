package com.a.report;


public class ReportConfigConst {

    public static String[] ignoreClasses = {
            ".*databinding.*Binding",
            ".*databinding.*BindingImpl",
            ".*.R",
            ".*.R\\$.*",
            ".*BuildConfig.*",
            ".*Manifest.*",
            ".*DataBinderMapperImpl.*",
            ".*DataBindingTriggerClass",
            ".*ViewInjector.*",
            ".*ViewBinder.*",
            ".*BuildConfig.*",
            ".*BR",
            ".*Manifest*.*",
            ".*_Factory.*",
            ".*_Provide.*Factory.*"
    };


}
