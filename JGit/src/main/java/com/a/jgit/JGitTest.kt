package com.a.jgit

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevTree
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.treewalk.AbstractTreeIterator
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*


fun main(args: Array<String>) {
    val git = Git.open(File(""))
//    val diff = getDiffBetweenBranches(git, "test1", "main")
//    println(diff)
    getDiffClassOfTwoBranch(git, "test1", "main")

    val classInfoList = CodeDiff.diffMethodsTwoBranch(git, "test1", "main")
    classInfoList.forEach { println(it) }
}

fun getDiffBetweenBranches(git: Git, newBranch: String, oldBranch: String): String {
    val repo = git.repository
    val reader = repo.newObjectReader()
    val branch1Id = repo.resolve("$newBranch^{tree}")
    val branch2Id = repo.resolve("$oldBranch^{tree}")
    val branch1Tree = CanonicalTreeParser()
    branch1Tree.reset(reader, branch1Id)
    val branch2Tree = CanonicalTreeParser()
    branch2Tree.reset(reader, branch2Id)
    val diffs = git.diff().setOldTree(branch1Tree).setNewTree(branch2Tree).call()
    val outputStream = ByteArrayOutputStream()
    val diffFormatter = DiffFormatter(outputStream)
    diffFormatter.setRepository(git.repository)
    for (entry in diffs) {
        diffFormatter.format(entry)
    }
    return String(outputStream.toByteArray())
//    return outputStream.toString(Charset.forName("UTF-8"))
}

fun getDiffClassOfTwoBranch(git: Git, newBranch: String, oldBranch: String){
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