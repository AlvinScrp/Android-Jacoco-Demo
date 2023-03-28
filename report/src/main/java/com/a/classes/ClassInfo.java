package com.a.classes;

import java.util.Set;

public class ClassInfo {

    private String packageName;
    private String className;
    private Set<MethodInfo> methods;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Set<MethodInfo> getMethods() {
        return methods;
    }

    public void setMethods(Set<MethodInfo> methods) {
        this.methods = methods;
    }
}
