package com.a.classes;

import java.util.Arrays;
import java.util.Objects;

public class MethodInfo {
    public String className; // com/a/test/HelloWorld
    public String methodName;
    public String desc; // (Ljava/lang/String;)V
    public String signature;
    public String[] exceptions;
    public String md5;

    @Override
    public int hashCode() {
        int result = Objects.hash(className, methodName, desc, signature, md5);
        result = 31 * result + Arrays.hashCode(exceptions);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodInfo that = (MethodInfo) o;
        return Objects.equals(className, that.className) && Objects.equals(methodName, that.methodName) && Objects.equals(desc, that.desc) && Objects.equals(signature, that.signature) && Arrays.equals(exceptions, that.exceptions) && Objects.equals(md5, that.md5);
    }

    @Override
    public String toString() {
        return "MethodInfo{" +
                "className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", desc='" + desc + '\'' +
                ", signature='" + signature + '\'' +
                ", exceptions=" + Arrays.toString(exceptions) +
                ", md5='" + md5 + '\'' +
                '}';
    }
}
