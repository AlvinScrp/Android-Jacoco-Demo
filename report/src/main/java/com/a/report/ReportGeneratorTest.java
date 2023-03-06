package com.a.report;

public class ReportGeneratorTest {
    public static void main(String[] args) throws Exception {


        String projectDir = "/Users/canglong/Documents/github_project/Android-Jacoco-Demo";
        String buildProjectDir = "/Users/canglong/Documents/github_project/Android-Jacoco-Demo-builds";

        String[] args2 = new String[]{
                ReportGeneratorParams.key_ecFile + "=" + projectDir + "/build/ec",
                ReportGeneratorParams.key_backupDir + "=" + buildProjectDir,
                ReportGeneratorParams.key_branch + "=b5",
                ReportGeneratorParams.key_relativeBranch + "=b4",
                ReportGeneratorParams.key_reportOutDir + "=" + projectDir + "/build/report4",
        };
        ReportGeneratorParams p = ReportGeneratorParams.createFromArgs(args2);
        ReportGenerator.generate(p);

    }
}
