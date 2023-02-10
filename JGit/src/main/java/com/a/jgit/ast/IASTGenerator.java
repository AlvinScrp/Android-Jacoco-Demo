package com.a.jgit.ast;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.util.List;

public interface IASTGenerator {

    ClassInfo getClassInfo();

    ClassInfo getClassInfo(List<MethodInfo> methodInfos, List<int[]> addLines, List<int[]> delLines);

    List<MethodAdapter> getMethods();

}
