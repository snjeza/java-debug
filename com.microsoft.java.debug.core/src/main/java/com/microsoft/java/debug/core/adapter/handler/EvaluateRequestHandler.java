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

package com.microsoft.java.debug.core.adapter.handler;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.microsoft.java.debug.core.adapter.AdapterUtils;
import com.microsoft.java.debug.core.adapter.ErrorCode;
import com.microsoft.java.debug.core.adapter.IDebugAdapterContext;
import com.microsoft.java.debug.core.adapter.IDebugRequestHandler;
import com.microsoft.java.debug.core.adapter.IEvaluationProvider;
import com.microsoft.java.debug.core.adapter.formatter.NumericFormatEnum;
import com.microsoft.java.debug.core.adapter.formatter.NumericFormatter;
import com.microsoft.java.debug.core.adapter.formatter.SimpleTypeFormatter;
import com.microsoft.java.debug.core.adapter.variables.JdiObjectProxy;
import com.microsoft.java.debug.core.protocol.Messages.Response;
import com.microsoft.java.debug.core.protocol.Requests.Arguments;
import com.microsoft.java.debug.core.protocol.Requests.Command;
import com.microsoft.java.debug.core.protocol.Requests.EvaluateArguments;
import com.microsoft.java.debug.core.protocol.Responses;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.StackFrame;

public class EvaluateRequestHandler implements IDebugRequestHandler {
    private final Pattern simpleExprPattern = Pattern.compile("[A-Za-z0-9_.\\s]+");

    @Override
    public List<Command> getTargetCommands() {
        return Arrays.asList(Command.EVALUATE);
    }
    public static StackFrame refreshStackFrames(StackFrame sf) {
        try {
            sf.thisObject();
            return sf;
        } catch (InvalidStackFrameException ex) {
             try {
                for(StackFrame nsf : sf.thread().frames()) {
                    if (nsf.location().equals(sf.location())) {
                        return nsf;
                    }
                }

            } catch (IncompatibleThreadStateException e) {
                e.printStackTrace();
            }
        }
        return sf;
    }
    @Override
    public void handle(Command command, Arguments arguments, Response response, IDebugAdapterContext context) {
        EvaluateArguments evalArguments = (EvaluateArguments) arguments;
        if (StringUtils.isBlank(evalArguments.expression)) {
            AdapterUtils.setErrorResponse(response, ErrorCode.ARGUMENT_MISSING,
                    " EvaluateRequest: property 'expression' is missing, null, or empty");
            return;
        }

        // This should be false by default(currently true for test).
        // User will need to explicitly turn it on by configuring launch.json
        final boolean showStaticVariables = true;
        // TODO: when vscode protocol support customize settings of value format, showFullyQualifiedNames should be one of the options.
        boolean showFullyQualifiedNames = true;
        Map<String, Object> options = context.getVariableFormatter().getDefaultOptions();
        if (evalArguments.format != null && evalArguments.format.hex) {
            options.put(NumericFormatter.NUMERIC_FORMAT_OPTION, NumericFormatEnum.HEX);
        }
        if (showFullyQualifiedNames) {
            options.put(SimpleTypeFormatter.QUALIFIED_CLASS_NAME_OPTION, showFullyQualifiedNames);
        }
        String expression = evalArguments.expression;

        if (StringUtils.isBlank(expression)) {
            AdapterUtils.setErrorResponse(response, ErrorCode.EVALUATE_FAILURE, "Failed to evaluate. Reason: Empty expression cannot be evaluated.");
            return;
        }


        JdiObjectProxy<StackFrame> stackFrameProxy = (JdiObjectProxy<StackFrame>) context.getRecyclableIdPool().getObjectById(evalArguments.frameId);
        if (stackFrameProxy == null) {
            // stackFrameProxy is null means the stackframe is continued by user manually,
            AdapterUtils.setErrorResponse(response, ErrorCode.EVALUATE_FAILURE, "Failed to evaluate. Reason: Cannot evaluate because the thread is resumed.");
            return;
        }
        StackFrame sf = refreshStackFrames(stackFrameProxy.getProxiedObject());
        if (sf == null) {
            sf = stackFrameProxy.getProxiedObject();
        }
        IEvaluationProvider engine = context.getProvider(IEvaluationProvider.class);
        final IDebugAdapterContext finalContext = context;
        engine.eval(expression, sf, result -> {
            response.body = new Responses.EvaluateResponseBody(result,
                    0, "test",
                    0);
            finalContext.sendResponseAsync(response);
        });
//        if (!StringUtils.isBlank(test)) {
//            response.body = new Responses.EvaluateResponseBody(test,
//                    0, "test",
//                    0);
//
//        } else {
//            response.body = new Responses.EvaluateResponseBody("fdsfs",
//                    0, "test",
//                    0);
//
//        }
        context.setResponseAsync(true);

    }
}
