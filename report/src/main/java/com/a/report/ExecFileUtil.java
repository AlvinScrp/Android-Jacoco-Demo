package com.a.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExecFileUtil {

    public static Map<String, List<String>> extractExecFilePathMap(String ecFilesText) {
        List<String> realPaths = extractExecFilePaths(ecFilesText);
        Map<String, List<String>> map = new HashMap<>();
        try {
            for (String realPath : realPaths) {
                String build = extractBuildNum(realPath);
                if (build != null && build.length() > 0) {
                    if (map.get(build) == null) {
                        map.put(build, new ArrayList<>());
                    }
                    map.get(build).add(realPath);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public static List<String> extractExecFilePaths(String ecFilesText) {
        List<String> realPaths = new ArrayList<>();
        try {
            String[] ecPathArray = ecFilesText.split(",");
            for (String path : ecPathArray) {
                File file = new File(path);
                if (file.exists()) {
                    addExecFilesToList(file, realPaths);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return realPaths;
    }

    public static String extractBuildNum(String execFilePath) {
        try {
            File file = new File(execFilePath);
            //FXJ-200-Redmi22041211AC-2303161412078.ec
            String[] ss = file.getName().split("-");
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

    public static void saveGenerateInfo(String ecFileText, String reportDir) {
        OutputStream out = null;
        try {
            String text=ecFileText.replace(",","\n");
            File dir = new File(reportDir);
            if (!dir.exists() || !dir.isDirectory()) {
                return;
            }
            File file = new File(reportDir, "report_log_info.text");
            if (!file.exists()) {
                file.createNewFile();
            }
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
