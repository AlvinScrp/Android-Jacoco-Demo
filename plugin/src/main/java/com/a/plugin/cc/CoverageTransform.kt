package com.a.plugin.cc

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
import java.util.regex.Pattern

abstract class CoverageTransform : AsmClassVisitorFactory<InstrumentationParameters.None> {

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        println("nextClassVisitor:$nextClassVisitor")

        if (nextClassVisitor is ClassWriter) {
            val bytes = getSourceBytes(nextClassVisitor)
            return genClassVisitor(bytes, nextClassVisitor)
        } else {
            throw RuntimeException("CoverageTransform createClassVisitor Exception")
//            return EmptyClassVisitor(nextClassVisitor)
        }
    }



    override fun isInstrumentable(classData: ClassData): Boolean {
        var instrumentable = Pattern.matches("com.a.jacocotest.*", classData.className)
        return instrumentable
//        return true
    }

    private fun genClassVisitor(
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
            RefInvoke.getFieldObject( symbolTable, "sourceClassReader")
        val bufferObj = RefInvoke.getFieldObject(readerObj, "classFileBuffer")
        return bufferObj as ByteArray
    }
}
class EmptyClassVisitor(nextVisitor: ClassVisitor) : ClassVisitor(Opcodes.ASM9, nextVisitor)
