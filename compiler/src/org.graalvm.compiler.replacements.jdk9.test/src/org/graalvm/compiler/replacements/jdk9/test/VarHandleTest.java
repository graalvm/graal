/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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
package org.graalvm.compiler.replacements.jdk9.test;

import jdk.vm.ci.aarch64.AArch64;
import jdk.vm.ci.meta.ResolvedJavaMethod;
import org.graalvm.compiler.core.test.GraalCompilerTest;
import org.graalvm.compiler.debug.GraalError;
import org.graalvm.compiler.graph.Node;
import org.graalvm.compiler.nodes.StartNode;
import org.graalvm.compiler.nodes.StructuredGraph;
import org.graalvm.compiler.nodes.extended.MembarNode;
import org.graalvm.compiler.nodes.memory.MemoryCheckpoint;
import org.graalvm.compiler.nodes.memory.ReadNode;
import org.graalvm.compiler.nodes.memory.WriteNode;
import org.graalvm.word.LocationIdentity;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class VarHandleTest extends GraalCompilerTest {

    static class Holder {
        /* Field is declared volatile, but accessed with non-volatile semantics in the tests. */
        volatile int volatileField = 42;

        /* Field is declared non-volatile, but accessed with volatile semantics in the tests. */
        int field = 2018;

        static final VarHandle VOLATILE_FIELD;
        static final VarHandle FIELD;

        static {
            try {
                VOLATILE_FIELD = MethodHandles.lookup().findVarHandle(Holder.class, "volatileField", int.class);
                FIELD = MethodHandles.lookup().findVarHandle(Holder.class, "field", int.class);
            } catch (ReflectiveOperationException ex) {
                throw GraalError.shouldNotReachHere(ex);
            }
        }
    }

    public static int testRead1Snippet(Holder h) {
        /* Explicitly access the volatile field with non-volatile access semantics. */
        return (int) Holder.VOLATILE_FIELD.get(h);
    }

    public static int testRead2Snippet(Holder h) {
        /* Explicitly access the volatile field with volatile access semantics. */
        return (int) Holder.VOLATILE_FIELD.getVolatile(h);
    }

    public static int testRead3Snippet(Holder h) {
        /* Explicitly access the non-volatile field with non-volatile access semantics. */
        return (int) Holder.FIELD.get(h);
    }

    public static int testRead4Snippet(Holder h) {
        /* Explicitly access the non-volatile field with volatile access semantics. */
        return (int) Holder.FIELD.getVolatile(h);
    }

    public static void testWrite1Snippet(Holder h) {
        /* Explicitly access the volatile field with non-volatile access semantics. */
        Holder.VOLATILE_FIELD.set(h, 123);
    }

    public static void testWrite2Snippet(Holder h) {
        /* Explicitly access the volatile field with volatile access semantics. */
        Holder.VOLATILE_FIELD.setVolatile(h, 123);
    }

    public static void testWrite3Snippet(Holder h) {
        /* Explicitly access the non-volatile field with non-volatile access semantics. */
        Holder.FIELD.set(h, 123);
    }

    public static void testWrite4Snippet(Holder h) {
        /* Explicitly access the non-volatile field with volatile access semantics. */
        Holder.FIELD.setVolatile(h, 123);
    }

    void testAccess(String name, int expectedReads, int expectedWrites, int expectedMembars, int expectedAnyKill) {
        ResolvedJavaMethod method = getResolvedJavaMethod(name);
        StructuredGraph graph = parseForCompile(method);
        compile(method, graph);
        Assert.assertEquals(expectedReads, graph.getNodes().filter(ReadNode.class).count());
        Assert.assertEquals(expectedWrites, graph.getNodes().filter(WriteNode.class).count());
        Assert.assertEquals(expectedMembars, graph.getNodes().filter(MembarNode.class).count());
        Assert.assertEquals(expectedAnyKill, countAnyKill(graph));
    }

    void testAArch64VolatileAccess(String name, int expectedReads, int expectedWrites, int expectedMembars, int expectedAnyKill) {
        ResolvedJavaMethod method = getResolvedJavaMethod(name);
        StructuredGraph graph = parseForCompile(method);
        compile(method, graph);
        Assert.assertEquals(expectedReads, graph.getNodes().filter(ReadNode.class).count());
        Assert.assertEquals(expectedWrites, graph.getNodes().filter(WriteNode.class).count());
        Assert.assertEquals(expectedMembars, graph.getNodes().filter(MembarNode.class).count());
        Assert.assertEquals(expectedAnyKill, countAnyKill(graph));
        for (Node n : graph.getNodes().filter(ReadNode.class)) {
            String clazzname = n.getClass().getSimpleName();
            Assert.assertTrue(clazzname.equals("AArch64VolatileReadNode") || clazzname.equals("AArch64VolatileExtendingReadNode"));
        }
        for (Node n : graph.getNodes().filter(WriteNode.class)) {
            String clazzname = n.getClass().getSimpleName();
            Assert.assertTrue(clazzname.equals("AArch64VolatileWriteNode"));
        }
    }

    @Test
    public void testRead1() {
        testAccess("testRead1Snippet", 1, 0, 0, 0);
    }

    @Test
    public void testRead2() {
        // ignore this test on AArch64
        Assume.assumeFalse(getTarget().arch instanceof AArch64);
        testAccess("testRead2Snippet", 1, 0, 2, 2);
    }

    @Test
    public void testRead2AArch64() {
        // run this test on AArch64
        Assume.assumeTrue(getTarget().arch instanceof AArch64);
        testAArch64VolatileAccess("testRead2Snippet", 1, 0, 0, 0);
    }

    @Test
    public void testRead3() {
        testAccess("testRead3Snippet", 1, 0, 0, 0);
    }

    @Test
    public void testRead4() {
        // ignore this test on AArch64
        Assume.assumeFalse(getTarget().arch instanceof AArch64);
        testAccess("testRead4Snippet", 1, 0, 2, 2);
    }

    @Test
    public void testRead4AArch64() {
        // run this test on AArch64
        Assume.assumeTrue(getTarget().arch instanceof AArch64);
        testAArch64VolatileAccess("testRead4Snippet", 1, 0, 0, 0);
    }

    @Test
    public void testWrite1() {
        testAccess("testWrite1Snippet", 0, 1, 0, 0);
    }

    @Test
    public void testWrite2() {
        // ignore this test on AArch64
        Assume.assumeFalse(getTarget().arch instanceof AArch64);
        testAccess("testWrite2Snippet", 0, 1, 2, 2);
    }

    @Test
    public void testWrite2AArch64() {
        // run this test on AArch64
        Assume.assumeTrue(getTarget().arch instanceof AArch64);
        testAArch64VolatileAccess("testWrite2Snippet", 0, 1, 0, 0);
    }

    @Test
    public void testWrite3() {
        testAccess("testWrite3Snippet", 0, 1, 0, 0);
    }

    @Test
    public void testWrite4() {
        // ignore this test on AArch64
        Assume.assumeFalse(getTarget().arch instanceof AArch64);
        testAccess("testWrite4Snippet", 0, 1, 2, 2);
    }

    @Test
    public void testWrite4AArch64() {
        // run this test on AArch64
        Assume.assumeTrue(getTarget().arch instanceof AArch64);
        testAArch64VolatileAccess("testWrite4Snippet", 0, 1, 0, 0);
    }

    private static int countAnyKill(StructuredGraph graph) {
        int anyKillCount = 0;
        int startNodes = 0;
        for (Node n : graph.getNodes()) {
            if (n instanceof StartNode) {
                startNodes++;
            } else if (n instanceof MemoryCheckpoint.Single) {
                MemoryCheckpoint.Single single = (MemoryCheckpoint.Single) n;
                if (single.getLocationIdentity().isAny()) {
                    anyKillCount++;
                }
            } else if (n instanceof MemoryCheckpoint.Multi) {
                MemoryCheckpoint.Multi multi = (MemoryCheckpoint.Multi) n;
                for (LocationIdentity loc : multi.getLocationIdentities()) {
                    if (loc.isAny()) {
                        anyKillCount++;
                        break;
                    }
                }
            }
        }
        // Ignore single StartNode.
        Assert.assertEquals(1, startNodes);
        return anyKillCount;
    }
}
