package com.a.plugin.cc

import com.a.plugin.cc.plugin1.CoverageTransform
import com.a.plugin.cc.plugin2.ModifyClassesTask
import com.android.build.api.artifact.MultipleArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import org.gradle.api.Plugin
import org.gradle.api.Project
import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import org.apache.commons.io.FileUtils
import java.io.File

class CoveragePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create("coverage", CoverageExtension::class.java)
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)

        androidComponents.onVariants { variant ->
            CoverageConfig.configByExtension(project)
            if (CoverageConfig.enable) {
                if (variant is ApplicationVariant) {
                    FileUtil.initRecordFilePath("${project.buildDir.absolutePath}/coco_log/record.txt")
                    if (CoverageConfig.is7_0_CustomTask()) {
                        instrumentCustomTask(project, variant)
                    } else {
                        instrumentAGPAsmTask(project, variant)
                    }
                }
            }
        }
    }

    private fun instrumentAGPAsmTask(project: Project, variant: ApplicationVariant) {
        variant.instrumentation.transformClassesWith(
            CoverageTransform::class.java,
            InstrumentationScope.ALL
        ) {}
        variant.instrumentation.setAsmFramesComputationMode(
            FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS
        )

    }

    private fun instrumentCustomTask(project: Project, variant: ApplicationVariant) {

        val taskName = variant.name + "AnCoco"
        val taskProvider =
            project.tasks.register(taskName, ModifyClassesTask::class.java)

        val anCocoAllClassDirPath =
            project.buildDir.absolutePath + "/intermediates/all_classes_dirs/" + variant.name + "/" + taskName
        FileUtils.deleteDirectory(File(anCocoAllClassDirPath))
        variant.artifacts.use(taskProvider)
            .wiredWith(
                ModifyClassesTask::classIntDirectories,
                ModifyClassesTask::output
            )
            .toTransform(MultipleArtifact.ALL_CLASSES_DIRS)
    }

}