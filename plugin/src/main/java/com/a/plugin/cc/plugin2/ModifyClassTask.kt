package com.a.plugin.cc.plugin2

import com.a.plugin.cc.CoverageConfig
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import org.jacoco.core.instr.Instrumenter
import org.jacoco.core.runtime.OfflineInstrumentationAccessGenerator
import java.io.File
import com.google.common.io.Files
import org.objectweb.asm.ClassReader

abstract class ModifyClassesTask : DefaultTask() {

    @get:InputFiles
    abstract val classIntDirectories: ListProperty<Directory>

    @get:OutputFiles
    abstract val output: DirectoryProperty


    @TaskAction
    fun taskAction() {
        val outputDir = output.asFile.get()
        println("****************************************")
        println("outputDir:${outputDir.absolutePath}")

        val instrumenter = Instrumenter(OfflineInstrumentationAccessGenerator())
        classIntDirectories.get().forEach { inDir ->
            println("inDir : ${inDir.asFile.absolutePath}")
            inDir.asFile.walk()
                .filter { it.isFile && it.name.endsWith(".class") }
                .forEach { classFile ->
                    println("classFile:${classFile.absolutePath}")
                    val outputFile = File(
                        classFile.absolutePath.replace(
                            inDir.asFile.absolutePath,
                            outputDir.absolutePath
                        )
                    )
//                    val isInject =
//                        Utils.startWith(outputFile.absolutePath, outputDir.absolutePath + "/com/a")
//
////

                    try {
//                        if (isInject) {
                        classFile.inputStream().readAllBytes().let { bytes ->
                            val classReader = ClassReader(bytes)
                            val className = classReader.className.replace('/', '.') + ".class"
                            val isInject = CoverageConfig.matches(className)

                            Files.createParentDirs(outputFile)
                            if (isInject) {
                                val instrumented =
                                    instrumenter.instrument(bytes, "$className instrument")
                                Files.write(instrumented, outputFile)
                            } else {
                                Files.write(bytes, outputFile)
                            }
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        throw Exception(
                            " Unable to instrument file with Jacoco: " + classFile.absolutePath,
                            e
                        )
                    }

                }
        }

        println("****************************************")
    }
}