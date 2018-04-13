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
package org.graalvm.compiler.truffle.pelang;

import org.graalvm.compiler.truffle.pelang.PELangBasicBlockNode.Execution;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.ExplodeLoop.LoopExplosionKind;

public class PELangBasicBlockDispatchNode extends PELangExpressionNode {

    @Children private final PELangBasicBlockNode[] blockNodes;

    public PELangBasicBlockDispatchNode(PELangBasicBlockNode[] blockNodes) {
        this.blockNodes = blockNodes;
    }

    @Override
    @ExplodeLoop(kind = LoopExplosionKind.MERGE_EXPLODE)
    public Object executeGeneric(VirtualFrame frame) {
        CompilerAsserts.compilationConstant(blockNodes.length);
        Object result = PELangNull.Instance;
        int blockIndex = (blockNodes.length == 0) ? PELangBasicBlockNode.NO_SUCCESSOR : 0;

        while (blockIndex != PELangBasicBlockNode.NO_SUCCESSOR) {
            Execution execution = blockNodes[blockIndex].executeBlock(frame);

            result = execution.getResult();
            blockIndex = execution.getSuccessor();
        }

        return result;
    }

}
