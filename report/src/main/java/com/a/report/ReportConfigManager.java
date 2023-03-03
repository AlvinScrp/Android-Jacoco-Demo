package com.a.report;

import com.a.jgit.diff.classfiles.ClassMethodInfo;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class ReportConfigManager {

    private static ReportConfigManager _instance = new ReportConfigManager();


    private Set<ClassMethodInfo> diffMethods = new HashSet<>();
    private Set<String> diffClassNames = new HashSet<>();
    private Set<String> diffMethodKeys = new HashSet<>();
    private boolean incremental = false;

    private Set<String> ignoreClassRegexes = new HashSet<>();

    private ReportConfigManager() {
        for (String ignoreClass : ReportConfigConst.ignoreClasses) {
            ignoreClassRegexes.add(ignoreClass);
        }
    }

    public static synchronized ReportConfigManager getInstance() {
        return _instance;
    }

    public void setDiff(Set<ClassMethodInfo> methods) {
        this.diffMethods.clear();
        this.diffClassNames.clear();
        this.diffMethodKeys.clear();
        if (methods != null && !methods.isEmpty()) {
            this.diffMethods.addAll(methods);
            for (ClassMethodInfo method : methods) {
                diffClassNames.add(method.className);
                String methodKey = toMethodKey(method.className, method.methodName, method.desc);
                diffMethodKeys.add(methodKey);
            }
        }
    }

    public void setIncremental(boolean incremental) {
        this.incremental = incremental;
    }

//    public boolean isIncremental() {
//        return incremental;
//    }

    public boolean isMatched(String className, String methodName, String desc) {

        if (!incremental) {
            boolean ignore = isIgnoreClass(className);
            return !ignore;
        }
        if(diffClassNames.contains(className)&&!isIgnoreClass(className)){
            String key = toMethodKey(className, methodName, desc);
            return diffMethodKeys.contains(key);
        }
        return false;
    }

    private boolean isDiffMethod(String className, String methodName, String desc) {
        String key = toMethodKey(className, methodName, desc);
        return diffMethodKeys.contains(key);
    }

    public String toMethodKey(String className, String methodName, String desc) {
        StringBuilder sb = new StringBuilder();
        sb.append(className)
                .append(" ")
                .append(methodName)
                .append(" ")
                .append(desc);
        return sb.toString();
    }

    public boolean isIgnoreClass(String className) {
        for (String regex : ignoreClassRegexes) {
            if (Pattern.matches(regex, className)) {
                return true;
            }
        }
        return false;
    }
}
