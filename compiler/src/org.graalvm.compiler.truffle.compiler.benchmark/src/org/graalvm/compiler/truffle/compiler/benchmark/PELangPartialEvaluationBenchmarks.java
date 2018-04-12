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
package org.graalvm.compiler.truffle.compiler.benchmark;

import org.graalvm.compiler.truffle.compiler.benchmark.pelang.PELangBasicBlockNode;
import org.graalvm.compiler.truffle.compiler.benchmark.pelang.PELangExpressionBuilder;
import org.graalvm.compiler.truffle.runtime.OptimizedCallTarget;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Truffle;

public class PELangPartialEvaluationBenchmarks {

    public static class ExampleOneBenchmark extends PartialEvaluationBenchmark {

        @Override
        protected OptimizedCallTarget createCallTarget() {
            PELangExpressionBuilder b = new PELangExpressionBuilder();

            // @formatter:off
            CallTarget callTarget = Truffle.getRuntime().createCallTarget(b.root(
                b.expressionBlock(
                    b.write(0, "flag"),
                    b.write(0, "counter"),
                    b.loop(
                        b.read("flag"),
                        b.expressionBlock(
                            b.increment(1, "counter"),
                            b.branch(
                                b.equals(10, "counter"),
                                b.write(1, "flag")))),
                    b.read("counter"))));
            // @formatter:on

            return (OptimizedCallTarget) callTarget;
        }

    }

    public static class DispatchBenchmark extends PartialEvaluationBenchmark {

        @Override
        protected OptimizedCallTarget createCallTarget() {
            PELangExpressionBuilder b = new PELangExpressionBuilder();

            // @formatter:off
            CallTarget callTarget = Truffle.getRuntime().createCallTarget(b.root(
                b.dispatch(
                    b.basicBlock(b.write(0, "flag"), 1),                                   // block 0
                    b.basicBlock(b.write(0, "counter"), 2),                                // block 1
                    b.basicBlock(b.read("flag"), 3, 6),                                    // block 2
                    b.basicBlock(b.increment(1, "counter"), 4),                            // block 3
                    b.basicBlock(b.equals(10, "counter"), 5, 2),                           // block 4
                    b.basicBlock(b.write(1, "flag"), 2),                                   // block 5
                    b.basicBlock(b.read("counter"), PELangBasicBlockNode.NO_SUCCESSOR)))); // block 6
            // @formatter:on

            return (OptimizedCallTarget) callTarget;
        }

    }

}
