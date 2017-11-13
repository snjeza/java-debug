/*******************************************************************************
 * Copyright (c) 2017 Microsoft Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Microsoft Corporation - initial API and implementation
 *******************************************************************************/

package com.microsoft.java.debug.plugin.internal.eval;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.eval.ICompiledExpression;
import org.eclipse.jdt.internal.debug.eval.ast.engine.ASTEvaluationEngine;
import org.eclipse.jdt.internal.debug.eval.ast.engine.RuntimeContext;
import org.eclipse.jdt.internal.debug.eval.ast.instructions.InstructionSequence;

import com.microsoft.java.debug.core.adapter.IEvaluationListener;
import com.sun.jdi.StackFrame;

public class EvaluationEngine {

    public String test(IJavaProject project, StackFrame sf, String code, IEvaluationListener listener) throws Exception {
        ASTEvaluationEngine engine = new ASTEvaluationEngine(project, LegacyUtils.createJDIDebugTarget(sf.virtualMachine(), project));
        ICompiledExpression result = engine.getCompiledExpression(code, LegacyUtils.createJDIStackFrame(sf, project));
        RuntimeContext context = new RuntimeContext(project, LegacyUtils.createJDIStackFrame(sf, project));
        InstructionSequence fExpression = (InstructionSequence)result;
        engine.evaluateExpression(fExpression, LegacyUtils.createJDIStackFrame(sf, project), rrr -> {
            if (rrr == null || rrr.getValue() == null) {
                listener.evaluationComplete("error");
            } else
            listener.evaluationComplete(rrr.getValue().toString());
        }, 0, false);
        return "";
    }


}
