package com.a.jgit.diff.sourcecode;

import java.util.List;

public class SourceClassInfo {
    /**
     * java文件
     */
    private String classFile;
    /**
     * 类名
     */
    private String className;
    /**
     * 包名
     */
    private String packages;

    /**
     * 类中的方法
     */
    private List<SourceMethodInfo> methodInfos;

    /**
     * 新增的行数
     */
    private List<int[]> addLines;

    /**
     * 删除的行数
     */
    private List<int[]> delLines;

    /**
     * 修改类型
     */
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<int[]> getAddLines() {
        return addLines;
    }

    public void setAddLines(List<int[]> addLines) {
        this.addLines = addLines;
    }

    public List<int[]> getDelLines() {
        return delLines;
    }

    public void setDelLines(List<int[]> delLines) {
        this.delLines = delLines;
    }

    public String getClassFile() {
        return classFile;
    }

    public void setClassFile(String classFile) {
        this.classFile = classFile;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getPackages() {
        return packages;
    }

    public void setPackages(String packages) {
        this.packages = packages;
    }

    public List<SourceMethodInfo> getMethodInfos() {
        return methodInfos;
    }

    public void setMethodInfos(List<SourceMethodInfo> methodInfos) {
        this.methodInfos = methodInfos;
    }

    @Override
    public String toString() {
        return "SourceClassInfo{" +
                "classFile='" + classFile + '\'' +
                ", className='" + className + '\'' +
                ", packages='" + packages + '\'' +
                ", methodInfos=" + methodInfos +
                ", addLines=" + addLines +
                ", delLines=" + delLines +
                ", type='" + type + '\'' +
                '}';
    }
}

