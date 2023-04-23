package com.a.report;

import java.util.HashMap;
import java.util.Map;

public class ReportGeneratorParams {


    public final static String key_ecFiles = "--ecFiles";// file or dir
    public final static String key_reportOutDir = "--reportOutDir";
    public final static String key_backupDir = "--backupDir";
    public final static String key_buildNum = "--buildNum";
    public final static String key_relativeBuildNum = "--relativebuildNum";
//    public final static String key_gitUsername = "--gitUsername";
//    public final static String key_gitPwd = "--gitPwd";

    private Map<String, String> paramsMap = new HashMap<>();


    public static ReportGeneratorParams createFromArgs(final String[] args) {
        ReportGeneratorParams p = new ReportGeneratorParams();
        for (String arg : args) {
            String[] ss = arg.trim().split("=");
            if (ss != null && ss.length == 2) {
                p.paramsMap.put(ss[0], ss[1]);
            }
        }

        return p;
    }

    public boolean isParamsValid() {
        return getEcFiles() != null && getReportOutDir() != null && getBackupDir() != null && getBuildNum() != null;
    }

    public String getEcFiles() {
        return paramsMap.get(key_ecFiles);
    }

    public String getReportOutDir() {
        return paramsMap.get( key_reportOutDir);
    }

    public String getBackupDir() {
        return paramsMap.get( key_backupDir);
    }

    public String getBuildNum() {
        return paramsMap.get(key_buildNum);
    }

    public String getRelativeBuildNum() {
        return paramsMap.get(key_relativeBuildNum);
    }

//    public String getGitUsername() {
//        return paramsMap.get( key_gitUsername);
//    }
//
//    public String getGitPwd() {
//        return paramsMap.get( key_gitPwd);
//    }


}
