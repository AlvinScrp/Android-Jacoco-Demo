package com.a.coco.backup

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import org.gradle.api.Plugin
import org.gradle.api.Project

class CocoBackupPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create(CocoBackUpConst.ExtensionName, CocoBackupExtension::class.java)
        val androidComponentsExtension =
            project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponentsExtension.onVariants { variant ->
            if (variant is ApplicationVariant) {
                project.tasks.register("${variant.name}${CocoBackUpConst.TaskSuffix}", CocoBackupTask::class.java)
            }
        }
    }
}