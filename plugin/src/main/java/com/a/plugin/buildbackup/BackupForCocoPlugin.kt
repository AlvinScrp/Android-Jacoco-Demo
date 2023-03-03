package com.a.plugin.buildbackup

import com.a.plugin.cc.CoverageExtension
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import org.gradle.api.Plugin
import org.gradle.api.Project

class BackupForCocoPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create("backupForCoco", BackupForCocoExtension::class.java)
        val androidComponentsExtension =
            project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponentsExtension.onVariants { variant ->
            if (variant is ApplicationVariant) {
                project.tasks.register("${variant.name}BackupForCoco", BackupForCocoTask::class.java)
            }
        }

    }
}