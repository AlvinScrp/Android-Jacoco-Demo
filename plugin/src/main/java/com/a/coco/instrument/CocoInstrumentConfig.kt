package com.a.coco.instrument

import org.gradle.api.Project
import java.util.regex.Pattern

object CocoInstrumentConfig {

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

    private var unSupportVariants: MutableSet<String> = mutableSetOf()

    fun configByExtension(project: Project) {

        val extension: CocoInstrumentExtension? =
            project.extensions.getByType(CocoInstrumentExtension::class.java)

//        println("extension:${extension?.instrumentType}")

        extension?.enable?.let { enable = it }
        extension?.unSupportVariants
            ?.takeIf { it.isNotEmpty() }
            ?.forEach { unSupportVariants.add(it.toLowerCase()) }

        extension?.instrumentType?.let { instrumentType = it }

        extension?.includes
            ?.takeIf { it.isNotEmpty() }
            ?.let { include.addAll(it) }

        extension?.excludes
            ?.takeIf { it.isNotEmpty() }
            ?.let { exclude.addAll(it) }

        exclude.addAll(CocoInstrumentConst.ignoreClasses)

    }

    fun isVariantSupport(variantName: String): Boolean {
        return !unSupportVariants.contains(variantName.toLowerCase())
    }

    fun matches(className: String): Boolean {
        val include = include.isEmpty() || include.any { Pattern.matches(it, className) }
        var exclude = exclude.any { Pattern.matches(it, className) }


        var match = include && !exclude
//        println("$className , isMatch:${match}")
        if (match) {
            FileUtil.record("$className , include,exclude,match:${include},${exclude},${match}\n")
        }
        return match
//        return true
    }

    fun isTransformAsmTask(): Boolean = instrumentType == InstrumentType_7_0_transformASM_task

}