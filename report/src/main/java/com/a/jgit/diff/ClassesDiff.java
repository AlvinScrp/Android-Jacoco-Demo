package com.a.jgit.diff;

import com.a.jgit.diff.classfiles.ClassFileMethodInfo;
import com.a.jgit.diff.classfiles.DiffClassVisitor;
import com.a.jgit.diff.sourcecode.ASTGenerator;
import com.a.jgit.diff.sourcecode.IASTGenerator;
import com.a.jgit.diff.sourcecode.KotlinASTGenerator;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.objectweb.asm.ClassReader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ClassesDiff {

    private final static String REF_HEADS = "refs/heads/";


    public static Set<ClassFileMethodInfo> diffMethodsTwoBranch(String gitDir, String newBranch, String oldBranch) {
        try {
            Git git = Git.open(new File(gitDir));
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
            Set<ClassFileMethodInfo> methods = batchPrepareDiffMethod(git, newBranch, oldBranch, df, diffs);
            return methods;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashSet<>();
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

    private static Set<ClassFileMethodInfo> batchPrepareDiffMethod(final Git git, final String branchName, final String oldBranchName, final DiffFormatter df, List<DiffEntry> diffs) {

        Set<ClassFileMethodInfo> total = new HashSet<>();
        for (DiffEntry diffEntry : diffs) {
            Set<ClassFileMethodInfo> methods = prepareDiffMethod(git, branchName, oldBranchName, df, diffEntry);
            if (methods != null) {
                total.addAll(methods);
            }
        }

        return total;
    }

    /**
     * 单个差异文件对比
     *
     * @param git
     * @param newBranchName
     * @param oldBranchName
     * @param df
     * @param diffEntry
     * @return
     */
    private synchronized static Set<ClassFileMethodInfo> prepareDiffMethod(Git git, String newBranchName, String oldBranchName, DiffFormatter df, DiffEntry diffEntry) {

        try {
            String newFilePath = diffEntry.getNewPath();
            DiffEntry.ChangeType changeType = diffEntry.getChangeType();
            if (!newFilePath.endsWith(".class") || changeType == DiffEntry.ChangeType.DELETE) {
                return new HashSet<>();
            }
            Set<ClassFileMethodInfo> newMethods = getBranchFileMethodInfo(git, newBranchName, newFilePath);

            if (changeType == DiffEntry.ChangeType.ADD
                    || changeType == DiffEntry.ChangeType.RENAME) {
                return newMethods;
            } else if (changeType == DiffEntry.ChangeType.MODIFY) {
                String oldFilePath = diffEntry.getOldPath();
                Set<ClassFileMethodInfo> oldMethods = getBranchFileMethodInfo(git, oldBranchName, oldFilePath);

                Set<ClassFileMethodInfo> res = new HashSet<>(newMethods);
                res.removeAll(oldMethods);

                //  获取文件差异位置，从而统计差异的行数，如增加行数，减少行数
                List<int[]> addLines = new ArrayList<int[]>();
                List<int[]> delLines = new ArrayList<int[]>();
                getDiffLines(df, diffEntry, addLines, delLines);
                return res;
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

    public static Set<ClassFileMethodInfo> getBranchFileMethodInfo(Git git, String branchName, String classFilePath) throws IOException {


        byte[] newFileContent = getBranchSpecificFileContent(git, branchName, classFilePath);
        ClassReader cr = new ClassReader(newFileContent);
        DiffClassVisitor cv = new DiffClassVisitor();
        cr.accept(cv, 0);
        return cv.getMethodInfos();

    }


    public static byte[] getBranchSpecificFileContent(Git git, String branchName, String javaPath) throws IOException {
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
    private static byte[] getFileContent(Repository repository, String javaPath, RevTree tree, RevWalk walk) throws IOException {
        TreeWalk treeWalk = TreeWalk.forPath(repository, javaPath, tree);
        ObjectId blobId = treeWalk.getObjectId(0);
        ObjectLoader loader = repository.open(blobId);
        byte[] bytes = loader.getBytes();
        walk.dispose();
        return bytes;
    }

    public static boolean isKtFile(String filePath) {
        return filePath.endsWith(".kt");
    }

    public static boolean isJavaFile(String filePath) {
        return filePath.endsWith(".java");
    }

    public static IASTGenerator createGenerator(String sourceContent, String filePath) {

        return isKtFile(filePath) ? new KotlinASTGenerator(sourceContent, filePath) : new ASTGenerator(sourceContent);

    }
}
