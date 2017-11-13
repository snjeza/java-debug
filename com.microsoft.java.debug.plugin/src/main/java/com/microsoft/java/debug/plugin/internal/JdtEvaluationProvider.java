package com.microsoft.java.debug.plugin.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import com.microsoft.java.debug.core.adapter.IEvaluationListener;
import com.microsoft.java.debug.core.adapter.IEvaluationProvider;
import com.microsoft.java.debug.plugin.internal.eval.EvaluationEngine;
import com.microsoft.java.debug.plugin.internal.eval.LegacyUtils;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;

public class JdtEvaluationProvider implements IEvaluationProvider {

    @Override
    public String eval(String code, StackFrame sf, IEvaluationListener listener) {
        // TODO Auto-generated method stub
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        for(IProject proj : root.getProjects()) {
               try {
                if (proj.isNatureEnabled("org.eclipse.jdt.core.javanature") && proj.getName().contains("lucene")) {
                        IJavaProject javaProject = JavaCore.create(proj);
                        return new EvaluationEngine().test(javaProject, sf, code, listener);
                   }
            } catch (CoreException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return "";

    }

    @Override
    public boolean isInEvaluation(ThreadReference thread) {
        // TODO Auto-generated method stub
        return LegacyUtils.createJDIThread(thread, null).isPerformingEvaluation();
    }

}
