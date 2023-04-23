package com.a.report;

import com.a.classes.MethodInfo;

import org.jacoco.core.internal.analysis.MethodKey;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class ReportMethodManager {

    private static ReportMethodManager _instance = new ReportMethodManager();

    private Map<MethodKey, MethodInfo> targetMethodMap = new HashMap<>();
    private Set<String> targetClassNames = new HashSet<>();
    private boolean incremental = false;

    private Set<String> ignoreClassRegexes = new HashSet<>();

    private ReportMethodManager() {
        for (String ignoreClass : ReportConfigConst.ignoreClasses) {
            ignoreClassRegexes.add(ignoreClass);
        }
    }

    public static synchronized ReportMethodManager getInstance() {
        return _instance;
    }

    public void setTarget(Set<MethodInfo> methods) {
        this.targetMethodMap.clear();
        this.targetClassNames.clear();
        if (methods != null && !methods.isEmpty()) {
            for (MethodInfo method : methods) {
                targetClassNames.add(method.className);
                MethodKey methodKey = new MethodKey(method.className, method.methodName, method.desc);
                targetMethodMap.put(methodKey, method);
            }
        }
    }

    public Map<MethodKey, MethodInfo> getTargetMethodMap() {
        return targetMethodMap;
    }

    public Set<String> getTargetClassNames() {
        return targetClassNames;
    }

    public void setIncremental(boolean incremental) {
        this.incremental = incremental;
    }

    public boolean isIncremental() {
        return incremental;
    }

    public boolean isClassMatched(String className){
        if (!incremental) {
            boolean ignore = isIgnoreClass(className);
            return !ignore;
        }
        return  targetClassNames.contains(className) && !isIgnoreClass(className);
    }

    public boolean isMethodMatched(String className, String methodName, String desc) {
        if (!incremental) {
            boolean ignore = isIgnoreClass(className);
            return !ignore;
        }
        if (targetClassNames.contains(className) && !isIgnoreClass(className)) {
            MethodKey key = new MethodKey(className, methodName, desc);
            return targetMethodMap.containsKey(key);
        }
        return false;
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
