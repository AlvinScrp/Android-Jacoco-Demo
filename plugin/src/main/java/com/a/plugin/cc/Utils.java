package com.a.plugin.cc;

import org.jacoco.core.internal.data.CRC64;
import org.jacoco.core.internal.flow.ClassProbesAdapter;
import org.jacoco.core.internal.instr.ClassInstrumenter;
import org.jacoco.core.internal.instr.IProbeArrayStrategy;
import org.jacoco.core.internal.instr.InstrSupport;
import org.jacoco.core.internal.instr.ProbeArrayStrategyFactory;
import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.jacoco.core.runtime.OfflineInstrumentationAccessGenerator;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import javax.annotation.Nullable;

public class Utils {

    public static boolean endWidth(@Nullable  String source,@Nullable  String with){
        return source!=null && with!=null && source.endsWith(with);
    }


    public static boolean startWith(@NotNull String source, @NotNull String with) {
        return source!=null && with!=null && source.startsWith(with);
    }



}
