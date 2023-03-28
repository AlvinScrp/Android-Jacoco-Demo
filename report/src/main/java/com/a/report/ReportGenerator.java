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
import com.a.diff.ClassesTool;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.internal.analysis.ClassCoverageImpl;
import org.jacoco.core.internal.analysis.MethodKey;
import org.jacoco.core.internal.analysis.MethodProbePosition;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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

    private List<String> execPaths;
    /**
     * 路径必须精确到xxx/src/main/java,即包名的上一级
     */
    private final String targetSourceDir;
    /**
     * 路径不用精确，内部会递推穷举
     */
    private final String targetClassesDir;
    private final String reportDir;

//    private ExecFileLoader execFileLoader;

    private String targetBuild;
    private ExecFileLoader targetExecLoader = new ExecFileLoader();
    private Map<String, ExecFileLoader> otherExecLoaderMap = new ConcurrentHashMap<>();

    private GeneratorInfoProvider infoProvider;

    /**
     * Create a new generator based for the given project.
     */
    public ReportGenerator(String targetBuild, List<String> execPaths, String reportDir, GeneratorInfoProvider infoProvider) {
        this.title = "CodeCoverageReport";
        this.targetBuild = targetBuild;
        this.execPaths = new ArrayList<>(execPaths);
        this.targetSourceDir = infoProvider.srcDir(targetBuild);
        this.targetClassesDir = infoProvider.classDir(targetBuild);
        this.reportDir = reportDir;
        this.infoProvider = infoProvider;
    }

    /**
     * Create the report.
     *
     * @throws IOException
     */
    public void create() throws Exception {

        try {
            loadExecutionData();
            mergeExecutionData();
            IBundleCoverage bundleCoverage = analyzeStructure(targetExecLoader, targetClassesDir, title);
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
        visitor.visitInfo(targetExecLoader.getSessionInfoStore().getInfos(),
                targetExecLoader.getExecutionDataStore().getContents());

        // Populate the report structure with the bundle coverage information.
        // Call visitGroup if you need groups in your report.
        MultiSourceFileLocator sourceFileLocator = new MultiSourceFileLocator(4);
        sourceFileLocator.add(new DirectorySourceFileLocator(new File(targetSourceDir), "utf-8", 4));
        visitor.visitBundle(bundleCoverage, sourceFileLocator);
        // Signal end of structure information to allow report to write all
        // information out
        visitor.visitEnd();

    }

    private void loadExecutionData() throws Exception {
        List<String> filePaths = new ArrayList<>(execPaths);
        if (filePaths == null || filePaths.isEmpty()) {
            throw new IllegalArgumentException("日志文件不存在 ");
        }
        for (String filePath : filePaths) {
            File file = new File(filePath);
            String build = ExecFileUtil.extractBuildNum(file.getName());
            if (!ExecFileUtil.isExecFile(file) || build == null || build.isEmpty()) {
                continue;
            }
            System.out.println("ExecFileLoader load:" + filePath);
            if (build.equals(targetBuild)) {
                targetExecLoader.load(file);
            } else {
                ExecFileLoader loader = otherExecLoaderMap.get(build);
                if (loader == null) {
                    loader = new ExecFileLoader();
                    otherExecLoaderMap.put(build, loader);
                }
                loader.load(file);

            }
        }
    }

    private void mergeExecutionData() throws Exception {
        Set<String> targetClassNames = ReportMethodManager.getInstance().getTargetClassNames();
        Map<MethodKey, MethodInfo> targetMethodMap = ReportMethodManager.getInstance().getTargetMethodMap();
        boolean filterClass = ReportMethodManager.getInstance().isIncremental();
        Map<String, ExecutionData> targetExecDataMap = extractExecutionDataFromLoader(targetExecLoader);
        String targetBundleName = "analyzeCoverage" + targetBuild;
        Map<MethodKey, MethodProbePosition> targetMethodProbePos = analyzeMethodProbePositions(targetExecLoader, targetClassesDir, targetBundleName);

        for (Map.Entry<String, ExecFileLoader> e : otherExecLoaderMap.entrySet()) {
            String build = e.getKey();
            ExecFileLoader loader = e.getValue();
            String classesDir = infoProvider.classDir(build);
            String bundleName = "analyzeCoverage" + build;

            Map<MethodKey, MethodInfo> methodMap = ClassesTool.loadMethodMapWithFilter(classesDir, filterClass, targetClassNames);
            Map<MethodKey, MethodProbePosition> methodProbePos = analyzeMethodProbePositions(loader, classesDir, bundleName);
            Set<MethodKey> sameKeys = sameKeysByCheckContent(methodMap, targetMethodMap);

            for (Map.Entry<MethodKey, MethodProbePosition> mEntry : methodProbePos.entrySet()) {
                MethodKey key = mEntry.getKey();
                if (sameKeys.contains(key)) {
                    MethodProbePosition position = mEntry.getValue();
                    MethodProbePosition targetPosition = targetMethodProbePos.get(key);
                    ExecutionData data = targetExecDataMap.get(key.getClassName());
                    ExecutionData newData = genNewExecData(key, position, targetPosition, data);
                    if (newData != null) {
                        targetExecLoader.getExecutionDataStore().put(newData);
                    }
                }
            }
        }
    }

    private static Set<MethodKey> sameKeysByCheckContent(Map<MethodKey, MethodInfo> map1, Map<MethodKey, MethodInfo> map2) {
        Set<MethodKey> keys = new HashSet<>();
        for (Map.Entry<MethodKey, MethodInfo> e : map1.entrySet()) {
            MethodKey key = e.getKey();
            MethodInfo other = map2.get(e.getKey());
            MethodInfo method = e.getValue();
            if (other != null && method != null && method.md5 != null && method.md5.equals(other.md5)) {
                keys.add(key);
            }
        }
        return keys;
    }

    private ExecutionData genNewExecData(MethodKey key, MethodProbePosition fromPos, MethodProbePosition toPos, ExecutionData data) {
        try {
            if (key == null || fromPos == null || toPos == null || data == null) {
                return null;
            }
            boolean[] fromProbes = fromPos.getProbes();
            boolean[] toProbes = toPos.getProbes();
            if (fromProbes == null || toProbes == null) {
                return null;
            }
            int length = toProbes.length;
            boolean[] probes = Arrays.copyOf(toProbes, length);
            int toEnd = toPos.getEnd();
            for (int i = toPos.getStart(), j = fromPos.getStart(); i <= toEnd; i++, j++) {
                probes[i] = probes[i] || fromProbes[j];
            }
            return new ExecutionData(data.getId(), data.getName(), probes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;


    }

    private static Map<String, ExecutionData> extractExecutionDataFromLoader(ExecFileLoader loader) {
        Collection<ExecutionData> contents = loader.getExecutionDataStore().getContents();
        Map<String, ExecutionData> res = new HashMap<>();
        for (ExecutionData data : contents) {
            res.put(data.getName(), data);
        }
        return res;
    }

    private static Map<MethodKey, MethodProbePosition> analyzeMethodProbePositions(ExecFileLoader loader, String classesDir, String bundleName) throws Exception {

        IBundleCoverage bundleCoverage = analyzeStructure(loader, classesDir, bundleName);
        Map<MethodKey, MethodProbePosition> res = new HashMap<>();
        for (IPackageCoverage aPackage : bundleCoverage.getPackages()) {
            Collection<IClassCoverage> classCoverages = aPackage.getClasses();
            for (IClassCoverage classCoverage : classCoverages) {
                if (classCoverage instanceof ClassCoverageImpl) {
                    ClassCoverageImpl cc = (ClassCoverageImpl) classCoverage;
                    Map<MethodKey, MethodProbePosition> map = cc.getMethodProbeMap();
                    res.putAll(map);
                }
            }
        }
        return res;
    }


    private static IBundleCoverage analyzeStructure(ExecFileLoader loader, String classesDir, String bundleName) throws IOException {
        long time = System.currentTimeMillis();
        final CoverageBuilder coverageBuilder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(loader.getExecutionDataStore(), coverageBuilder);
        int count = analyzer.analyzeAll(new File(classesDir));
        String cost = "" + ((System.currentTimeMillis() - time) / 1000.0) + "s";
        System.out.println("---------classesDir:" + classesDir + " [count:" + count + ", cost:" + cost + "]---------");
        return coverageBuilder.getBundle(bundleName);
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
        GeneratorInfoProvider infoProvider = createGeneratorInfoProvider(p.getBackupDir());
        analysisAndSaveTwoBuildDiff(p, infoProvider);
        List<String> realEcFilePaths = ExecFileUtil.extractExecFilePaths(p.getEcFiles());
        String reportDir = p.getReportOutDir();
        ReportGenerator generator = new ReportGenerator(p.getBuildNum(), realEcFilePaths, reportDir, infoProvider);
        generator.create();
        ExecFileUtil.saveGenerateInfo(realEcFilePaths, reportDir);
    }

    private static void analysisAndSaveTwoBuildDiff(ReportGeneratorParams p, GeneratorInfoProvider infoProvider) {
        String relativeBuildNum = p.getRelativeBuildNum();
        if (relativeBuildNum != null && relativeBuildNum.length() > 0) {
            String newDirPath = infoProvider.classDir(p.getBuildNum());
            String oldDirPath = infoProvider.classDir(relativeBuildNum);
            System.out.println("analysisAndSaveTwoBuildDiff,----------  \n * newDir:" + newDirPath + " \n * oldDir:" + oldDirPath);
            Set<MethodInfo> methods = ClassesTool.diffMethodsOfTwoDir(newDirPath, oldDirPath);
            ReportMethodManager.getInstance().setTarget(methods);
            ReportMethodManager.getInstance().setIncremental(true);
        } else {
            ReportMethodManager.getInstance().setIncremental(false);
        }
    }

    private static GeneratorInfoProvider createGeneratorInfoProvider(String buildBackDir) {
        return new GeneratorInfoProvider() {
            @Override
            public String backBuildDir(String buildNum) {
                return buildBackDir + "/" + BUILD_PREFIX + buildNum;
            }

            @Override
            public String classDir(String buildNum) {
                return buildBackDir + "/" + BUILD_PREFIX + buildNum + "/build";
            }

            @Override
            public String srcDir(String buildNum) {
                return buildBackDir + "/" + BUILD_PREFIX + buildNum + "/src/main/java";
            }
        };
    }

    private static String formatDateTime(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        return format.format(time);
    }

    interface GeneratorInfoProvider {
        String backBuildDir(String buildNum);

        String classDir(String buildNum);

        String srcDir(String buildNum);
    }
}


