package com.a.plugin.cc

import org.gradle.api.Project
import java.util.regex.Pattern

object CoverageConfig {

    private const val InstrumentType_7_0_transformASM_task = 1
    private const val InstrumentType_7_0_custom_task = 2


    var enable = false
        get() = field

    private var instrumentType = InstrumentType_7_0_transformASM_task

    /**
     * 配置，哪些类需要插桩或者不插桩，根据className匹配，
     * include 默认为全匹配
     * @see com.a.plugin.cc.plugin1.CoverageTransform.isInstrumentable
     *
     */
    private var include: MutableSet<String> = mutableSetOf()

    private var exclude: MutableSet<String> = mutableSetOf()

    fun configByExtension(project: Project) {

        val extension: CoverageExtension? =
            project.extensions.getByType(CoverageExtension::class.java)

//        println("extension:${extension?.instrumentType}")

        extension?.enable?.let { enable = it }
        extension?.instrumentType?.let { instrumentType = it }

        extension?.includes
            ?.takeIf { it.isNotEmpty() }
            ?.let { include.addAll(it) }

        extension?.excludes
            ?.takeIf { it.isNotEmpty() }
            ?.let { exclude.addAll(it) }

        exclude.addAll(ignoreClasses)

    }

    fun matches(className: String): Boolean {
        val include = include.isEmpty() || include.any { Pattern.matches(it, className) }
        var exclude = this.exclude.any { Pattern.matches(it, className) }


        var match = include && !exclude
//        println("$className , isMatch:${match}")
        if(match) {
            FileUtil.record("$className , include,exclude,match:${include},${exclude},${match}\n")
        }
        return match
//        return true
    }

    fun is7_0_CustomTask(): Boolean = instrumentType == InstrumentType_7_0_custom_task

}