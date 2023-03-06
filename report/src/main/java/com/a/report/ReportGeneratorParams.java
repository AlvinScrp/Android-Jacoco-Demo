package com.a.report;

import java.util.HashMap;
import java.util.Map;

public class ReportGeneratorParams {


    public final static String key_ecFile = "--ecFile";// file or dir
    public final static String key_reportOutDir = "--reportOutDir";
    public final static String key_backupDir = "--backupDir";
    public final static String key_branch = "--branch";
    public final static String key_relativeBranch = "--relativeBranch";

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
        return getEcFile() != null && getReportOutDir() != null && getBackupDir() != null && getBranch() != null;
    }

    public String getEcFile() {
        return paramsMap.get( key_ecFile);
    }

    public String getReportOutDir() {
        return paramsMap.get( key_reportOutDir);
    }

    public String getBackupDir() {
        return paramsMap.get( key_backupDir);
    }

    public String getBranch() {
        return paramsMap.get( key_branch);
    }

    public String getRelativeBranch() {
        return paramsMap.get( key_relativeBranch);
    }
}
