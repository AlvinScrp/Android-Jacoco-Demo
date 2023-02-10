package com.a.jgit.ast;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import kastree.ast.Node;

public class MethodAdapter {

    MethodDeclaration declaration;

    Node.Decl.Func func;

    private MethodAdapter(MethodDeclaration declaration, Node.Decl.Func func) {
        this.declaration = declaration;
        this.func = func;
    }

    public static MethodAdapter javaMethod(MethodDeclaration declaration) {
        return new MethodAdapter(declaration, null);
    }

    public static MethodAdapter ktMethod(Node.Decl.Func func) {
        return new MethodAdapter(null, func);
    }

    public String toKey() {
        if (declaration != null) {
            String md5 = declaration.toString();
            return declaration.getName().toString() + declaration.parameters().toString() + md5;
        } else if (func != null) {
            String md5 = func.getBody() == null ? "" : MD5Encode(func.getBody().toString());
            return func.getName().toString()
                    + func.getParams().toString()
                    + md5
                    + (func.getReceiverType() == null ? "" : func.getReceiverType().toString());
        }
        return null;
    }

    public MethodInfo toMethodInfo() {
        if (declaration != null) {
            return toMethodInfo(declaration);
        } else if (func != null) {
            return toMethodInfo(func);
        }
        return null;
    }

    public static MethodInfo toMethodInfo(MethodDeclaration methodDeclaration) {
        MethodInfo methodInfo = new MethodInfo();
        methodInfo.setMd5(MD5Encode(methodDeclaration.toString()));
        methodInfo.setMethodName(methodDeclaration.getName().toString());
        methodInfo.setParameters(methodDeclaration.parameters().toString());
        return methodInfo;
    }

    public static MethodInfo toMethodInfo(Node.Decl.Func func) {
        MethodInfo methodInfo = new MethodInfo();
        methodInfo.setMd5(func.getBody() == null ? "" : MD5Encode(func.getBody().toString()));
        methodInfo.setMethodName(func.getName());
        methodInfo.setParameters(func.getParams().toString());
        return methodInfo;
    }

    public static String MD5Encode(String s) {
        String MD5String = "";
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");

            MD5String = Base64.getEncoder().encodeToString(md5.digest(s.getBytes("utf-8")));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return MD5String;
    }
}
