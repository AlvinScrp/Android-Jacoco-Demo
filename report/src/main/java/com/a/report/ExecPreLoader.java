/*******************************************************************************
 * Copyright (c) 2009, 2021 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package com.a.report;

import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.internal.ContentTypeDetector;
import org.jacoco.core.internal.InputStreams;
import org.jacoco.core.internal.data.CRC64;
import org.jacoco.core.internal.flow.ClassProbesAdapter;
import org.jacoco.core.internal.instr.InstrSupport;
import org.jacoco.core.tools.ExecFileLoader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


class ExecPreLoader {

    private final SessionInfoStore sessionInfos;
    private final ExecutionDataStore executionData;

    /**
     * New instance to combine session infos and execution data from multiple
     * files.
     */
    public ExecPreLoader() {
        sessionInfos = new SessionInfoStore();
        executionData = new ExecutionDataStore();
    }

    public int analyzeAll(final File file) {
        int count = 0;
        if (file.isDirectory()) {
            for (final File f : file.listFiles()) {
                count += analyzeAll(f);
            }
        } else {
            count += analyzeFile(file, file.getPath());
        }
        return count;
    }

    public int analyzeFile(final File file, final String location) {
        try {
            InputStream input = new FileInputStream(file);
            final ContentTypeDetector detector;
            detector = new ContentTypeDetector(input);
            if (detector.getType() == ContentTypeDetector.CLASSFILE) {
                byte[] source = InputStreams.readFully(detector.getInputStream());
                final long classId = CRC64.classId(source);
                final ClassReader reader = InstrSupport.classReaderFor(source);

                int access = reader.getAccess();
                if ((access & Opcodes.ACC_MODULE) == 0
                        || (access & Opcodes.ACC_SYNTHETIC) == 0) {
                    analyzeExecData(classId, reader);
                    return 1;
                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
            analyzerError(location, e).printStackTrace();
        }
        return 0;
    }

    private void analyzeExecData(long classId, ClassReader reader) {
        String className = reader.getClassName();
        final ProbeCounterVisitor counter = new ProbeCounterVisitor();
        reader.accept(new ClassProbesAdapter(counter, false), 0);
        int probeCount = counter.getCount();
        executionData.put(new ExecutionData(classId, className, probeCount));

    }


    private IOException analyzerError(final String location,
                                      final Exception cause) {
        final IOException ex = new IOException(
                String.format("Error while analyzing %s.", location));
        ex.initCause(cause);
        return ex;
    }

    public ExecutionDataStore getExecutionDataStore() {
        return executionData;
    }
}
