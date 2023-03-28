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

import com.a.classes.MethodInfo;
import com.a.diff.ClassesDiffTool;

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
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

    private final List<String> execFilePaths;
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
    public ReportGenerator(List<String> execFilePaths, String sourceDir, String classesDir, String reportDir) {
        this.title = "CodeCoverageReport";
        this.execFilePaths = execFilePaths;
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
            help.append("参数异常,重新输入。\n");
            help.append("java -jar <jar file path>\n");
            help.append(ReportGeneratorParams.key_ecFiles + "  日志文件或目录，用于生成报告,用英文逗号,隔开\n");
            help.append(ReportGeneratorParams.key_backupDir + "   源码和编译产物git仓库本地目录，用于生成报告\n");
            help.append(ReportGeneratorParams.key_buildNum + "    构建序号，对应到源码和编译产物\n");
            help.append(ReportGeneratorParams.key_relativeBuildNum + "   相比较的构建序号，用于生成增量报告，空则全量\n");
            help.append(ReportGeneratorParams.key_reportOutDir + "   报告输出目录\n");
            help.append(ReportGeneratorParams.key_gitUsername + "   如果在~/.netrc中配置了git账户密码，可以不填\n");
            help.append(ReportGeneratorParams.key_gitPwd + "   如果在~/.netrc中配置了git账户密码，可以不填\n");
            help.append("参数设置方式，可以使用 --<参数名称>=<值> ");
            System.out.println(help);
            return;
        }
        generate(p);
    }

    private static final String BUILD_PREFIX = "b";

    public static void generate(ReportGeneratorParams p) throws Exception {

//        prepareBackupDir(p);
        analysisAndSaveTwoBuildDiff(p);

        List<String> realEcFilePaths = preHandleEcFilePaths(p.getEcFiles());
        String buildNum = p.getBuildNum();
        String buildBackDir = p.getBackupDir() + "/b" + buildNum;
        String sourceDir = buildBackDir + "/src/main/java";
        String classDir = buildBackDir + "/build";
        String reportDir = p.getReportOutDir();

        ReportGenerator generator = new ReportGenerator(realEcFilePaths, sourceDir, classDir, reportDir);
        generator.create();
        saveGenerateInfo(realEcFilePaths, reportDir);
    }

    private static void analysisAndSaveTwoBuildDiff(ReportGeneratorParams p) {

        String relativeBuildNum = p.getRelativeBuildNum();

        if (relativeBuildNum != null && relativeBuildNum.length() > 0) {
            String newDirPath = p.getBackupDir() + "/" + BUILD_PREFIX + p.getBuildNum();
            String oldDirPath = p.getBackupDir() + "/" + BUILD_PREFIX + relativeBuildNum;
            System.out.println("analysisAndSaveTwoBuildDiff,----------  \n * newDir:" + newDirPath + " \n * oldDir:" + oldDirPath);
            Set<MethodInfo> methods = ClassesDiffTool.diffMethodsOfTwoDir(newDirPath, oldDirPath);
            ReportConfigManager.getInstance().setDiff(methods);
            ReportConfigManager.getInstance().setIncremental(true);
        } else {
            ReportConfigManager.getInstance().setIncremental(false);
        }
    }

    private static String formatDateTime(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        return format.format(time);
    }

    private static List<String> preHandleEcFilePaths(String ecFilesText) {
        List<String> realItemPaths = new ArrayList<>();
        try {
            String[] ecPathArray = ecFilesText.split(",");
            for (String path : ecPathArray) {

                File file = new File(path);
                if (!file.exists()) {
                    continue;
                }
                File[] files = file.isDirectory() ? file.listFiles() : new File[]{file};
                for (File itemFile : files) {
                    String itemPath = itemFile.getAbsolutePath();
                    if (isExecFile(itemFile) && !realItemPaths.contains(itemPath)) {
                        realItemPaths.add(itemPath);
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return realItemPaths;
    }

    private static void saveGenerateInfo(List<String> paths, String reportDir) {
        OutputStream out = null;
        try {
            if (paths == null || paths.isEmpty()) {
                return;
            }
            File dir = new File(reportDir);
            if (!dir.exists() || !dir.isDirectory()) {
                return;
            }
            File file = new File(reportDir, "report_log_info.text");
            if (!file.exists()) {
                file.createNewFile();
            }
            StringBuilder sb = new StringBuilder();
            for (String path : paths) {
                sb.append("," + path);
            }
            String text = sb.substring(1);
            out = new FileOutputStream(file);
            out.write(text.getBytes(StandardCharsets.UTF_8));


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

}
