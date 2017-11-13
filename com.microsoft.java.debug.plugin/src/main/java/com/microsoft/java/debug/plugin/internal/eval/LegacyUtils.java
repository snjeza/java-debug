package com.microsoft.java.debug.plugin.internal.eval;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIFieldVariable;
import org.eclipse.jdt.internal.debug.core.model.JDILocalVariable;
import org.eclipse.jdt.internal.debug.core.model.JDIReferenceType;
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;
import org.eclipse.jdt.internal.debug.core.model.JDIType;

import com.microsoft.java.debug.core.adapter.handler.EvaluateRequestHandler;
import com.sun.jdi.Field;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;

public class LegacyUtils {
    private static Map<ThreadReference, JDIThread> threadMap = new HashMap<>();

    public static StackFrame refreshStackFrames(StackFrame sf) {
        return EvaluateRequestHandler.refreshStackFrames(sf);
    }

    public static JDIReferenceType createJDIReferenceType(ReferenceType type) {
        return (JDIReferenceType) JDIType.createType(null, type);
    }

    public static IJavaVariable createLocalVariable(StackFrame sf, LocalVariable local, IJavaProject project) {
        return new JDILocalVariable(createJDIStackFrame(sf, project), local);
    }

    public static IJavaVariable createFieldVariable(Field field, ObjectReference or) {
        return new JDIFieldVariable(null, field, or, null);
    }

    public static JDIStackFrame createJDIStackFrame(StackFrame sf, IJavaProject project) {
        try {
            for (Object df : createJDIThread(sf.thread(), project).computeNewStackFrames()) {
                JDIStackFrame sf2 = (JDIStackFrame) df;
                if (sf2.getLineNumber() == sf.location().lineNumber() && sf2.getUnderlyingMethod().equals(sf.location().method())) {
                    return sf2;
                }
            }
            return new JDIStackFrame(createJDIThread(sf.thread(), project), sf, 0);
        } catch (DebugException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new JDIStackFrame(createJDIThread(sf.thread(), project), sf, 0);
        }

    }

    public static JDIThread createJDIThread(ThreadReference thread, IJavaProject project) {
        if (threadMap.containsKey(thread)) {
            return threadMap.get(thread);
        }
        JDIThread newThread = new JDIThread(createJDIDebugTarget(thread.virtualMachine(), project), thread);
        threadMap.put(thread, newThread);
        return newThread;
    }

    public static JDIDebugTarget createJDIDebugTarget(VirtualMachine vm, IJavaProject project) {
        return new JDIDebugTarget(createLaunch(project), vm, "", false, false, null, false) {
            @Override
            protected synchronized void initialize() {

            }
        };
    }

    private static ILaunch createLaunch(IJavaProject project) {
        return new ILaunch() {

            @Override
            public boolean canTerminate() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean isTerminated() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void terminate() throws DebugException {
                // TODO Auto-generated method stub

            }

            @Override
            public <T> T getAdapter(Class<T> arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void addDebugTarget(IDebugTarget arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void addProcess(IProcess arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public String getAttribute(String arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Object[] getChildren() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public IDebugTarget getDebugTarget() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public IDebugTarget[] getDebugTargets() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public ILaunchConfiguration getLaunchConfiguration() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String getLaunchMode() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public IProcess[] getProcesses() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public ISourceLocator getSourceLocator() {
                // TODO Auto-generated method stub
                // JavaSourceLookupDirector javaSourceLookupDirector = new
                // JavaSourceLookupDirector();
                // return new org.eclipse.jdt.internal.launching.JavaSourceLookupDirector();
                return new AbstractSourceLookupDirector() {

                    @Override
                    public void initializeParticipants() {
                        // TODO Auto-generated method stub
                        try {
                            this.setSourceContainers( new ProjectSourceContainer((IProject) project, true).getSourceContainers());
                        } catch (CoreException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                };
            }

            @Override
            public boolean hasChildren() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void removeDebugTarget(IDebugTarget arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void removeProcess(IProcess arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setAttribute(String arg0, String arg1) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setSourceLocator(ISourceLocator arg0) {
                // TODO Auto-generated method stub

            }

        };
    }
}
