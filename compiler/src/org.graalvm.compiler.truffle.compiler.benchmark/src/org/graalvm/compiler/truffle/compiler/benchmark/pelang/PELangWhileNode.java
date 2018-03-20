/*
 * Copyright (c) 2018, 2018, Oracle and/or its affiliates. All rights reserved.
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
package org.graalvm.compiler.truffle.compiler.benchmark.pelang;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.nodes.RepeatingNode;

public final class PELangWhileNode extends PELangExpressionNode {

    @Child private LoopNode loopNode;

    public PELangWhileNode(PELangExpressionNode conditionNode, PELangExpressionNode bodyNode) {
        loopNode = Truffle.getRuntime().createLoopNode(new PELangWhileRepeatingNode(conditionNode, bodyNode));
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        Object lastResult = PELangNull.Instance;

        try {
            loopNode.executeLoop(frame);
        } catch (PELangResultException e) {
            lastResult = e.getLastResult();
        }
        return lastResult;
    }

    private static final class PELangWhileRepeatingNode implements RepeatingNode {

        @Child private PELangExpressionNode conditionNode;
        @Child private PELangExpressionNode bodyNode;

        private Object lastResult = PELangNull.Instance;

        public PELangWhileRepeatingNode(PELangExpressionNode conditionNode, PELangExpressionNode bodyNode) {
            this.conditionNode = conditionNode;
            this.bodyNode = bodyNode;
        }

        @Override
        public boolean executeRepeating(VirtualFrame frame) {
            long conditionResult = conditionNode.evaluateCondition(frame);

            if (conditionResult == 0L) {
                lastResult = bodyNode.executeGeneric(frame);
                return true;
            } else {
                throw new PELangResultException(lastResult);
            }
        }
    }

    private static final class PELangResultException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        private final Object lastResult;

        public PELangResultException(Object lastResult) {
            this.lastResult = lastResult;
        }

        public Object getLastResult() {
            return lastResult;
        }

    }

}
