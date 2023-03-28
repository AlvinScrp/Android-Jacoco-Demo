package com.a.test;

import com.a.report.ReportGenerator;
import com.a.report.ReportGeneratorParams;

public class ReportGeneratorTest {
    public static void main(String[] args) throws Exception {

        String[] args2 = new String[]{
                ReportGeneratorParams.key_ecFiles + "=/Users/canglong/Documents/android_project/fxj2/build/ec/jacoco/fxj_3_20230313-141459-483.ec;/Users/canglong/Documents/android_project/fxj2/build/ec/jacoco/fxj_7_20230313-142301-291.ec",
                ReportGeneratorParams.key_backupDir + "=/Users/canglong/Documents/android_project/fxj-CocoBackup",
                ReportGeneratorParams.key_buildNum + "=3",
                ReportGeneratorParams.key_relativeBuildNum + "=2",
                ReportGeneratorParams.key_reportOutDir +  "=/Users/canglong/Documents/android_project/fxj2/build/report",
        };
        ReportGeneratorParams p = ReportGeneratorParams.createFromArgs(args2);
        ReportGenerator.generate(p);

    }
}
