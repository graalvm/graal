/*
 * Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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

package org.graalvm.compiler.truffle.test.inlining;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.TruffleRuntime;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.test.polyglot.ProxyLanguage;
import org.graalvm.polyglot.Context;
import org.junit.Test;

public class ExcludeInliningTest {
    private static final String COMPILATION_ROOT_NAME = "main";
    private static final String METHOD_EXCLUDED_FROM_INLINING = "should-not-be-inlined";

    @Test
    public void testNoInlineForExcludedMethods() throws Exception {
        try (Context c = Context.newBuilder().allowExperimentalOptions(true).//
                    option("engine.CompilationFailureAction", "Throw").//
                    option("engine.CompileImmediately", "true").//
                    option("engine.BackgroundCompilation", "false").//
                    option("engine.CompileOnly", COMPILATION_ROOT_NAME).//
                    option("engine.ExcludeInlining", METHOD_EXCLUDED_FROM_INLINING).//
                    build()) {
            c.eval(ExcludeInliningTest.ExcludeInliningTestLanguage.ID, "");
            c.eval(ExcludeInliningTest.ExcludeInliningTestLanguage.ID, "");
        }
    }

    @TruffleLanguage.Registration(id = ExcludeInliningTest.ExcludeInliningTestLanguage.ID, name = "Exclude Inlining Test Language", version = "1.0")
    public static class ExcludeInliningTestLanguage extends ProxyLanguage {

        public static final String ID = "truffle-exclude-inlining-test-language";

        @Override
        protected CallTarget parse(ParsingRequest request) throws Exception {
            final TruffleRuntime runtime = Truffle.getRuntime();
            final RootCallTarget rootCallTarget = runtime.createCallTarget(new RootNode(this) {

                @Override
                public String toString() {
                    return getName();
                }

                @Override
                public Object execute(VirtualFrame frame) {
                    CompilerAsserts.neverPartOfCompilation("This node should not be inlined");
                    return 99;
                }

                @Override
                public String getName() {
                    return METHOD_EXCLUDED_FROM_INLINING;
                }
            });
            return runtime.createCallTarget(new RootNode(this) {

                @Child
                DirectCallNode callNode = runtime.createDirectCallNode(rootCallTarget);

                @Override
                public String toString() {
                    return getName();
                }

                @Override
                public String getName() {
                    return COMPILATION_ROOT_NAME;
                }

                @Override
                public Object execute(VirtualFrame frame) {
                    return callNode.call();
                }
            });
        }
    }
}
