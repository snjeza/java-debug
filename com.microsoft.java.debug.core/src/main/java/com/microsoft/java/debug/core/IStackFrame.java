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

import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;

public interface IStackFrame extends IDebugResource {

    boolean isNative();

    boolean pop();

    ReferenceType getDeclaringType();

    ThreadReference getThread();
}
