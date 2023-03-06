package com.a.jgit;

import com.a.jgit.diff.ClassesDiff;
import com.a.jgit.diff.classfiles.ClassMethodInfo;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;

import java.io.File;
import java.util.List;
import java.util.Set;

public class JGitTest {

    public static void main(String[] args)  throws Exception{
        String dir = "/Users/canglong/Documents/github_project/Android-Jacoco-Demo-builds";

        getDiffFileOfTwoBranch(dir, "build-2", "build-1");

        Set<ClassMethodInfo> methods = ClassesDiff.diffMethodsTwoBranch(dir, "build-2", "build-1");
        for (ClassMethodInfo method : methods) {
            System.out.println(method.toString());
        }

    }

   public static void getDiffFileOfTwoBranch(String gitDir , String newBranch,String  oldBranch ) throws Exception {
       Git git = Git.open(new File(gitDir));
       Repository repo = git.getRepository();
       AbstractTreeIterator oldTreeParser =ClassesDiff.prepareTreeParser(repo, "refs/heads/"+oldBranch);
       AbstractTreeIterator newTreeParser =ClassesDiff. prepareTreeParser(repo, "refs/heads/"+newBranch);

        // then the procelain diff-command returns a list of diff entries
        List<DiffEntry> diffs = git.diff().setOldTree(oldTreeParser).setNewTree(newTreeParser).call();
       for (DiffEntry diff : diffs) {
           System.out.println("diff:"+diff);
       }

    }
}
