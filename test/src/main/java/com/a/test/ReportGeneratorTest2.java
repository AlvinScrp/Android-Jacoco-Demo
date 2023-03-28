/*******************************************************************************
 * Copyright (c) 2009, 2020 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *
 *******************************************************************************/
package com.a.test;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.MultiSourceFileLocator;
import org.jacoco.report.html.HTMLFormatter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * This example creates a HTML report for eclipse like projects based on a
 * single execution data store called jacoco.exec. The report contains no
 * grouping information.
 * <p>
 * The class files under test must be compiled with debug information, otherwise
 * source highlighting will not work.
 */
public class ReportGeneratorTest2 {

    private final String title;

    private final List<String> execFilePaths;
    /**
     * 路径必须精确到xxx/src/main/java,即包名的上一级
     */
    private final String sourceDir;
    /**
     * 路径不用精确，内部会递推穷举
     */
    private final List<String> classesDirectories;
    private final String reportDir;

    private ExecFileLoader execFileLoader;

    /**
     * Create a new generator based for the given project.
     */
    public ReportGeneratorTest2(List<String> execFilePaths, String sourceDir, List<String> classesDirectories, String reportDir) {
        this.title = "CodeCoverageReport";
        this.execFilePaths = execFilePaths;
        this.sourceDir = sourceDir;
        this.classesDirectories = classesDirectories;
        this.reportDir = reportDir;
    }

    /**
     * Create the report.
     *
     * @throws IOException
     */
    public void create() throws Exception {

        try {
            loadExecutionData();
            IBundleCoverage bundleCoverage = analyzeStructure();
            createReport(bundleCoverage);
        } catch (Exception e) {//不中断流程
            e.printStackTrace();
            System.err.println(e.getMessage());
        }

    }

    private void createReport(final IBundleCoverage bundleCoverage)
            throws IOException {

        // Create a concrete report visitor based on some supplied
        // configuration. In this case we use the defaults
        final HTMLFormatter htmlFormatter = new HTMLFormatter();
        final IReportVisitor visitor = htmlFormatter
                .createVisitor(new FileMultiReportOutput(new File(reportDir)));

        // Initialize the report with all of the execution and session
        // information. At this point the report doesn't know about the
        // structure of the report being created
        visitor.visitInfo(execFileLoader.getSessionInfoStore().getInfos(),
                execFileLoader.getExecutionDataStore().getContents());

        // Populate the report structure with the bundle coverage information.
        // Call visitGroup if you need groups in your report.
        MultiSourceFileLocator sourceFileLocator = new MultiSourceFileLocator(4);
        sourceFileLocator.add(new DirectorySourceFileLocator(new File(sourceDir), "utf-8", 4));
        visitor.visitBundle(bundleCoverage, sourceFileLocator);
        // Signal end of structure information to allow report to write all
        // information out
        visitor.visitEnd();

    }

    private void loadExecutionData() throws Exception {
        execFileLoader = new ExecFileLoader();
        ExecFileLoader loader = execFileLoader;

        List<String> filePaths = new ArrayList<>(execFilePaths);

        if (filePaths != null && !filePaths.isEmpty()) {
            for (String filePath : filePaths) {
                File file = new File(filePath);
                if (isExecFile(file)) {
                    System.out.println("ExecFileLoader load:" + filePath);
                    loader.load(file);
                }
            }
        } else {
            throw new IllegalArgumentException("日志文件不存在 ");
        }


    }

    private static boolean isExecFile(File file) {
        return file.exists()
                && file.isFile()
                && (file.getName().endsWith(".exec") || file.getName().endsWith(".ec"));
    }


    private IBundleCoverage analyzeStructure() throws IOException {
        final CoverageBuilder coverageBuilder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(
                execFileLoader.getExecutionDataStore(), coverageBuilder);
        if (classesDirectories != null && !classesDirectories.isEmpty()) {
            for (String classDir : classesDirectories) {
                analyzer.analyzeAll(new File(classDir));
            }
        }
//        int count = analyzer.analyzeAll(new File(classesDir));
//        System.out.println("---------classesDir:" + classesDir + " [count:" + count + "]---------");

        return coverageBuilder.getBundle(title);
    }


    /**
     * Starts the report generation process
     *
     * @param args Arguments to the application. This will be the location of the
     *             eclipse projects that will be used to generate reports for
     * @throws IOException
     */
    public static void main(final String[] args) throws Exception {

        String projectDir = "/Users/canglong/Documents/github_project/Android-Jacoco-Demo";

        List<String> ecFilePaths =new ArrayList<>();
        ecFilePaths.add( projectDir+"/build/ec/aa.ec");

        String sourceDir = projectDir + "/mergereport/src/main/java";

        List<String> classDirs = new ArrayList<>();
//        classDirs.add( projectDir + "/mergereport/build/intermediates/javac/dailyDebug");
//        classDirs.add( projectDir + "/mergereport/build/tmp/kotlin-classes/dailyDebug");
        classDirs.add( projectDir + "/mergereport/build/intermediates/javac/dailyDebug/classes/com/a/jacocotest/MethodModifies.class");

        String reportDir = projectDir + "/build/reportY/";

        ReportGeneratorTest2 generator = new ReportGeneratorTest2(ecFilePaths, sourceDir, classDirs, reportDir);
        generator.create();
    }


    private static String formatDateTime(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyMMdd-HHmmss");
        return format.format(time);
    }

}
