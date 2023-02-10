package com.a.jgit;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class GitAdapter {
    private static final Logger logger = LoggerFactory.getLogger(GitAdapter.class);

    private final static String REF_HEADS = "refs/heads/";
    private final static String MASTER_BRANCH = "master";

    private Git git;

    private Repository repository;





    public GitAdapter(Git git) {
       this.git = git;
       this.repository =git.getRepository();

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


    /**
     * 获取指定分支的指定文件内容
     * @param branchName        分支名称
     * @param javaPath          文件路径
     * @return  java类
     * @throws IOException
     */
    public String getBranchSpecificFileContent(String branchName, String javaPath) throws IOException {
        Ref branch = repository.exactRef( REF_HEADS + branchName);
        ObjectId objId = branch.getObjectId();
        RevWalk walk = new RevWalk(repository);
        RevTree tree = walk.parseTree(objId);
        return  getFileContent(javaPath,tree,walk);
    }

    /**
     * 获取指定分支指定的指定文件内容
     * @param javaPath      件路径
     * @param tree          git RevTree
     * @param walk          git RevWalk
     * @return  java类
     * @throws IOException
     */
    private String getFileContent(String javaPath,RevTree tree,RevWalk walk) throws IOException {
        TreeWalk treeWalk = TreeWalk.forPath(repository, javaPath, tree);
        ObjectId blobId = treeWalk.getObjectId(0);
        ObjectLoader loader = repository.open(blobId);
        byte[] bytes = loader.getBytes();
        walk.dispose();
        return new String(bytes);
    }


}

