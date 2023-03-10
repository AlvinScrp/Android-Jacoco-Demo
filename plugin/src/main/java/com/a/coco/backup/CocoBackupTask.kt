package com.a.coco.backup

import org.apache.commons.io.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.ByteArrayOutputStream
import java.io.File

abstract class CocoBackupTask : DefaultTask() {


    // ./gradlew dailyDebugCocoBackup  --toDir=~/xx/fxj --buildNum=100
    @Option(option = "toDir", description = "build backup dir")
    @Internal
    var toDir: String = "";

    @Option(option = "buildNum", description = "jenkins build num")
    @Internal
    var buildNum: String = "";


    @TaskAction
    fun taskAction() {
        try {

            println("**** start CocoBackupTask ****")
            println("toDir:${toDir}")
            val variantName: String = name.removeSuffix(CocoBackUpConst.TaskSuffix)
            println("variantName:${variantName}")
            // copy dirs to temp
            val tempDir = File(project.buildDir, "cocoBackupTemp")
            ensureCleanDir(tempDir)

            val extension: CocoBackupExtension? =
                project.extensions.getByType(CocoBackupExtension::class.java)
            val excludeModules = mutableSetOf<String>().apply {
                addAll(extension?.excludeModules ?: emptyArray())
            }

            // copy dirs to temp
            val moduleProjects = project.rootProject.subprojects
            moduleProjects.forEach {
                if (!excludeModules.contains(it.name)) {
                    copyModuleProjectToDir(it, variantName, tempDir)
                }
            }


            if (buildNum.isNullOrEmpty()) {
                throw  IllegalArgumentException("buildNum:${buildNum} 异常")
            }
            if (toDir.isNullOrEmpty()) {
                throw  IllegalArgumentException("toDir:${toDir} 异常")
            }

            val backupDir = File(toDir)
            val branchName = "b${buildNum}"
            // git create banch
            gitCreateBranch(branchName, backupDir)

            //clear backupDir and copy temp to builds dir
            println("-------> clear ${backupDir.absolutePath} , and copy from ${tempDir.absolutePath}    ")
            FileUtils.copyDirectory(tempDir, backupDir)

            //git commit  git push
            println("-------> git commit and push to $branchName")
            gitCommit(branchName, backupDir)

            //delete temp
//            ensureCleanDir(tempDir)

        } catch (e: Exception) {
            e.printStackTrace()
            println("back up Exception ${e.message}")
        } finally {

        }

        println("**** end CocoBackupTask ****")


    }

    private fun gitCreateBranch(branchName: String, backupDir: File) {

        println("-------> git pull")
        try{
            ExecCmd.execute(
                project, "git",
                listOf("pull"),
                backupDir
            )
        }catch (e:Exception){
            e.printStackTrace()
        }

        val byteArrayOutputStream = ByteArrayOutputStream(1024)
        ExecCmd.execute(
            project, "git",
            listOf("branch", "-r"),
            backupDir,
            byteArrayOutputStream
        )
        val out = String(byteArrayOutputStream.toByteArray(), Charsets.UTF_8)
        var mainBranch = listOf("master", "main").firstOrNull { out.contains("origin/$it") }
        if (mainBranch.isNullOrEmpty()) {
            throw IllegalArgumentException("远程主分支不存在")
        }
        println("mainBranch:${mainBranch}")

        ExecCmd.execute(
            project, "git",
            listOf("checkout", "$mainBranch"),
            backupDir
        )

//        println("-------> git checkout -b $branchName origin/master")
        println("-------> git branch $branchName ")
        ExecCmd.execute(
            project, "git",
            listOf("checkout", "-b", "$branchName"),
            backupDir
        )

        println("-------> git push origin $branchName")
        ExecCmd.execute(
            project, "git",
            listOf("push", "origin", "$branchName"),
            backupDir
        )

        ExecCmd.execute(
            project, "git",
            listOf("push", "--set-upstream", "origin", "$branchName"),
            backupDir
        )
    }

    private fun gitCommit(branchName: String, backupDir: File) {

        println("-------> git add .")
        ExecCmd.execute(
            project, "git",
            listOf("add", "."),
            backupDir
        )

        val byteArrayOutputStream = ByteArrayOutputStream(1024)
        val out = String(byteArrayOutputStream.toByteArray(), Charsets.UTF_8)
        println("-------> git commit -m")
        ExecCmd.execute(
            project, "git",
            listOf("commit", "-m", "'commit:${branchName}'"),
            backupDir,
            byteArrayOutputStream
        )


        println("-------> git push")
        ExecCmd.execute(
            project, "git",
            listOf("push", "--set-upstream", "origin", "$branchName"),
            backupDir
        )


//        project.exec { spec ->
//            spec.executable = "git"
//            spec.args = listOf("add .")
//            spec.workingDir = backupDir
//        }
//
//        project.exec { spec ->
//            spec.executable = "git"
//            spec.args = listOf("commit -m 'commit:${branchName}'")
//            spec.workingDir = backupDir
//        }
//
//        project.exec { spec ->
//            spec.executable = "git"
//            spec.args = listOf("push origin $branchName")
//            spec.workingDir = backupDir
//        }
    }


    private fun copyModuleProjectToDir(p: Project, variantName: String, destDir: File) {
        try {
            val rootDir = p.rootDir
            val sourceDir = File("${p.projectDir}/src/main/java")
            val javaClassDir = File("${p.projectDir}/build/intermediates/javac/${variantName}")
            val kotlinClassDir = File("${p.projectDir}/build/tmp/kotlin-classes/${variantName}")
            listOf(sourceDir, javaClassDir, kotlinClassDir).forEach {
                if (it.exists() && it.isDirectory) {
                    val subPath = File(rootDir, p.name).toPath().relativize(it.toPath()).toString()
                    val subDir = File(destDir, subPath)
//                    ensureCleanDir(subDir)
                    FileUtils.copyDirectory(it, subDir)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun ensureCleanDir(dir: File) {
        if (dir.exists()) {
            if (dir.isDirectory) {
                FileUtils.cleanDirectory(dir)
            } else if (dir.isFile) {
                FileUtils.forceDelete(dir)
            }
        }
        if (!dir.exists()) {
            dir.mkdirs()
        }
    }
}