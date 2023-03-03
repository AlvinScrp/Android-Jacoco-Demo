package com.a.plugin.buildbackup;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.process.ExecResult;
import org.gradle.process.ExecSpec;

import java.io.File;
import java.io.OutputStream;
import java.util.List;

public class ExecCmd {

    public static void execute(Project project, String executable, List<String> args, File workDir) {
        execute(project, executable, args, workDir, System.out);
    }

    public static void execute(Project project, String executable, List<String> args, File workDir, OutputStream out) {
        ExecResult result = project.exec(new Action<ExecSpec>() {
            @Override
            public void execute(ExecSpec execSpec) {
                execSpec.setExecutable(executable);
                execSpec.setArgs(args);
                execSpec.setWorkingDir(workDir);
                execSpec.setStandardOutput(out);
                execSpec.setErrorOutput(System.err);
            }
        });
        result.assertNormalExitValue();
//        result.assertNormalExitValue();
//        System.out.println("result.getExitValue:"+result.getExitValue());
    }
}
