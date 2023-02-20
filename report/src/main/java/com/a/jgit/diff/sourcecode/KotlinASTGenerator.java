package com.a.jgit.diff.sourcecode;


import kastree.ast.Node;
import kastree.ast.psi.Parser;

import java.util.ArrayList;
import java.util.List;

public class KotlinASTGenerator implements IASTGenerator {
    private Node.File file;
    private String filePath;
    public static final Parser parser = new Parser();
    public KotlinASTGenerator(String kotlinText, String filePath) {
        this.filePath = filePath;
        file = parser.parseFile(kotlinText, false);

    }


    /**
     * 获取kotlin类包名
     * @return
     */
    public String getPackageName() {
        if (file == null) {
            return "";
        }
        StringBuilder convertedListStr = new StringBuilder();
        int index = 0;
        for (String pkg: file.getPkg().getNames()) {
            index ++;
            if (index < file.getPkg().getNames().size()) {
                convertedListStr.append(pkg).append(".");
            }else {
                convertedListStr.append(pkg);
            }

        }
        return convertedListStr.toString();
    }

    /**
     * 获取普通类单元
     * @return
     */
    public String getJavaClass() {
        if (file == null) {
            return null;
        }
        if (file.getDecls().size() > 0) {
            if (file.getDecls().get(0).getClass().toString().equals("class kastree.ast.Node$Decl$Structured")) {
                return ((Node.Decl.Structured)file.getDecls().get(0)).getName();
            }else {
                // 这里可能全部都是方法，没有定义类的概念，所以要处理下
                return (filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf(".")));
            }

        }else {
            return null;
        }
    }


    /**
     * 获取kotlin类中所有方法
     * @return 类中所有方法
     */
    @Override
    public List<SourceMethodAdapter> getMethods() {
        List<Node.Decl.Func> funcs = new ArrayList<Node.Decl.Func>();
        for( Node.Decl decl: file.getDecls()) {
            if (decl.getClass().toString().equals("class kastree.ast.Node$Decl$Structured")) {
                for (Node.Decl decl1 : ((Node.Decl.Structured)decl).getMembers()) {
                    if (decl1.getClass().toString().equals("class kastree.ast.Node$Decl$Func")) {
                        funcs.add((Node.Decl.Func) decl1);
                    }else if (decl1.getClass().toString().equals("class kastree.ast.Node$Decl$Structured")) {
                        for (Node.Decl decl2 : ((Node.Decl.Structured)decl1).getMembers()) {
                            if (decl2.getClass().toString().equals("class kastree.ast.Node$Decl$Func")) {
                                funcs.add((Node.Decl.Func) decl2);
                            }
                        }
                    }
                }
            }else if (decl.getClass().toString().equals("class kastree.ast.Node$Decl$Func")) {
                funcs.add((Node.Decl.Func)decl);
            }else {
                System.out.println(decl.getClass().toString());
            }
        }

        List<SourceMethodAdapter> methodAdapters = new ArrayList<>();
        for (Node.Decl.Func func : funcs) {
            methodAdapters.add(SourceMethodAdapter.ktMethod(func));
        }
        return methodAdapters;
    }



    /**
     * 获取修改类型的类的信息以及其中的所有方法，排除接口类
     * @param methodInfos
     * @param addLines
     * @param delLines
     * @return
     */
    @Override
    public SourceClassInfo getClassInfo(List<SourceMethodInfo> methodInfos, List<int[]> addLines, List<int[]> delLines) {
        if (getJavaClass() == null) {
            return null;
        }
        SourceClassInfo classInfo = new SourceClassInfo();
        classInfo.setClassName(getJavaClass());
        classInfo.setPackages(getPackageName());
        classInfo.setMethodInfos(methodInfos);
        classInfo.setAddLines(addLines);
        classInfo.setDelLines(delLines);
        classInfo.setType("REPLACE");
        return classInfo;
    }

    /**
     * 获取新增类型的类的信息以及其中的所有方法，排除接口类
     * @return
     */
    @Override
    public SourceClassInfo getClassInfo() {
        if (getJavaClass() == null) {
            return null;
        }
        List<SourceMethodAdapter> methodAdapters = getMethods();
        SourceClassInfo classInfo = new SourceClassInfo();
        classInfo.setClassName(getJavaClass());
        classInfo.setPackages(getPackageName());
        classInfo.setType("ADD");
        List<SourceMethodInfo> methodInfoList = new ArrayList<SourceMethodInfo>();
        for (SourceMethodAdapter methodAdapter: methodAdapters) {
            SourceMethodInfo methodInfo = methodAdapter.toMethodInfo();
            methodInfoList.add(methodInfo);
        }
        classInfo.setMethodInfos(methodInfoList);
        return classInfo;
    }

    /**
     * 获取修改中的方法
     * @param methodDeclaration
     * @return
     */




}
