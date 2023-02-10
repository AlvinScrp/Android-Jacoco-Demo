package com.a.jgit;

import com.a.jgit.ast.ASTGenerator;
import com.a.jgit.ast.ClassInfo;
import com.a.jgit.ast.IASTGenerator;
import com.a.jgit.ast.KotlinASTGenerator;
import com.a.jgit.ast.MethodAdapter;
import com.a.jgit.ast.MethodInfo;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class CodeDiff {

    private final static String REF_HEADS = "refs/heads/";


    public static List<ClassInfo> diffMethodsTwoBranch(Git git, String newBranch, String oldBranch) {
        try {
            Repository repo = git.getRepository();
            AbstractTreeIterator oldTreeParser = prepareTreeParser(repo, REF_HEADS + oldBranch);
            AbstractTreeIterator newTreeParser = prepareTreeParser(repo, REF_HEADS + newBranch);

            //  对比差异
            List<DiffEntry> diffs = git.diff().setOldTree(oldTreeParser).setNewTree(newTreeParser).setShowNameAndStatusOnly(true).call();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DiffFormatter df = new DiffFormatter(out);
            //  设置比较器为忽略空白字符对比（Ignores all whitespace）
            df.setDiffComparator(RawTextComparator.WS_IGNORE_ALL);
            df.setRepository(git.getRepository());
            List<ClassInfo> allClassInfos = batchPrepareDiffMethod(git, newBranch, oldBranch, df, diffs);
            return allClassInfos;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<ClassInfo>();
    }


    private static AbstractTreeIterator prepareTreeParser(Repository repository, String ref) throws IOException {
        // from the commit we can build the tree which allows us to construct the TreeParser
        Ref head = repository.exactRef(ref);
        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commit = walk.parseCommit(head.getObjectId());
            RevTree tree = walk.parseTree(commit.getTree().getId());

            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            try (ObjectReader reader = repository.newObjectReader()) {
                treeParser.reset(reader, tree.getId());
            }

            walk.dispose();

            return treeParser;
        }
    }

    private static List<ClassInfo> batchPrepareDiffMethod(final Git git, final String branchName, final String oldBranchName, final DiffFormatter df, List<DiffEntry> diffs) {

        List<ClassInfo> allClassInfoList = new ArrayList<>();
        for (DiffEntry diffEntry : diffs) {
            ClassInfo classInfo = prepareDiffMethod(git, branchName, oldBranchName, df, diffEntry);
            if (classInfo != null) {
                allClassInfoList.add(classInfo);
            }
        }

        return allClassInfoList;
    }

    /**
     * 单个差异文件对比
     *
     * @param git
     * @param branchName
     * @param oldBranchName
     * @param df
     * @param diffEntry
     * @return
     */
    private synchronized static ClassInfo prepareDiffMethod(Git git, String branchName, String oldBranchName, DiffFormatter df, DiffEntry diffEntry) {
        List<MethodInfo> methodInfoList = new ArrayList<MethodInfo>();
        try {
            String newFilePath = diffEntry.getNewPath();
            //  排除测试类
            if (newFilePath.contains("/src/androidTest/java/")
                    || newFilePath.contains("/src/test/java/")) {
                return null;
            }
            String oldFilePath = diffEntry.getOldPath();
            boolean isNewJava = isJavaFile(newFilePath);
            boolean isNewKotlin = isKtFile(newFilePath);
            DiffEntry.ChangeType changeType = diffEntry.getChangeType();
            //  非java文件 和 删除类型不记录
            if (!(isNewJava || isNewKotlin) || changeType == DiffEntry.ChangeType.DELETE) {
                return null;
            }
            String newClassContent = getBranchSpecificFileContent(git, branchName, newFilePath);
            if (changeType == DiffEntry.ChangeType.ADD
                    || changeType == DiffEntry.ChangeType.RENAME) {
                IASTGenerator generator = isNewKotlin ? new KotlinASTGenerator(newFilePath, newClassContent)
                        : new ASTGenerator(newClassContent);
                ClassInfo classInfo = generator.getClassInfo();
                return classInfo;
            } else if (changeType == DiffEntry.ChangeType.MODIFY) {
                IASTGenerator newAstGenerator = isNewKotlin ? new KotlinASTGenerator(newFilePath, newClassContent)
                        : new ASTGenerator(newClassContent);

                String oldClassContent = getBranchSpecificFileContent(git, oldBranchName, oldFilePath);
                ASTGenerator oldAstGenerator = new ASTGenerator(oldClassContent);
                List<MethodAdapter> newMethods = newAstGenerator.getMethods();
                List<MethodAdapter> oldMethods = oldAstGenerator.getMethods();
                Set<String> oldMethodKeys = new HashSet<>();
                for (MethodAdapter oldMethod : oldMethods) {
                    oldMethodKeys.add(oldMethod.toKey());
                }
                for (final MethodAdapter newMethod : newMethods) {
                    String newKey = newMethod.toKey();
                    if (!oldMethodKeys.contains(newKey)) {
                        MethodInfo methodInfo = newMethod.toMethodInfo();
                        methodInfoList.add(methodInfo);
                    }
                }
                //  获取文件差异位置，从而统计差异的行数，如增加行数，减少行数
                List<int[]> addLines = new ArrayList<int[]>();
                List<int[]> delLines = new ArrayList<int[]>();
                getDiffLines(df, diffEntry, addLines, delLines);
                return newAstGenerator.getClassInfo(methodInfoList, addLines, delLines);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static void getDiffLines(DiffFormatter df, DiffEntry diffEntry, List<int[]> addLines, List<int[]> delLines) throws IOException {
        FileHeader fileHeader = df.toFileHeader(diffEntry);
        EditList editList = fileHeader.toEditList();
        for (Edit edit : editList) {
            if (edit.getLengthA() > 0) {
                delLines.add(new int[]{edit.getBeginA(), edit.getEndA()});
            }
            if (edit.getLengthB() > 0) {
                addLines.add(new int[]{edit.getBeginB(), edit.getEndB()});
            }
        }
    }


    public static String getBranchSpecificFileContent(Git git, String branchName, String javaPath) throws IOException {
        Repository repository = git.getRepository();
        Ref branch = repository.exactRef(REF_HEADS + branchName);
        ObjectId objId = branch.getObjectId();
        RevWalk walk = new RevWalk(repository);
        RevTree tree = walk.parseTree(objId);
        return getFileContent(repository, javaPath, tree, walk);
    }

    /**
     * 获取指定分支指定的指定文件内容
     *
     * @param javaPath 件路径
     * @param tree     git RevTree
     * @param walk     git RevWalk
     * @return java类
     * @throws IOException
     */
    private static String getFileContent(Repository repository, String javaPath, RevTree tree, RevWalk walk) throws IOException {
        TreeWalk treeWalk = TreeWalk.forPath(repository, javaPath, tree);
        ObjectId blobId = treeWalk.getObjectId(0);
        ObjectLoader loader = repository.open(blobId);
        byte[] bytes = loader.getBytes();
        walk.dispose();
        return new String(bytes);
    }

    public static boolean isKtFile(String filePath) {
        return filePath.endsWith(".kt");
    }

    public static boolean isJavaFile(String filePath) {
        return filePath.endsWith(".java");
    }


}
