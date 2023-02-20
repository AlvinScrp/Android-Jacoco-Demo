package com.a.plugin.cc;

import org.jacoco.core.internal.InputStreams;
import org.jacoco.core.internal.data.CRC64;
import org.jacoco.core.internal.flow.ClassProbesAdapter;
import org.jacoco.core.internal.instr.ClassInstrumenter;
import org.jacoco.core.internal.instr.IProbeArrayStrategy;
import org.jacoco.core.internal.instr.InstrSupport;
import org.jacoco.core.internal.instr.ProbeArrayStrategyFactory;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Consumer;

public class AsmTes {
    public static void test() {
        System.out.println("----------startTest");
        FileInputStream input = null;
        try {
            input = new FileInputStream("/Users/canglong/Documents/github_project/Android-Jacoco-Demo/MainActivity.class");
            byte[] source = InputStreams.readFully(input);
            final long classId = CRC64.classId(source);
            final ClassReader reader = InstrSupport.classReaderFor(source);
            final ClassWriter writer = new ClassWriter(reader, 0) {
                @Override
                protected String getCommonSuperClass(final String type1,
                                                     final String type2) {
                    throw new IllegalStateException();
                }
            };

            Arrays.stream(writer.getClass().getDeclaredFields()).forEach(
                    method -> {
                        System.out.println(method.getName());
                    }
            );
          Object symbolTable =   RefInvoke.getFieldObject(ClassWriter.class, writer,"symbolTable");
          Object readerObj = RefInvoke.getFieldObject(symbolTable,"sourceClassReader");
            Object bufferObj =     RefInvoke.getFieldObject(readerObj,"classFileBuffer");
            byte[] out = (byte[]) bufferObj;

            for (int i = 0; i < source.length; i++) {
                if (source[i] != out[i]) {
                    System.out.println("位置 " + i + " 不相等");
                    break;
                }
            }



        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("----------endTest------------------------------------------");

    }

}
