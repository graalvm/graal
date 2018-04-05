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
package org.graalvm.compiler.lir.amd64;

import static jdk.vm.ci.amd64.AMD64.rdi;
import static jdk.vm.ci.amd64.AMD64.rdx;
import static jdk.vm.ci.amd64.AMD64.rsi;

import static jdk.vm.ci.code.ValueUtil.asRegister;
import static org.graalvm.compiler.lir.LIRInstruction.OperandFlag.REG;

import org.graalvm.compiler.asm.amd64.AMD64MacroAssembler;
import org.graalvm.compiler.core.common.LIRKind;
import org.graalvm.compiler.lir.LIRInstructionClass;
import org.graalvm.compiler.lir.Opcode;
import org.graalvm.compiler.lir.asm.CompilationResultBuilder;
import org.graalvm.compiler.lir.gen.LIRGeneratorTool;

import jdk.vm.ci.amd64.AMD64Kind;
import jdk.vm.ci.code.Register;
import jdk.vm.ci.meta.Value;


@Opcode("AMD64_STRING_INFLATE")
public final class AMD64StringLatin1InflateOp extends AMD64LIRInstruction {
    public static final LIRInstructionClass<AMD64StringLatin1InflateOp> TYPE = LIRInstructionClass.create(AMD64StringLatin1InflateOp.class);

    @Alive({REG}) private Value rsrc;
    @Alive({REG}) private Value rdst;
    @Alive({REG}) private Value rlen;

    @Temp({REG}) private Value vtmp1;
    @Temp({REG}) private Value rtmp2;

    public AMD64StringLatin1InflateOp(LIRGeneratorTool tool, Value src, Value dst, Value len) {
        super(TYPE);

        assert asRegister(src).equals(rsi);
        assert asRegister(dst).equals(rdi);
        assert asRegister(len).equals(rdx);

        rsrc = src;
        rdst = dst;
        rlen = len;

        vtmp1 = tool.newVariable(LIRKind.value(AMD64Kind.V512_BYTE));
        rtmp2 = tool.newVariable(LIRKind.value(AMD64Kind.DWORD));
    }

    @Override
    public void emitCode(CompilationResultBuilder crb, AMD64MacroAssembler masm) {

        Register src = asRegister(rsrc);
        Register dst = asRegister(rdst);
        Register len = asRegister(rlen);

        Register tmp1 = asRegister(vtmp1);
        Register tmp2 = asRegister(rtmp2);

        masm.byte_array_inflate(src, dst, len, tmp1, tmp2);
    }

}
