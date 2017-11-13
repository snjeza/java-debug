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

import com.microsoft.java.debug.core.DebugEvent;
import com.microsoft.java.debug.core.IDebugSession;
import com.microsoft.java.debug.core.UsageDataSession;
import com.microsoft.java.debug.core.adapter.AdapterUtils;
import com.microsoft.java.debug.core.adapter.ErrorCode;
import com.microsoft.java.debug.core.adapter.IDebugAdapterContext;
import com.microsoft.java.debug.core.adapter.IDebugRequestHandler;
import com.microsoft.java.debug.core.protocol.Events;
import com.microsoft.java.debug.core.protocol.Messages.Response;
import com.microsoft.java.debug.core.protocol.Requests.Arguments;
import com.microsoft.java.debug.core.protocol.Requests.Command;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.ThreadDeathEvent;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.request.MethodEntryRequest;

public class ConfigurationDoneRequestHandler implements IDebugRequestHandler {

    @Override
    public List<Command> getTargetCommands() {
        return Arrays.asList(Command.CONFIGURATIONDONE);
    }

    @Override
    public void handle(Command command, Arguments arguments, Response response, IDebugAdapterContext context) {
        IDebugSession debugSession = context.getDebugSession();
        if (debugSession != null) {
            // This is a global event handler to handle the JDI Event from Virtual Machine.
            debugSession.getEventHub().events().subscribe(debugEvent -> {
                handleDebugEvent(debugEvent, debugSession, context);
            });
            // configuration is done, and start debug session.
            debugSession.start();
        } else {
            context.sendEventAsync(new Events.TerminatedEvent());
            AdapterUtils.setErrorResponse(response, ErrorCode.EMPTY_DEBUG_SESSION, "Failed to launch debug session, the debugger will exit.");
        }
    }

    private void handleDebugEvent(DebugEvent debugEvent, IDebugSession debugSession, IDebugAdapterContext context) {
        Event event = debugEvent.event;
        boolean isImportantEvent = true;
        MethodEntryRequest request = null;
        if (event instanceof VMStartEvent) {
            // ignore
        } else if (event instanceof MethodEntryEvent) {
            // ignore
        } else if (event instanceof VMDeathEvent) {
            context.setVmTerminated();
            context.sendEventAsync(new Events.ExitedEvent(0));
        } else if (event instanceof VMDisconnectEvent) {
            context.setVmTerminated();
            context.sendEventAsync(new Events.TerminatedEvent());
            // Terminate eventHub thread.
            try {
                debugSession.getEventHub().close();
            } catch (Exception e) {
                // do nothing.
            }
        } else if (event instanceof ThreadStartEvent) {
            ThreadReference startThread = ((ThreadStartEvent) event).thread();
            Events.ThreadEvent threadEvent = new Events.ThreadEvent("started", startThread.uniqueID());
            context.sendEventAsync(threadEvent);
        } else if (event instanceof ThreadDeathEvent) {
            ThreadReference deathThread = ((ThreadDeathEvent) event).thread();
            Events.ThreadEvent threadDeathEvent = new Events.ThreadEvent("exited", deathThread.uniqueID());
            context.sendEventAsync(threadDeathEvent);
        } else if (event instanceof BreakpointEvent) {
            if (debugEvent.eventSet.size() > 1 && debugEvent.eventSet.stream().anyMatch(t -> t instanceof StepEvent)) {
                // The StepEvent and BreakpointEvent are grouped in the same event set only if they occurs at the same location and in the same thread.
                // In order to avoid two duplicated StoppedEvents, the debugger will skip the BreakpointEvent.
            } else {
                ThreadReference bpThread = ((BreakpointEvent) event).thread();
                context.sendEventAsync(new Events.StoppedEvent("breakpoint", bpThread.uniqueID()));
                debugEvent.shouldResume = false;
            }
        } else if (event instanceof StepEvent) {
            ThreadReference stepThread = ((StepEvent) event).thread();
            context.sendEventAsync(new Events.StoppedEvent("step", stepThread.uniqueID()));
            debugEvent.shouldResume = false;
        } else if (event instanceof ExceptionEvent) {
            ThreadReference thread = ((ExceptionEvent) event).thread();
            context.sendEventAsync(new Events.StoppedEvent("exception", thread.uniqueID()));
            debugEvent.shouldResume = false;
        } else {
            isImportantEvent = false;
        }

        // record events of important types only, to get rid of noises.
        if (isImportantEvent) {
            UsageDataSession.recordEvent(event);
        }
    }
}
