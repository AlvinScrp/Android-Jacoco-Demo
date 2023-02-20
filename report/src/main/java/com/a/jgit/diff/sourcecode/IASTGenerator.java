package com.a.jgit.diff.sourcecode;

import java.util.List;

public interface IASTGenerator {

    SourceClassInfo getClassInfo();

    SourceClassInfo getClassInfo(List<SourceMethodInfo> methodInfos, List<int[]> addLines, List<int[]> delLines);

    List<SourceMethodAdapter> getMethods();

}
