package com.a.jgit

import com.a.jgit.diff.ClassesDiff
import com.a.jgit.diff.SourceCodeDiff
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevTree
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.treewalk.AbstractTreeIterator
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import java.io.File
import java.io.IOException
import java.util.*

fun main(args: Array<String>) {
    val dir = "/Users/canglong/Documents/github_project/Android-Jacoco-Demo-builds"

    getDiffFileOfTwoBranch(dir, "build-2", "build-1")

    val classInfoList = SourceCodeDiff.diffMethodsTwoBranch(dir, "build-2", "build-1")
    println("-----------")
    classInfoList.forEach { it.methodInfos.forEach { println(it) } }
    println("-----------")
    val methods = ClassesDiff.diffMethodsTwoBranch(dir, "build-2", "build-1")
    methods.forEach { println(it) }

}

fun getDiffFileOfTwoBranch(gitDir: String, newBranch: String, oldBranch: String) {
    val git = Git.open(File(gitDir))
    val oldTreeParser = prepareTreeParser(git.repository, "refs/heads/${oldBranch}")
    val newTreeParser = prepareTreeParser(git.repository, "refs/heads/${newBranch}")

    // then the procelain diff-command returns a list of diff entries
    val diff = git.diff().setOldTree(oldTreeParser).setNewTree(newTreeParser).call()
    for (entry in diff) {
        println("Entry: $entry")
    }
}

@Throws(IOException::class)
private fun prepareTreeParser(repository: Repository, ref: String): AbstractTreeIterator? {
    // from the commit we can build the tree which allows us to construct the TreeParser
    val head: Ref = repository.exactRef(ref)
    RevWalk(repository).use { walk ->
        val commit: RevCommit = walk.parseCommit(head.getObjectId())
        val tree: RevTree = walk.parseTree(commit.tree.id)
        val treeParser = CanonicalTreeParser()
        repository.newObjectReader().use { reader -> treeParser.reset(reader, tree.id) }
        walk.dispose()
        return treeParser
    }
}