package com.a.jgit.diff.sourcecode;

import com.a.jgit.Utils;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import kastree.ast.Node;

public class SourceMethodAdapter {

    MethodDeclaration declaration;

    Node.Decl.Func func;

    private SourceMethodAdapter(MethodDeclaration declaration, Node.Decl.Func func) {
        this.declaration = declaration;
        this.func = func;
    }

    public static SourceMethodAdapter javaMethod(MethodDeclaration declaration) {
        return new SourceMethodAdapter(declaration, null);
    }

    public static SourceMethodAdapter ktMethod(Node.Decl.Func func) {
        return new SourceMethodAdapter(null, func);
    }

    public String toKey() {
        if (declaration != null) {
            String md5 = declaration.toString();
            return declaration.getName().toString() + declaration.parameters().toString() + md5;
        } else if (func != null) {
            String md5 = func.getBody() == null ? "" : Utils.MD5Encode(func.getBody().toString());
            return func.getName().toString()
                    + func.getParams().toString()
                    + md5
                    + (func.getReceiverType() == null ? "" : func.getReceiverType().toString());
        }
        return null;
    }

    public SourceMethodInfo toMethodInfo() {
        if (declaration != null) {
            return toMethodInfo(declaration);
        } else if (func != null) {
            return toMethodInfo(func);
        }
        return null;
    }

    public static SourceMethodInfo toMethodInfo(MethodDeclaration methodDeclaration) {
        SourceMethodInfo methodInfo = new SourceMethodInfo();
        methodInfo.setMd5(Utils.MD5Encode(methodDeclaration.toString()));
        methodInfo.setMethodName(methodDeclaration.getName().toString());
        methodInfo.setParameters(methodDeclaration.parameters().toString());
        return methodInfo;
    }

    public static SourceMethodInfo toMethodInfo(Node.Decl.Func func) {
        SourceMethodInfo methodInfo = new SourceMethodInfo();
        methodInfo.setMd5(func.getBody() == null ? "" : Utils.MD5Encode(func.getBody().toString()));
        methodInfo.setMethodName(func.getName());
        methodInfo.setParameters(func.getParams().toString());
        return methodInfo;
    }


}
