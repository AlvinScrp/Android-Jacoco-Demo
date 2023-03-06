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
package com.a.report;

import com.a.jgit.diff.ClassesDiff;
import com.a.jgit.diff.classfiles.ClassMethodInfo;

import org.eclipse.jgit.api.Git;
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
import java.util.Set;

/**
 * This example creates a HTML report for eclipse like projects based on a
 * single execution data store called jacoco.exec. The report contains no
 * grouping information.
 * <p>
 * The class files under test must be compiled with debug information, otherwise
 * source highlighting will not work.
 */
public class ReportGenerator {

    private final String title;

    private final String execFilePath;
    /**
     * 路径必须精确到xxx/src/main/java,即包名的上一级
     */
    private final String sourceDir;
    /**
     * 路径不用精确，内部会递推穷举
     */
    private final String classesDir;
    private final String reportDir;

    private ExecFileLoader execFileLoader;

    /**
     * Create a new generator based for the given project.
     */
    public ReportGenerator(String execFilePath, String sourceDir, String classesDir, String reportDir) {
        this.title = "CodeCoverageReport";
        this.execFilePath = execFilePath;
        this.sourceDir = sourceDir;
        this.classesDir = classesDir;
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
        load(execFileLoader, execFilePath);

    }

    public void load(final ExecFileLoader loader, String dir) throws Exception {
        File file = new File(dir);
        boolean hasExecFile = false;
        if (file.exists()) {
            File[] files = file.isDirectory() ? file.listFiles() : new File[]{file};
            for (File itemFile : files) {
                if (isExecFile(itemFile)) {
                    loader.load(itemFile);
                    hasExecFile = true;
                }
            }
        }
        if (!hasExecFile) {
            throw new IllegalArgumentException("日志文件不存在 " + dir);
        }
    }

    private boolean isExecFile(File file) {
        return file.exists()
                && file.isFile()
                && (file.getName().endsWith(".exec") || file.getName().endsWith(".ec"));
    }


    private IBundleCoverage analyzeStructure() throws IOException {
        final CoverageBuilder coverageBuilder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(
                execFileLoader.getExecutionDataStore(), coverageBuilder);
//        if (classesDirectories != null && !classesDirectories.isEmpty()) {
//            for (File classDir : classesDirectories) {
//                analyzer.analyzeAll(classDir);
//            }
//        }
        int count = analyzer.analyzeAll(new File(classesDir));
        System.out.println("---------classesDir:" + classesDir + " [count:" + count + "]---------");

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

//         ecDir  reportOutDir  backupDir branch  relativeBranch

        ReportGeneratorParams p = ReportGeneratorParams.createFromArgs(args);

        if (!p.isParamsValid()) {
            StringBuilder help = new StringBuilder();
            help.append("参数异常,重新输入。");
            help.append("java -jar <jar file path>\n");
            help.append(ReportGeneratorParams.key_ecFile + "  日志文件或目录，用于生成报告\n");
            help.append(ReportGeneratorParams.key_backupDir + "   源码和编译产物git仓库本地目录，用于生成报告\n");
            help.append(ReportGeneratorParams.key_branch + "    源码和编译产物git仓库分支，程序会将backupDir切换到当前branch\n");
            help.append(ReportGeneratorParams.key_relativeBranch + "   源码和编译产物git仓库相对分支，用于生成增量报告，空则全量\n");
            help.append(ReportGeneratorParams.key_reportOutDir + "   报告输出目录\n");
            help.append("参数设置方式，可以使用 --<参数名称>=<值> ");
            System.out.println(help);
            return;
        }
        generate(p);
    }

    public static void generate(ReportGeneratorParams p) throws Exception {

        String branch = p.getBranch();
        Git git = Git.open(new File(p.getBackupDir()));
        git.pull().call();
        git.checkout().setName(branch).call();

        String relativeBranch = p.getRelativeBranch();
        if (relativeBranch != null && relativeBranch.length() > 0) {
            Set<ClassMethodInfo> methods = ClassesDiff.diffMethodsTwoBranch(p.getBackupDir(), branch, relativeBranch);
            ReportConfigManager.getInstance().setDiff(methods);
            ReportConfigManager.getInstance().setIncremental(true);
        } else {
            ReportConfigManager.getInstance().setIncremental(false);
        }

        String backupDir = p.getBackupDir();
        String sourceDir = backupDir + "/src/main/java";
        String classDir = backupDir + "/build";

        ReportGenerator generator = new ReportGenerator(p.getEcFile(), sourceDir, classDir, p.getReportOutDir());
        generator.create();
    }

}
