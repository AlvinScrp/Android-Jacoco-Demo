package com.a.report;

import java.util.List;

public class InputInfo {

  private   String build;

    /**
     * 路径必须精确到xxx/src/main/java,即包名的上一级
     */
    private   String srcDir;
    /**
     * 路径不用精确，内部会递推穷举
     */
    private  String classDir;
    private  List<String> execFilePaths;

    public InputInfo(String build, String srcDir, String classDir, List<String> execFilePaths) {
        this.build = build;
        this.srcDir = srcDir;
        this.classDir = classDir;
        this.execFilePaths = execFilePaths;
    }

    public String getBuild() {
        return build;
    }

    public String getSrcDir() {
        return srcDir;
    }

    public String getClassDir() {
        return classDir;
    }

    public List<String> getExecFilePaths() {
        return execFilePaths;
    }

    @Override
    public String toString() {
        return "InputInfo{" +
                "build='" + build + '\'' +
                ", srcDir='" + srcDir + '\'' +
                ", classDir='" + classDir + '\'' +
                ", execFilePaths=" + execFilePaths +
                '}';
    }
}
