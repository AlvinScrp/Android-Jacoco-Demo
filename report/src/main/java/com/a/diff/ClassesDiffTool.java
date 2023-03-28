package com.a.diff;

import com.a.classes.MethodExtractClassVisitor;
import com.a.classes.MethodInfo;

import org.jacoco.core.internal.data.CRC64;
import org.objectweb.asm.ClassReader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class ClassesDiffTool {

    public static Set<MethodInfo> diffMethodsOfTwoDir(String newDirPath, String oldDirPath) {
        long time = System.currentTimeMillis();
        int compareCount = 0;
        Set<MethodInfo> res = new HashSet<>();

        Set<String> newClassPaths = getAllClassPaths(newDirPath);
        Set<String> oldClassPaths = getAllClassPaths(oldDirPath);

        for (String relaPath : newClassPaths) {
            File newFile = new File(newDirPath, relaPath);

            if (!oldClassPaths.contains(relaPath)) {
                //file add
                Set<MethodInfo> newMethods = getFileMethodInfo(newFile);
                res.addAll(newMethods);
            } else {
                File oldFile = new File(oldDirPath, relaPath);
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
        System.out.println(" diff methods of twoDir, methods.size:" + res.size() + " compareCount:" + compareCount + " cost:" + cost + "s");
        return res;
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


    private static Set<String> getAllClassPaths(String rootDirPath) {
        Set<String> paths = new HashSet<>();
        File rootDir = new File(rootDirPath);
        addClassFilesToList(rootDir.toPath(), rootDir, paths);
        return paths;
    }

    private static void addClassFilesToList(Path rootPath, File dir, Set<String> paths) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    addClassFilesToList(rootPath, file, paths);
                } else if (file.getName().endsWith(".class")) {
                    paths.add(rootPath.relativize(file.toPath()).toString());
                }
            }
        }
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
