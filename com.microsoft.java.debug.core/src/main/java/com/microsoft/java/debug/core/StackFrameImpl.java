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

package com.microsoft.java.debug.core;

import java.util.List;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.request.EventRequest;

import io.reactivex.disposables.Disposable;

public class StackFrameImpl implements IStackFrame {

    private StackFrame frame;

    public StackFrameImpl(StackFrame frame) {
        this.frame = frame;
    }

    @Override
    public boolean isNative() {
        return frame.location().method().isNative();
    }

    @Override
    public boolean pop() {
        try {
            frame.thread().popFrames(frame);
        } catch (IncompatibleThreadStateException e) {
            return false;
        }
        return true;
    }

    @Override
    public ReferenceType getDeclaringType() {
        return frame.location().method().declaringType();
    }

    @Override
    public ThreadReference getThread() {
        return frame.thread();
    }

    protected StackFrame getUnderlyingFrame() {
        return frame;
    }

    @Override
    public List<EventRequest> requests() {
        return null;
    }

    @Override
    public List<Disposable> subscriptions() {
        return null;
    }

    @Override
    public void close() throws Exception {
    }
}
