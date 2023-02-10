package com.a.jgit.ast;


import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class ASTGenerator implements IASTGenerator {
    private String javaText;
    private CompilationUnit compilationUnit;

    public ASTGenerator(String javaText) {
        this.javaText = javaText;
        this.initCompilationUnit();
    }

    /**
     * 获取AST编译单元,首次加载很慢
     */
    private void initCompilationUnit() {
        //  AST编译
        final ASTParser astParser = ASTParser.newParser(8);
        final Map<String, String> options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
        astParser.setCompilerOptions(options);
        astParser.setKind(ASTParser.K_COMPILATION_UNIT);
        astParser.setResolveBindings(true);
        astParser.setBindingsRecovery(true);
        astParser.setStatementsRecovery(true);
        astParser.setSource(javaText.toCharArray());
        compilationUnit = (CompilationUnit) astParser.createAST(null);
    }

    /**
     * 获取java类包名
     *
     * @return
     */
    public String getPackageName() {
        if (compilationUnit == null) {
            return "";
        }
        PackageDeclaration packageDeclaration = compilationUnit.getPackage();
        if (packageDeclaration == null) {
            return "";
        }
        String packageName = packageDeclaration.getName().toString();
        return packageName;
    }

    /**
     * 获取普通类单元
     *
     * @return
     */
    public TypeDeclaration getJavaClass() {
        if (compilationUnit == null) {
            return null;
        }
        TypeDeclaration typeDeclaration = null;
        final List<?> types = compilationUnit.types();
        for (final Object type : types) {
            if (type instanceof TypeDeclaration) {
                typeDeclaration = (TypeDeclaration) type;
                break;
            }
        }
        return typeDeclaration;
    }

    /**
     * 获取java类中所有方法
     *
     * @return 类中所有方法
     */
    @Override
    public List<MethodAdapter> getMethods() {
        List<MethodAdapter> methodAdapters = new ArrayList<>();
        TypeDeclaration typeDec = getJavaClass();
        if (typeDec == null) {
            return methodAdapters;
        }
        MethodDeclaration[] methodDec = typeDec.getMethods();
        for (MethodDeclaration methodDeclaration : methodDec) {
            methodAdapters.add(MethodAdapter.javaMethod(methodDeclaration));
        }
        return methodAdapters;
    }

    /**
     * 获取新增类中的所有方法信息
     *
     * @return
     */
    public List<MethodInfo> getMethodInfoList() {
        List<MethodAdapter> methodAdapters = getMethods();
        List<MethodInfo> methodInfoList = new ArrayList<MethodInfo>();
        for (MethodAdapter methodAdapter : methodAdapters) {
            methodInfoList.add(methodAdapter.toMethodInfo());
        }
        return methodInfoList;
    }

    /**
     * 获取修改类型的类的信息以及其中的所有方法，排除接口类
     *
     * @param methodInfos
     * @param addLines
     * @param delLines
     * @return
     */
    @Override
    public ClassInfo getClassInfo(List<MethodInfo> methodInfos, List<int[]> addLines, List<int[]> delLines) {
        TypeDeclaration typeDec = getJavaClass();
        if (typeDec == null || typeDec.isInterface()) {
            return null;
        }
        ClassInfo classInfo = new ClassInfo();
        classInfo.setClassName(getJavaClass().getName().toString());
        classInfo.setPackages(getPackageName());
        classInfo.setMethodInfos(methodInfos);
        classInfo.setAddLines(addLines);
        classInfo.setDelLines(delLines);
        classInfo.setType("REPLACE");
        return classInfo;
    }

    /**
     * 获取新增类型的类的信息以及其中的所有方法，排除接口类
     *
     * @return
     */
    @Override
    public ClassInfo getClassInfo() {
        TypeDeclaration typeDec = getJavaClass();
        if (typeDec == null || typeDec.isInterface()) {
            return null;
        }
        List<MethodAdapter> methodAdapters = getMethods();
        ClassInfo classInfo = new ClassInfo();
        classInfo.setClassName(getJavaClass().getName().toString());
        classInfo.setPackages(getPackageName());
        classInfo.setType("ADD");
        List<MethodInfo> methodInfoList = new ArrayList<MethodInfo>();
        for (MethodAdapter methodAdapter : methodAdapters) {
            methodInfoList.add(methodAdapter.toMethodInfo());
        }
        classInfo.setMethodInfos(methodInfoList);
        return classInfo;
    }


}
