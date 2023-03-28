package com.a.test;

import com.a.classes.MethodInfo;
import com.a.classes.MethodExtractClassVisitor;
import com.a.diff.ClassesDiffTool;

import org.objectweb.asm.ClassReader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Set;


public class MyTest {

    public static void main(String[] args) {
//        HelloWorld.b("ok");
//        testLoadMethodsOfClassFiles();

        testClassesDiff2();


    }

    private static void testClassesDiff2() {
//        Set<MethodInfo> methods1 = ClassesDiff.diffMethodsTwoBranch(
//                "/Users/canglong/Documents/android_project/fxj-CocoBackup",
//                "b200", "b8");


        Set<MethodInfo> methods2 = ClassesDiffTool.diffMethodsOfTwoDir(
                "/Users/canglong/Downloads/coverage/backup/b200",
                "/Users/canglong/Downloads/coverage/backup/b2");

//        Set<MethodInfo> diff = new HashSet<>(methods1);
//        diff.removeAll(methods2);
//        System.out.println(methods1.size() + "," + methods2.size() + " , diff size:" + diff.size());

    }

    private static void testLoadMethodsOfClassFiles() {
        String[] filePaths = new String[]{
                "./test/classes/HelloWorld.class",
                "./test/classes/HelloWorld2.class",
                "./test/classes/HelloWorld3.class"};

        for (String path : filePaths) {
            Set<MethodInfo> methods = ClassesDiffTool.getFileMethodInfo(new File(path));
            for (MethodInfo methodInfo : methods) {
                System.out.println(methodInfo);
            }
            System.out.println("---------");

        }
    }

    public static Set<MethodInfo> getFileMethodInfo(String classFilePath) {


        byte[] newFileContent = readFileToByteArray(classFilePath);
        ClassReader cr = new ClassReader(newFileContent);
        MethodExtractClassVisitor cv = new MethodExtractClassVisitor();
        cr.accept(cv, 0);
        return cv.getMethodSet();

    }

    public static byte[] readFileToByteArray(String filePath) {
        try {
            FileInputStream fis = new FileInputStream(filePath);
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