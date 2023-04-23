package com.a.test;

import com.a.diff.ClassesTool;
import com.a.report.ReportGenerator;
import com.a.report.ReportGeneratorParams;

import java.io.File;

public class ReportGeneratorTest {
    public static void main(String[] args) throws Exception {

        String[] args2 = new String[]{
                ReportGeneratorParams.key_ecFiles + "=/Users/canglong/Downloads/coverage2/log/Android/FXJ/251",
                ReportGeneratorParams.key_backupDir + "=/Users/canglong/Downloads/coverage2/Android/backup/FXJ",
                ReportGeneratorParams.key_buildNum + "=252",
                ReportGeneratorParams.key_relativeBuildNum + "=245",
                ReportGeneratorParams.key_reportOutDir + "=build/report252-245",
        };
        ReportGeneratorParams p = ReportGeneratorParams.createFromArgs(args2);
        ReportGenerator.generate(p);
//        ReportGenerator.analysisAndSaveTwoBuildDiff(p);

        String classFile1 = "/Users/canglong/Downloads/coverage2/Android/backup/FXJ/245/build/tmp/kotlin-classes/grayRelease/com/webuy/common/base/adapter/CBaseDiffListAdapter.class";
        String classFile2 = "/Users/canglong/Downloads/coverage2/Android/backup/FXJ/245/build/tmp/kotlin-classes/grayRelease/com/webuy/common/base/adapter/CBaseListAdapter.class";
        String classFile3 = "/Users/canglong/Downloads/coverage2/Android/backup/FXJ/245/build/tmp/kotlin-classes/grayRelease/com/webuy/common/base/adapter/ViewTypeDelegateManager.class";

//        String[] files = {classFile1, classFile2, classFile3};
//        for (String filePath : files) {
//            File file = new File(filePath);
//            System.out.println("<>getFileMethodInfo:" + file.getName());
//            ClassesTool.getFileMethodInfo(file);
//        }

    }
}
