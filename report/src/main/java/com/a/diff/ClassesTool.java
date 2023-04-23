package com.a.diff;

import com.a.classes.MethodExtractClassVisitor;
import com.a.classes.MethodInfo;

import org.jacoco.core.internal.analysis.MethodKey;
import org.jacoco.core.internal.data.CRC64;
import org.objectweb.asm.ClassReader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClassesTool {

    public static Set<MethodInfo> diffMethodsOfTwoDir(String newDirPath, String oldDirPath) {
        long time = System.currentTimeMillis();
        int compareCount = 0;
        Set<MethodInfo> res = new HashSet<>();

        Map<String, String> newClassPaths = getAllClassPaths(newDirPath);
        Map<String, String> oldClassPaths = getAllClassPaths(oldDirPath);

        for (Map.Entry<String, String> e : newClassPaths.entrySet()) {
            String className = e.getKey();
            String newClassPath = e.getValue();

            File newFile = new File(newClassPath);
            if (!oldClassPaths.containsKey(className)) {
                //file add
                Set<MethodInfo> newMethods = getFileMethodInfo(newFile);
                res.addAll(newMethods);
            } else {
                String oldClassPath = oldClassPaths.get(className);
                File oldFile = new File(oldClassPath);
                compareCount++;
                if (!compareFileContent(newFile, oldFile)) {
                    //file modify
                    Set<MethodInfo> newMethods = getFileMethodInfo(newFile);
                    Set<MethodInfo> oldMethods = getFileMethodInfo(oldFile);
                    newMethods.removeAll(oldMethods);
                    res.addAll(newMethods);
                }
            }

        }
        double cost = (System.currentTimeMillis() - time) / 1000.0;
        System.out.println(" diff methods of twoDir, methods.size:" + res.size() + " compareClassCount:" + compareCount + " cost:" + cost + "s");
        return res;
    }

    public static Map<MethodKey, MethodInfo> loadMethodMapWithFilter(String dirPath, boolean filter, Set<String> filterClassNames) {
        Set<MethodInfo> set = loadMethodsWithFilter(dirPath, filter, filterClassNames);
        Map<MethodKey, MethodInfo> map = new HashMap<>();
        for (MethodInfo m : set) {
            MethodKey key = new MethodKey(m.className, m.methodName, m.desc);
            map.put(key, m);
        }
        return map;
    }

    public static Set<MethodInfo> loadMethodsWithFilter(String dirPath, boolean filter, Set<String> filterClassNames) {
        long time = System.currentTimeMillis();
        int classCount = 0;
        Map<String, String> classPaths = getAllClassPaths(dirPath);
        Set<MethodInfo> methods = new HashSet<>();
        for (String className : classPaths.keySet()) {
            try {
                if (filter && filterClassNames.contains(className)) {
                    String classPath = classPaths.get(className);
                    File classFile = new File( classPath);
                    byte[] newFileContent = readFileToByteArray(classFile);
                    MethodExtractClassVisitor cv = new MethodExtractClassVisitor();
                    ClassReader cr = new ClassReader(newFileContent);
                    cr.accept(cv, 0);
                    Set<MethodInfo> set = cv.getMethodSet();
                    methods.addAll(set);
                    classCount++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String cost = "" + ((System.currentTimeMillis() - time) / 1000.0) + "s";
        System.out.println("loadMethodsWithFilter, classPaths.size:" + classPaths.size() + " classCount:" + classCount + " methodCount:" + methods.size() + " cost:" + cost);

        return methods;
    }

    private static boolean compareFileContent(File file1, File file2) {
        try {
            if (file1.length() != file2.length()) {
                return false;
            }
            return CRC64.classId(readFileToByteArray(file1)) == CRC64.classId(readFileToByteArray(file2));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    private static Map<String, String> getAllClassPaths(String rootDirPath) {
        Map<String, String> paths = new HashMap<>();
        File rootDir = new File(rootDirPath);
        addClassFilesToList(rootDir.toPath(), rootDir, paths);
        return paths;
    }

    private static void addClassFilesToList(Path rootPath, File dir, Map<String, String> paths) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    addClassFilesToList(rootPath, file, paths);
                } else if (file.getName().endsWith(".class")) {
                    String className = getClassName(file);
                    paths.put(className, file.getAbsolutePath());
                }
            }
        }
    }

    public static String getClassName(File classFile) {
        byte[] newFileContent = readFileToByteArray(classFile);
        ClassReader cr = new ClassReader(newFileContent);
        return cr.getClassName();

    }

    public static Set<MethodInfo> getFileMethodInfo(File classFile) {
        byte[] newFileContent = readFileToByteArray(classFile);
        ClassReader cr = new ClassReader(newFileContent);
        MethodExtractClassVisitor cv = new MethodExtractClassVisitor();
        cr.accept(cv, 0);
        return cv.getMethodSet();

    }

    public static byte[] readFileToByteArray(File classFile) {
        try {
            FileInputStream fis = new FileInputStream(classFile);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int readBytes;
            while ((readBytes = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, readBytes);
            }
            fis.close();
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
