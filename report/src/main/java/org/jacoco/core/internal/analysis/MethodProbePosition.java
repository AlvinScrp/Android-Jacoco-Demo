package org.jacoco.core.internal.analysis;

public class MethodProbePosition {

    private boolean[] probes ;

    private int start = -1;
    private int end = -1;

    public MethodProbePosition(boolean[] probes, int start, int end) {
        this.probes = probes;
        this.start = start;
        this.end = end;
    }

    public boolean[] getProbes() {
        return probes;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }
}
