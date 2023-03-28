package org.jacoco.core.internal.analysis;

import java.util.Objects;

public class MethodKey {
    private String className; // com/a/test/HelloWorld
    private String methodName;
    private String desc; // (Ljava/lang/String;)V

    public MethodKey(String className, String methodName, String desc) {
        this.className = className;
        this.methodName = methodName;
        this.desc = desc;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MethodKey methodKey = (MethodKey) o;

        if (!Objects.equals(className, methodKey.className))
            return false;
        if (!Objects.equals(methodName, methodKey.methodName))
            return false;
        return Objects.equals(desc, methodKey.desc);
    }

    @Override
    public int hashCode() {
        int result = className != null ? className.hashCode() : 0;
        result = 31 * result + (methodName != null ? methodName.hashCode() : 0);
        result = 31 * result + (desc != null ? desc.hashCode() : 0);
        return result;
    }
}
