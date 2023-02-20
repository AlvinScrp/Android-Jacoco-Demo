package com.a.plugin.cc

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
import java.io.IOException
import java.io.UncheckedIOException
import com.google.common.io.Files

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
                .filter { it.isFile && Utils.endWidth(it.name, ".class") }
                .forEach { classFile ->
                    println("classFile:${classFile.absolutePath}")
                    val outputFile = File(
                        classFile.absolutePath.replace(
                            inDir.asFile.absolutePath,
                            outputDir.absolutePath
                        )
                    )
                    val isInject =
                        Utils.startWith(outputFile.absolutePath, outputDir.absolutePath + "/com/a")
                    try {
                        if (isInject) {
                            classFile.inputStream().buffered().use { inputStream ->
                                val instrumented =
                                    instrumenter.instrument(inputStream, "toInstrument.toString()")
                                Files.createParentDirs(outputFile)
                                Files.write(instrumented, outputFile)
                            }
                        } else {
                            Files.createParentDirs(outputFile)
                            Files.copy(classFile, outputFile)
                        }

                    } catch (e: IOException) {
                        throw UncheckedIOException(
                            "isInject:" + isInject + ",Unable to instrument file with Jacoco: " + classFile.absolutePath,
                            e
                        )
                    }

                }
        }

        println("****************************************")
    }
}