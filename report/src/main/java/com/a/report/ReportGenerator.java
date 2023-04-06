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

    Map<String, InputInfo> inputMap;
    private final String reportDir;
    private InputInfo targetInput;
    private ExecFileLoader targetExecLoader = new ExecFileLoader();
    private Map<String, ExecFileLoader> otherExecLoaderMap = new ConcurrentHashMap<>();

    /**
     * Create a new generator based for the given project.
     */
    public ReportGenerator(String targetBuild, Map<String, InputInfo> inputMap, String reportDir) {
        this.title = "CodeCoverageReport";
        this.targetInput = inputMap.get(targetBuild);
        this.inputMap = new HashMap<>(inputMap);
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
            mergeExecutionData();
            IBundleCoverage bundleCoverage = analyzeStructure(targetExecLoader, targetInput.getClassDir(), title);
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
        sourceFileLocator.add(new DirectorySourceFileLocator(new File(targetInput.getSrcDir()), "utf-8", 4));
        visitor.visitBundle(bundleCoverage, sourceFileLocator);
        // Signal end of structure information to allow report to write all
        // information out
        visitor.visitEnd();

    }

    private void loadExecutionData() throws Exception {
        Map<String, InputInfo> inputMap = new HashMap<>(this.inputMap);
        if (inputMap == null || inputMap.isEmpty()) {
            throw new IllegalArgumentException("日志文件不存在 ");
        }
        for (InputInfo input : inputMap.values()) {
            String build = input.getBuild();
            List<String> execPaths = input.getExecFilePaths();
            for (String filePath : execPaths) {
                File file = new File(filePath);
                if (!ExecFileUtil.isExecFile(file) || build == null || build.isEmpty()) {
                    continue;
                }
                System.out.println("ExecFileLoader load:" + filePath);
                if (build.equals(targetInput.getBuild())) {
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
    }

    private void mergeExecutionData() throws Exception {

        Map<String, ExecutionData> targetExecDataMap = extractExecutionDataFromLoader(targetExecLoader);
        Map<MethodKey, MethodProbePosition> targetProbePos = analyzeMethodProbePositions(targetExecLoader, targetInput.getClassDir(), targetInput.getBuild());

        long time = System.currentTimeMillis();
        for (Map.Entry<String, ExecFileLoader> e : otherExecLoaderMap.entrySet()) {
            String build = e.getKey();
            InputInfo input = inputMap.get(build);
            ExecFileLoader loader = e.getValue();
            String classesDir = input.getClassDir();

            System.out.println("---------mergeExecutionData <start> classesDir:" + classesDir);

            Map<MethodKey, MethodProbePosition> probePos = analyzeMethodProbePositions(loader, classesDir, build);
            Set<MethodKey> unChangedKeys = unChangedKeyCompareToTarget(classesDir);
            System.out.println(" [methodProbePos.size:" + probePos.size() + "]");
//            for (ExecutionData data : targetExecLoader.getExecutionDataStore().getContents()) {
//                data.reset();
//            }
            for (Map.Entry<MethodKey, MethodProbePosition> mEntry : probePos.entrySet()) {
                MethodKey key = mEntry.getKey();
                if (!unChangedKeys.contains(key)) {
                    continue;
                }
                MethodProbePosition position = mEntry.getValue();
                MethodProbePosition targetPosition = targetProbePos.get(key);
                ExecutionData data = targetExecDataMap.get(key.getClassName());
                ExecutionData newData = genNewExecData(key, position, targetPosition, data);
                if (newData != null) {
                    targetExecLoader.getExecutionDataStore().put(newData);
                }
            }

            String cost = "" + ((System.currentTimeMillis() - time) / 1000.0) + "s";
            System.out.println("---------mergeExecutionData <end> classesDir:" + classesDir + ", cost:" + cost + "]---------");
            time = System.currentTimeMillis();

        }
    }

    private Set<MethodKey> unChangedKeyCompareToTarget(String classesDir) {

        ReportMethodManager manager = ReportMethodManager.getInstance();
        boolean filterClass = manager.isIncremental();
        Set<String> targetClassNames = manager.getTargetClassNames();
        Map<MethodKey, MethodInfo> map1 = ClassesTool.loadMethodMapWithFilter(classesDir, filterClass, targetClassNames);
        Map<MethodKey, MethodInfo> map2 = ReportMethodManager.getInstance().getTargetMethodMap();

        Set<MethodKey> keys = new HashSet<>();
        for (Map.Entry<MethodKey, MethodInfo> e : map2.entrySet()) {
            MethodKey key = e.getKey();
            MethodInfo other = map1.get(e.getKey());
            MethodInfo method = e.getValue();
            if (other != null && method != null && method.md5 != null && method.md5.equals(other.md5)) {
                keys.add(key);
            }
        }
        System.out.println(" [all method in unchanged class:" + map1.size() + " ,targetMethod.size:" + map2.size() + ", unchanged method:" + keys.size());
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
            boolean[] probes = new boolean[toProbes.length];
            int toEnd = toPos.getEnd();
            for (int i = toPos.getStart(), j = fromPos.getStart(); i <= toEnd; i++, j++) {
                probes[i] = fromProbes[j];
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

    private static Map<MethodKey, MethodProbePosition> analyzeMethodProbePositions(ExecFileLoader loader, String classesDir, String build) throws Exception {
        String bundleName = "analyzeCoverage" + build;
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
        System.out.println("    ---------analyzeStructure classesDir:" + classesDir + " [classFile count:" + count + ", cost:" + cost + "]---------");
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

//    private static final String BUILD_PREFIX = "b";

    public static void generate(ReportGeneratorParams p) throws Exception {

        analysisAndSaveTwoBuildDiff(p);
        Map<String, InputInfo> inputMap = prepareInputInfo(p);
        String reportDir = p.getReportOutDir();
        ReportGenerator generator = new ReportGenerator(p.getBuildNum(), inputMap, reportDir);
        generator.create();
        ExecFileUtil.saveGenerateInfo(p.getEcFiles(), reportDir);
    }

    private static void analysisAndSaveTwoBuildDiff(ReportGeneratorParams p) {
        String relativeBuildNum = p.getRelativeBuildNum();
        String backupDir = p.getBackupDir();
        if (relativeBuildNum != null && relativeBuildNum.length() > 0) {
            String newDirPath = classDirOfBuild(backupDir, p.getBuildNum());
            String oldDirPath = classDirOfBuild(backupDir, relativeBuildNum);
            System.out.println("analysisAndSaveTwoBuildDiff,----------  \n * newDir:" + newDirPath + " \n * oldDir:" + oldDirPath);
            Set<MethodInfo> methods = ClassesTool.diffMethodsOfTwoDir(newDirPath, oldDirPath);
            ReportMethodManager.getInstance().setTarget(methods);
            ReportMethodManager.getInstance().setIncremental(true);
        } else {
            ReportMethodManager.getInstance().setIncremental(false);
        }
    }

    private static Map<String, InputInfo> prepareInputInfo(ReportGeneratorParams p) {
        String targetBuild = p.getBuildNum();
        Map<String, List<String>> realEcFilePaths = ExecFileUtil.extractExecFilePathMap(p.getEcFiles());
        if (!realEcFilePaths.containsKey(targetBuild)) {
            realEcFilePaths.put(targetBuild, new ArrayList<>());
        }
        String backupDir = p.getBackupDir();
        Map<String, InputInfo> inputMap = new HashMap<>();
        for (String build : realEcFilePaths.keySet()) {
            String srcDir = srcDirOfBuild(backupDir, build);
            String classDir = classDirOfBuild(backupDir, build);
            List<String> ecPaths = realEcFilePaths.get(build);
            inputMap.put(build, new InputInfo(build, srcDir, classDir, ecPaths));
        }
        return inputMap;
    }

    public static String classDirOfBuild(String buildBackDir, String buildNum) {
        return buildBackDir + "/" + buildNum + "/build";
    }


    public static String srcDirOfBuild(String buildBackDir, String buildNum) {
        return buildBackDir + "/" + buildNum + "/src/main/java";
    }

    private static String formatDateTime(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        return format.format(time);
    }

}


