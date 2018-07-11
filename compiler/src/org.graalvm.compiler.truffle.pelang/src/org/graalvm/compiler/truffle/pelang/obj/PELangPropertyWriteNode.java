/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package org.graalvm.compiler.truffle.pelang.obj;

import org.graalvm.compiler.truffle.pelang.PELangException;
import org.graalvm.compiler.truffle.pelang.PELangExpressionNode;
import org.graalvm.compiler.truffle.pelang.PELangState;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;

public final class PELangPropertyWriteNode extends PELangExpressionNode {

    @Child private PELangExpressionNode receiverNode;
    private final String name;
    @Child private PELangExpressionNode valueNode;
    @Child private PELangPropertyWriteCacheNode writeNode = PELangPropertyWriteCacheNode.createNode();

    public PELangPropertyWriteNode(PELangExpressionNode receiverNode, String name, PELangExpressionNode valueNode) {
        this.receiverNode = receiverNode;
        this.name = name;
        this.valueNode = valueNode;
    }

    public PELangExpressionNode getReceiverNode() {
        return receiverNode;
    }

    public String getName() {
        return name;
    }

    public PELangExpressionNode getValueNode() {
        return valueNode;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        DynamicObject receiver = receiverNode.evaluateObject(frame);

        if (!PELangState.isPELangObject(receiver)) {
            throw new PELangException("receiver must be a PELangObject", this);
        }
        Object value = valueNode.executeGeneric(frame);
        writeNode.executeWrite(receiver, name, value);
        return value;
    }

}
