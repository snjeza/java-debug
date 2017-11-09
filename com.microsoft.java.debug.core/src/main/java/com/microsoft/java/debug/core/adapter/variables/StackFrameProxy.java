package com.microsoft.java.debug.core.adapter.variables;

import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;

public class StackFrameProxy {
    private ThreadReference thread;
    private StackFrame stackFrame;
    private int depth;

    public StackFrameProxy(StackFrame stackFrame, int depth) {
        thread = stackFrame.thread();
        this.stackFrame = stackFrame;
        this.depth = depth;
    }

    public ThreadReference getThread() {
        return thread;
    }

    public StackFrame getStackFrame() {
        return stackFrame;
    }

    public int getDepth() {
        return depth;
    }
}
