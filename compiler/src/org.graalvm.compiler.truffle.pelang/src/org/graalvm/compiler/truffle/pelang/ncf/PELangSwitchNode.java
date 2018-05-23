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
package org.graalvm.compiler.truffle.pelang.ncf;

import org.graalvm.compiler.truffle.pelang.PELangStatementNode;
import org.graalvm.compiler.truffle.pelang.expr.PELangExpressionNode;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;

public final class PELangSwitchNode extends PELangStatementNode {

    @Child private PELangExpressionNode valueNode;
    @Child private PELangStatementNode defaultBodyNode;

    @Children private final PELangExpressionNode[] caseValueNodes;
    @Children private final PELangStatementNode[] caseBodyNodes;

    public PELangSwitchNode(PELangExpressionNode valueNode, PELangStatementNode defaultBodyNode, PELangExpressionNode[] caseValueNodes, PELangStatementNode[] caseBodyNodes) {
        if (caseValueNodes.length != caseBodyNodes.length) {
            throw new IllegalArgumentException("length of case value and body nodes must be the same");
        }
        this.valueNode = valueNode;
        this.defaultBodyNode = defaultBodyNode;
        this.caseValueNodes = caseValueNodes;
        this.caseBodyNodes = caseBodyNodes;
    }

    public PELangExpressionNode getValueNode() {
        return valueNode;
    }

    public PELangStatementNode getDefaultBodyNode() {
        return defaultBodyNode;
    }

    public PELangExpressionNode[] getCaseValueNodes() {
        return caseValueNodes;
    }

    public PELangStatementNode[] getCaseBodyNodes() {
        return caseBodyNodes;
    }

    @ExplodeLoop
    @Override
    public void executeVoid(VirtualFrame frame) {
        CompilerAsserts.compilationConstant(caseValueNodes.length);
        CompilerAsserts.compilationConstant(caseBodyNodes.length);

        Object value = valueNode.executeGeneric(frame);

        for (int i = 0; i < caseValueNodes.length; i++) {
            PELangExpressionNode caseValueNode = caseValueNodes[i];
            Object caseValue = caseValueNode.executeGeneric(frame);

            if (value.equals(caseValue)) {
                PELangStatementNode caseBodyNode = caseBodyNodes[i];
                CompilerAsserts.partialEvaluationConstant(caseBodyNode);

                caseBodyNode.executeVoid(frame);
                return;
            }
        }
        defaultBodyNode.executeVoid(frame);
    }

}
