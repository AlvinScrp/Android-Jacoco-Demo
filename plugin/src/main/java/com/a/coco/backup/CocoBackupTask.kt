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


    // ./gradlew dailyDebugCocoBackup  --toDir=/User/xx/Android/backup/FXJ/100
    @Option(option = "toDir", description = "build backup dir")
    @Internal
    var toDir: String = "";

    @TaskAction
    fun taskAction() {
        try {
            println("**** start CocoBackupTask ****")
            val variantName: String = name.removeSuffix(CocoBackUpConst.TaskSuffix)
            println("toDir:${toDir} , variantName:${variantName}")

            val tempDir = File(project.buildDir, "cocoBackupTemp")
            val backupDir = File(toDir)
            println("-------> clear and copy to ${backupDir.absolutePath} , from ${tempDir.absolutePath}    ")

            ensureCleanDir(tempDir)
            copyAllModuleProjectsToDir(variantName, tempDir)
            ensureCleanDir(backupDir)
            FileUtils.copyDirectory(tempDir, backupDir)
        } catch (e: Exception) {
            e.printStackTrace()
            println("back up Exception ${e.message}")
        } finally {

        }
        println("**** end CocoBackupTask ****")
    }

    private fun copyAllModuleProjectsToDir(variantName: String, tempDir: File) {
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
    }

    private fun copyModuleProjectToDir(p: Project, variantName: String, destDir: File) {
        try {
            var second = if (variantName.endsWith("Release")) "release" else "debug"
            val rootDir = p.rootDir
            val sourceDir = File("${p.projectDir}/src/main/java")
            val javaClassDir = File("${p.projectDir}/build/intermediates/javac/${variantName}")
            val javaClassDir2 = File("${p.projectDir}/build/intermediates/javac/${second}")
            val kotlinClassDir = File("${p.projectDir}/build/tmp/kotlin-classes/${variantName}")
            val kotlinClassDir2 = File("${p.projectDir}/build/tmp/kotlin-classes/${second}")
            listOf(
                sourceDir,
                javaClassDir,
                javaClassDir2,
                kotlinClassDir,
                kotlinClassDir2
            ).forEach {
                if (it.exists() && it.isDirectory) {
                    var subPath = File(rootDir, p.name).toPath().relativize(it.toPath()).toString()
                    if (subPath.endsWith("/${second}")) {
                        subPath = subPath.replace("/${second}", "/${variantName}")
                    }
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