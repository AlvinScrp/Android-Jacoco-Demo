package com.a.coco.instrument

import com.a.coco.instrument.plugin1.CoverageTransform
import com.a.coco.instrument.plugin2.ModifyClassesTask
import com.android.build.api.artifact.MultipleArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import org.gradle.api.Plugin
import org.gradle.api.Project
import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import org.apache.commons.io.FileUtils
import java.io.File

class CocoInstrumentPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create(CocoInstrumentConst.ExtensionName, CocoInstrumentExtension::class.java)
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)

        androidComponents.onVariants { variant ->
            CocoInstrumentConfig.configByExtension(project)
            println(variant.name)
            if (variant is ApplicationVariant
                && CocoInstrumentConfig.enable
                && CocoInstrumentConfig.isVariantSupport(variant.name)
            ) {
                FileUtil.initRecordFilePath("${project.buildDir.absolutePath}/coco_log/record.txt")
                if (CocoInstrumentConfig.isTransformAsmTask()) {
                    instrumentAGPAsmTask(project, variant)
                } else {
                    instrumentCustomTask(project, variant)
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

        val taskName = variant.name + CocoInstrumentConst.TaskAllClassSuffix
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