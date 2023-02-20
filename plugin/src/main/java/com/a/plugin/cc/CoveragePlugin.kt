package com.a.plugin.cc

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.MethodInsnNode
import java.util.*
import com.android.build.api.artifact.MultipleArtifact
import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import org.apache.commons.io.FileUtils
import java.io.File

class CoveragePlugin : Plugin<Project> {
    override fun apply(project: Project) {

        AsmTes.test()

        project.extensions.create("coverage", CoverageExtension::class.java)
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)

        androidComponents.onVariants { variant ->
            println("${project.name} ${variant.name}")
            resolvePrivacyLogExtension(project)
            if (variant is ApplicationVariant) {
//                val taskName = variant.name + "AnCoco"
//                val taskProvider =
//                    project.tasks.register(taskName, ModifyClassesTask::class.java)
//
//                val anCocoAllClassDirPath =
//                    project.buildDir.absolutePath + "/intermediates/all_classes_dirs/" + variant.name + "/" + taskName
//                FileUtils.deleteDirectory(File(anCocoAllClassDirPath))
////                variant.artifacts.getOutputPath(type, taskProvider.name)
//                variant.artifacts.use(taskProvider)
//                    .wiredWith(ModifyClassesTask::classIntDirectories, ModifyClassesTask::output)
//                    .toTransform(MultipleArtifact.ALL_CLASSES_DIRS)

                if (variant is ApplicationVariant) {
                    variant.instrumentation.transformClassesWith(
                        CoverageTransform::class.java,
                        InstrumentationScope.PROJECT
                    ) {}
                    variant.instrumentation.setAsmFramesComputationMode(
                        FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS
                    )
                }
            }
        }

    }


    private fun resolvePrivacyLogExtension(project: Project): CoverageExtension {
        val coverageExtension = project.extensions.getByType(CoverageExtension::class.java)

        coverageExtension.let { ext ->
            println(ext)
//            parseLogMethod(ext.logMethod)?.let {
//                CoverageConfig.logMethodNode = it
//            }
            ext.ignorePackages?.forEach {
//                println("ignorePackages:${it}")
                CoverageConfig.ignorePackages.add(it)
            }
        }
        return coverageExtension
    }



}