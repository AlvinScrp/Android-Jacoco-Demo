package com.a.test;

import com.a.report.ReportGenerator;
import com.a.report.ReportGeneratorParams;

public class ReportGeneratorTest {
    public static void main(String[] args) throws Exception {

        String[] args2 = new String[]{
                ReportGeneratorParams.key_ecFiles + "=/Users/canglong/Downloads/coverage2/log/Android/FXJ/211,/Users/canglong/Downloads/coverage2/log/Android/FXJ/209",
                ReportGeneratorParams.key_backupDir + "=/Users/canglong/Downloads/coverage2/Android/backup/FXJ",
                ReportGeneratorParams.key_buildNum + "=211",
                ReportGeneratorParams.key_relativeBuildNum + "=200",
                ReportGeneratorParams.key_reportOutDir + "=build/report211-209-200-2",
        };
        ReportGeneratorParams p = ReportGeneratorParams.createFromArgs(args2);
        ReportGenerator.generate(p);

    }
}
