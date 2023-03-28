package com.a.test;

import com.a.report.ReportGenerator;
import com.a.report.ReportGeneratorParams;

public class ReportGeneratorTest {
    public static void main(String[] args) throws Exception {

        String[] args2 = new String[]{
                ReportGeneratorParams.key_ecFiles + "=/Users/canglong/Downloads/coverage/log/fxj_Android_200",
                ReportGeneratorParams.key_backupDir + "=/Users/canglong/Downloads/coverage/backup",
                ReportGeneratorParams.key_buildNum + "=200",
                ReportGeneratorParams.key_relativeBuildNum + "=8",
                ReportGeneratorParams.key_reportOutDir + "=build/reportfxj",
        };
        ReportGeneratorParams p = ReportGeneratorParams.createFromArgs(args2);
        ReportGenerator.generate(p);

    }
}
