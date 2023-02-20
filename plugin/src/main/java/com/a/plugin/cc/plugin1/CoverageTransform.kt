package com.a.plugin.cc.plugin1

import com.a.plugin.cc.CoverageConfig
import com.android.build.api.instrumentation.*
import org.jacoco.core.internal.data.CRC64
import org.jacoco.core.internal.flow.ClassProbesAdapter
import org.jacoco.core.internal.instr.ClassInstrumenter
import org.jacoco.core.internal.instr.InstrSupport
import org.jacoco.core.internal.instr.ProbeArrayStrategyFactory
import org.jacoco.core.runtime.IExecutionDataAccessorGenerator
import org.jacoco.core.runtime.OfflineInstrumentationAccessGenerator
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

abstract class CoverageTransform : AsmClassVisitorFactory<InstrumentationParameters.None> {

    /**
     * @see com.android.build.gradle.internal.instrumentation.AsmInstrumentationManager.doInstrumentByteCode
     */
    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
//        println("nextClassVisitor:$nextClassVisitor")
        val classWriter = originClassWriter(nextClassVisitor)
        return if (classWriter != null) {
            val bytes = getSourceBytes(classWriter)
            createInstrumentClassVisitor(bytes, nextClassVisitor)
        } else {
            //            throw RuntimeException("CoverageTransform createClassVisitor Exception")
            EmptyClassVisitor(nextClassVisitor)
        }

    }


    override fun isInstrumentable(classData: ClassData): Boolean {
        return CoverageConfig.matches(classData.className)
//        return true
    }

    private fun createInstrumentClassVisitor(
        source: ByteArray,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        val accessorGenerator: IExecutionDataAccessorGenerator =
            OfflineInstrumentationAccessGenerator()
        val classId = CRC64.classId(source)
        val reader = InstrSupport.classReaderFor(source)
        val strategy = ProbeArrayStrategyFactory
            .createFor(classId, reader, accessorGenerator)
        val version = InstrSupport.getMajorVersion(reader)
        return ClassProbesAdapter(
            ClassInstrumenter(strategy, nextClassVisitor),
            InstrSupport.needsFrames(version)
        )
    }

    private fun getSourceBytes(classWriter: ClassWriter): ByteArray {
        val symbolTable = RefInvoke.getFieldObject(
            ClassWriter::class.java,
            classWriter,
            "symbolTable"
        )
        val readerObj =
            RefInvoke.getFieldObject(symbolTable, "sourceClassReader")
        val bufferObj = RefInvoke.getFieldObject(readerObj, "classFileBuffer")
        return bufferObj as ByteArray
    }

    /**
     * 递推调用获取
     * @see com.android.build.gradle.internal.instrumentation.FixFramesClassWriter
     */
    private fun originClassWriter(classVisitor: Any?): ClassWriter? {
        if (classVisitor == null) return null
        if (classVisitor is ClassWriter) {
            return classVisitor
        }
        val cvInner: Any? = RefInvoke.getFieldObject(ClassVisitor::class.java, classVisitor, "cv")
        return originClassWriter(cvInner)

    }
}

class EmptyClassVisitor(nextVisitor: ClassVisitor) : ClassVisitor(Opcodes.ASM9, nextVisitor)
