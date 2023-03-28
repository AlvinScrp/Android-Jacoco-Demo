package com.a.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ExecFileUtil {

    public static List<String> extractExecFilePaths(String ecFilesText) {
        List<String> realPaths = new ArrayList<>();
//        Map<String, List<String>> map = new HashMap<>();
        try {
            String[] ecPathArray = ecFilesText.split(",");
            for (String path : ecPathArray) {
                File file = new File(path);
                if (file.exists()) {
                    addExecFilesToList(file, realPaths);
                }
            }
//            for (String realPath : realPaths) {
//                String build = extractBuildNum(realPath);
//                if (build != null && build.length() > 0) {
//                    if (map.get(build) == null) {
//                        map.put(build, new ArrayList<>());
//                    }
//                    map.get(build).add(realPath);
//                }
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return realPaths;
    }

    public static String extractBuildNum(String execFilePath) {
        try {
            File file = new File(execFilePath);
            //fxj_200_Redmi22041211AC_230316-1412078.ec
            String[] ss = file.getName().split("_");
            return ss[1];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void addExecFilesToList(File file, List<String> paths) {
        File[] files = file.isDirectory() ? file.listFiles() : new File[]{file};
        if (files != null) {
            for (File itemFile : files) {
                if (itemFile.isDirectory()) {
                    addExecFilesToList(itemFile, paths);
                    continue;
                }
                String path = itemFile.getAbsolutePath();
                if (isExecFile(itemFile) && !paths.contains(path)) {
                    paths.add(path);
                }
            }
        }
    }

    public static void saveGenerateInfo(List<String> paths, String reportDir) {
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

    public static boolean isExecFile(File file) {
        return file.exists()
                && file.isFile()
                && (file.getName().endsWith(".exec") || file.getName().endsWith(".ec"));
    }

}
