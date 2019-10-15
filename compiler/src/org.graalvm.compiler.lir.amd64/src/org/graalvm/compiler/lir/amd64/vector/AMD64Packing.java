/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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
package org.graalvm.compiler.lir.amd64.vector;

import jdk.vm.ci.amd64.AMD64;
import jdk.vm.ci.amd64.AMD64Kind;
import jdk.vm.ci.code.Register;
import jdk.vm.ci.meta.AllocatableValue;
import jdk.vm.ci.meta.JavaConstant;
import jdk.vm.ci.meta.PlatformKind;
import jdk.vm.ci.meta.Value;
import org.graalvm.compiler.asm.amd64.AMD64Address;
import org.graalvm.compiler.asm.amd64.AMD64MacroAssembler;
import org.graalvm.compiler.asm.amd64.AVXKind;
import org.graalvm.compiler.core.common.LIRKind;
import org.graalvm.compiler.debug.GraalError;
import org.graalvm.compiler.lir.LIRInstructionClass;
import org.graalvm.compiler.lir.amd64.AMD64AddressValue;
import org.graalvm.compiler.lir.amd64.AMD64LIRInstruction;
import org.graalvm.compiler.lir.asm.CompilationResultBuilder;
import org.graalvm.compiler.lir.gen.LIRGeneratorTool;

import java.nio.ByteBuffer;
import java.util.List;

import static java.lang.Double.doubleToRawLongBits;
import static java.lang.Float.floatToRawIntBits;
import static jdk.vm.ci.code.ValueUtil.asRegister;
import static jdk.vm.ci.code.ValueUtil.isRegister;
import static jdk.vm.ci.code.ValueUtil.isStackSlot;
import static org.graalvm.compiler.asm.amd64.AMD64Assembler.VexMoveOp.VMOVD;
import static org.graalvm.compiler.asm.amd64.AMD64Assembler.VexMoveOp.VMOVQ;
import static org.graalvm.compiler.asm.amd64.AVXKind.AVXSize.DWORD;
import static org.graalvm.compiler.asm.amd64.AVXKind.AVXSize.QWORD;
import static org.graalvm.compiler.asm.amd64.AVXKind.AVXSize.XMM;
import static org.graalvm.compiler.asm.amd64.AVXKind.AVXSize.YMM;
import static org.graalvm.compiler.lir.LIRInstruction.OperandFlag.COMPOSITE;
import static org.graalvm.compiler.lir.LIRInstruction.OperandFlag.REG;
import static org.graalvm.compiler.lir.LIRInstruction.OperandFlag.STACK;
import static org.graalvm.compiler.lir.LIRValueUtil.asJavaConstant;
import static org.graalvm.compiler.lir.LIRValueUtil.isJavaConstant;

public final class AMD64Packing {

    private AMD64Packing() { }

    /**
     * This helper function doubles the size of an AMD64Kind.
     *
     * @param kind The kind we want to double.
     * @return An AMD64Kind with twice the size of the input kind.
     */
    private static AMD64Kind twice(AMD64Kind kind) {
        switch (kind) {
            case BYTE:
                return AMD64Kind.WORD;
            case WORD:
                return AMD64Kind.DWORD;
            case DWORD:
                return AMD64Kind.QWORD;
            case SINGLE:
                return AMD64Kind.QWORD;
            default:
                throw GraalError.shouldNotReachHere("Unable to double AMD64Kind '" + kind.toString() + "'");
        }
    }

    private static void reg2addr(CompilationResultBuilder crb, AMD64MacroAssembler masm, AMD64Kind kind, AMD64Address dst, Register src) {
        switch (kind) {
            case BYTE:
                masm.movb(dst, src);
                break;
            case WORD:
                masm.movw(dst, src);
                break;
            case DWORD:
                masm.movl(dst, src);
                break;
            case QWORD:
                masm.movq(dst, src);
                break;
            case SINGLE:
                masm.movl(dst, src);
                break;
            case DOUBLE:
                masm.movq(dst, src);
                break;
        }
    }

    private static void addr2reg(CompilationResultBuilder crb, AMD64MacroAssembler masm, AMD64Kind kind, Register dst, AMD64Address src) {
        switch (kind) {
            case BYTE:
                masm.movb(dst, src);
                break;
            case WORD:
                masm.movw(dst, src);
                break;
            case DWORD:
                masm.movl(dst, src);
                break;
            case QWORD:
                masm.movq(dst, src);
                break;
            case SINGLE:
                masm.movl(dst, src);
                break;
            case DOUBLE:
                masm.movq(dst, src);
                break;
        }
    }

    private static void const2addr(CompilationResultBuilder crb, AMD64MacroAssembler masm, AMD64Kind kind, AMD64Address dst, JavaConstant src) {
        switch (kind) {
            case BYTE:
                masm.movb(dst, src.asInt());
                break;
            case WORD:
                masm.movw(dst, src.asInt());
                break;
            case DWORD:
                masm.movl(dst, src.asInt());
                break;
            case QWORD:
                masm.movlong(dst, src.asLong());
                break;
            case SINGLE:
                masm.movl(dst, floatToRawIntBits(src.asFloat()));
                break;
            case DOUBLE:
                masm.movlong(dst, doubleToRawLongBits(src.asDouble()));
                break;
        }
    }

    /**
     * This method and the three following it are basic move operations, as found in AMD64Move,
     * but operating on raw AMD64Addresses instead of AMD64AddressValues. They're here to bypass the
     * register allocator's requirements when we want to move values to temporary stack space, as
     * seen in PackStackOp, LoadStackOp, and StoreStackOp.
     *
     * Note that this move assumes that dst is always an address, meaning we can't move to
     * registers using this scheme. For now, this is unnecessary - the only time we ever move
     * from a memory location is in StoreStackOp, and the target in that case is always an array.
     * If we want to implement a stack-based unpacking instruction, though, this might be worth
     * considering.
     *
     * @param crb
     * @param masm
     * @param dst The address we want to move to.
     * @param src The value we want to move from. Can be a register, stack variable, or constant.
     * @param srcKind The AMD64Kind of the src value.
     * @param scratch A scratch register to use if necessary. Must be allocated by the caller as an @Temp.
     */
    private static void move(CompilationResultBuilder crb, AMD64MacroAssembler masm, AMD64Address dst, Value src, AMD64Kind srcKind, Register scratch, AMD64Address backupSlot) {
        if (isRegister(src)) {
            reg2addr(crb, masm, srcKind, dst, asRegister(src));
        } else if (isStackSlot(src)) {
            // We move first to the scratch register, then to our destination.
            // TODO: Is there a faster solution?
            reg2addr(crb, masm, srcKind, backupSlot, scratch);
            addr2reg(crb, masm, srcKind, scratch, (AMD64Address) crb.asAddress(src));
            reg2addr(crb, masm, srcKind, dst, scratch);
            addr2reg(crb, masm, srcKind, scratch, backupSlot);
        } else if (isJavaConstant(src)) {
            const2addr(crb, masm, srcKind, dst, asJavaConstant(src));
        }
    }

    private static AMD64Address displace(AMD64Address address, int displacement) {
        return new AMD64Address(address.getBase(), address.getIndex(), address.getScale(), address.getDisplacement() + displacement);
    }

    /**
     * This operation packs a buffer of serialized constants into a vector register.
     */
    public static final class PackConstantsOp extends AMD64LIRInstruction {

        public static final LIRInstructionClass<PackConstantsOp> TYPE = LIRInstructionClass.create(PackConstantsOp.class);

        private final ByteBuffer byteBuffer;

        // We only permit registers. Since constants are already in memory, and vector arithmetic
        // performed only between registers, we shouldn't ever be moving a vector constant to a
        // memory location.
        @Def({REG}) private AllocatableValue result;

        /**
         * Creates a PackConstantsOp.
         * @param result The destination of the operation - in other words, the vector value we are packing.
         * @param byteBuffer The constants we want to pack, serialized in buffer form.
         */
        public PackConstantsOp(AllocatableValue result, ByteBuffer byteBuffer) {
            this(TYPE, result, byteBuffer);
        }

        protected PackConstantsOp(LIRInstructionClass<? extends PackConstantsOp> c, AllocatableValue result, ByteBuffer byteBuffer) {
            super(c);
            this.result = result;
            this.byteBuffer = byteBuffer;
        }

        @Override
        public void emitCode(CompilationResultBuilder crb, AMD64MacroAssembler masm) {
            final PlatformKind pc = result.getPlatformKind();
            final int alignment = pc.getSizeInBytes() / pc.getVectorLength();
            final AMD64Address address = (AMD64Address) crb.recordDataReferenceInCode(byteBuffer.array(), alignment);

            // Based on the size of the constants, we use one of several memory-to-register
            // instructions. The buffer should be aligned to one of these sizes by this point, and
            // we throw an exception otherwise.
            switch (AVXKind.AVXSize.fromBytes(byteBuffer.capacity())) {
                case DWORD:
                    VMOVD.emit(masm, DWORD, asRegister(result), address);
                    break;
                case QWORD:
                    VMOVQ.emit(masm, QWORD, asRegister(result), address);
                    break;
                case XMM:
                    masm.movdqu(asRegister(result), address);
                    break;
                case YMM:
                    assert ((AMD64) masm.target.arch).getFeatures().contains(AMD64.CPUFeature.AVX) : "AVX is unsupported";
                    masm.vmovdqu(asRegister(result), address);
                    break;
                default:
                    throw GraalError.shouldNotReachHere("Unsupported constant size '" + AVXKind.AVXSize.fromBytes(byteBuffer.capacity()));
            }
        }
    }

    private abstract static class StackOp extends AMD64LIRInstruction {
        public static final LIRInstructionClass<StackOp> TYPE = LIRInstructionClass.create(StackOp.class);

        // Scratch register for memory-to-memory moves (see move(...) above).
        @Temp({REG}) AllocatableValue scratch;
        @Temp({STACK}) AllocatableValue packSpace;

        final int numElements;

        StackOp(LIRInstructionClass<? extends StackOp> c, LIRGeneratorTool tool, int numElements) {
            super(c);
            assert numElements > 0 : "vector stack op must have at least one element";
            this.numElements = numElements;

            this.scratch = tool.newVariable(LIRKind.value(AMD64Kind.QWORD));
            this.packSpace = tool.getResult().getFrameMapBuilder().allocateSpillSlot(LIRKind.value(AMD64Kind.V256_BYTE));
        }

        void stackMove(CompilationResultBuilder crb, AMD64MacroAssembler masm, AMD64Kind scalarKind) {
            int stackOffset = 0;
            for (int i = 0; i < numElements;) {
                // Number of array elements to be moved in a single instruction.
                int elements = 1;
                AMD64Kind movKind = scalarKind;

                // While we have room, we try to double the number of elements we pack in a
                // single move. We will never try and move more than a QWORD at once, since
                // that's how large our scratch register is.
                //
                // For example, this code might compress six MOVB instructions instructions to a
                // MOVD and MOVW.
                while (i + elements * 2 <= numElements && movKind.getSizeInBytes() < QWORD.getBytes()) {
                    movKind = twice(movKind);
                    elements *= 2;
                }

                // We compute the addresses we're going to move between.
                final AMD64Address src = displace(getSourceAddress(crb), stackOffset);
                final AMD64Address dst = displace(getDestinationAddress(crb), stackOffset);

                // Perform the move.
                addr2reg(crb, masm, movKind, asRegister(scratch), src);
                reg2addr(crb, masm, movKind, dst, asRegister(scratch));

                // Increment both stack offset and number of elements packed, so that we can
                // proceed with the loop.
                stackOffset += movKind.getSizeInBytes();
                i += elements;
            }
        }

        AMD64Address getSourceAddress(CompilationResultBuilder crb) {
            return (AMD64Address) crb.asAddress(packSpace);
        }

        AMD64Address getDestinationAddress(CompilationResultBuilder crb) {
            return (AMD64Address) crb.asAddress(packSpace);
        }

    }

    /**
     * This operation loads a vector register with contiguous values from an array. It does so with
     * the aid of some temporary stack space to minimize expensive vector instructions.
     */
    public static final class LoadStackOp extends StackOp {
        public static final LIRInstructionClass<LoadStackOp> TYPE = LIRInstructionClass.create(LoadStackOp.class);

        // Our destination should always be a register.
        @Def({REG}) private AllocatableValue result;

        // The array we're loading from.
        @Alive({COMPOSITE}) private AMD64AddressValue input;

        /**
         * Creates a LoadStackOp.
         *
         * @param tool
         * @param result The vector value we want to load a value into.
         * @param input The array value we want to load a value from.
         * @param numElements The number of array elements we are loading.
         */
        public LoadStackOp(LIRGeneratorTool tool, AllocatableValue result, AMD64AddressValue input, int numElements) {
            this(TYPE, tool, result, input, numElements);
        }

        protected LoadStackOp(LIRInstructionClass<? extends LoadStackOp> c, LIRGeneratorTool tool, AllocatableValue result, AMD64AddressValue input, int numElements) {
            super(c, tool, numElements);
            this.result = result;
            this.input = input;
        }

        @Override
        public void emitCode(CompilationResultBuilder crb, AMD64MacroAssembler masm) {
            assert isRegister(scratch) : "scratch must be a register";
            assert isStackSlot(packSpace) : "pack space must be stack slot";

            final AMD64Kind vectorKind = (AMD64Kind) result.getPlatformKind();
            final AMD64Kind scalarKind = vectorKind.getScalar();
            final int sizeInBytes = numElements * scalarKind.getSizeInBytes();

            // If we can fill an entire *MM register, we can just perform a single move and skip more complicated packing logic.
            if (sizeInBytes == YMM.getBytes()) {
                masm.vmovdqu(asRegister(result), input.toAddress());
            } else if (sizeInBytes == XMM.getBytes()) {
                masm.vmovdqu128(asRegister(result), input.toAddress());
            } else {
                final AMD64Address packSpaceAddress = (AMD64Address) crb.asAddress(packSpace);

                stackMove(crb, masm, scalarKind);

                // Write memory into vector register.
                masm.vmovdqu(asRegister(result), packSpaceAddress);
            }
        }

        @Override
        AMD64Address getSourceAddress(CompilationResultBuilder crb) {
            return input.toAddress();
        }
    }

    /**
     * This operation stores a vector register into an array. It does so with
     * the aid of some temporary stack space to minimize expensive vector instructions.
     */
    public static final class StoreStackOp extends StackOp {
        public static final LIRInstructionClass<StoreStackOp> TYPE = LIRInstructionClass.create(StoreStackOp.class);

        // The array we're writing to.
        @Alive({COMPOSITE}) private AMD64AddressValue result;

        // Our input should always be a register.
        @Use({REG}) private AllocatableValue input;

        /**
         * Creates a StoreStackOp.
         *
         * @param tool
         * @param result The array value we're storing the vector into.
         * @param input The vector value we're taking a value from.
         * @param numElements The number of scalar elements in the vector we're storing.
         */
        public StoreStackOp(LIRGeneratorTool tool, AMD64AddressValue result, AllocatableValue input, int numElements) {
            this(TYPE, tool, result, input, numElements);
        }

        protected StoreStackOp(LIRInstructionClass<? extends StoreStackOp> c, LIRGeneratorTool tool, AMD64AddressValue result, AllocatableValue input, int numElements) {
            super(c, tool, numElements);
            this.result = result;
            this.input = input;
        }

        @Override
        public void emitCode(CompilationResultBuilder crb, AMD64MacroAssembler masm) {
            assert isRegister(scratch) : "scratch must be a register";
            assert isStackSlot(packSpace) : "pack space must be stack slot";

            final AMD64Kind vectorKind = (AMD64Kind) input.getPlatformKind();
            final AMD64Kind scalarKind = vectorKind.getScalar();
            final int sizeInBytes = numElements * scalarKind.getSizeInBytes();

            // If we can fill an entire *MM register, we can just perform a single move and skip more complicated packing logic.
            if (sizeInBytes == YMM.getBytes()) {
                masm.vmovdqu(result.toAddress(), asRegister(input));
            } else if (sizeInBytes == XMM.getBytes()) {
                masm.vmovdqu128(result.toAddress(), asRegister(input));
            } else {
                final AMD64Address packSpaceAddress = (AMD64Address) crb.asAddress(packSpace);

                // Write to temporary stack space from the vector register.
                masm.vmovdqu(packSpaceAddress, asRegister(input));

                stackMove(crb, masm, scalarKind);
            }
        }

        @Override
        AMD64Address getDestinationAddress(CompilationResultBuilder crb) {
            return result.toAddress();
        }
    }

    /**
     * This operation will pack a vector register with arbitrary values (e.g. not necessarily
     * contiguous). It does so with the aid of some temporary stack space to minimize expensive
     * vector instructions.
     */
    public static final class PackStackOp extends AMD64LIRInstruction {

        public static final LIRInstructionClass<PackStackOp> TYPE = LIRInstructionClass.create(PackStackOp.class);

        // Our destination should always be a register.
        @Def({REG}) private AllocatableValue result;

        // The values we're going to be packing.
        @Use({REG, STACK}) private AllocatableValue[] values;

        // We need a scratch register for any possible memory-to-memory moves (see move(...) above).
        @Temp({REG}) private AllocatableValue scratch;
        @Temp({STACK}) private AllocatableValue backupSlot;
        @Temp({STACK}) private AllocatableValue packSpace;

        /**
         * Creates a PackStackOp.
         *
         * @param tool
         * @param result The vector value we're packing.
         * @param values The values we want to pack into the result.
         */
        public PackStackOp(LIRGeneratorTool tool, AllocatableValue result, List<AllocatableValue> values) {
            this(TYPE, tool, result, values);
        }

        protected PackStackOp(LIRInstructionClass<? extends PackStackOp> c, LIRGeneratorTool tool, AllocatableValue result, List<AllocatableValue> values) {
            super(c);
            assert !values.isEmpty() : "values to pack for pack op cannot be empty";
            this.result = result;
            this.scratch = tool.newVariable(LIRKind.value(AMD64Kind.QWORD));
            this.values = values.toArray(new AllocatableValue[0]);

            this.backupSlot = tool.getResult().getFrameMapBuilder().allocateSpillSlot(LIRKind.value(AMD64Kind.QWORD));
            this.packSpace = tool.getResult().getFrameMapBuilder().allocateSpillSlot(LIRKind.value(AMD64Kind.V256_BYTE));
        }

        @Override
        public void emitCode(CompilationResultBuilder crb, AMD64MacroAssembler masm) {
            assert isRegister(scratch) : "scratch must be a register";
            assert isStackSlot(packSpace) : "pack space must be stack slot";

            final AMD64Kind vectorKind = (AMD64Kind) result.getPlatformKind();
            final AMD64Kind scalarKind = vectorKind.getScalar();

            final AMD64Address backupSlotAddress = (AMD64Address) crb.asAddress(backupSlot);
            final AMD64Address packSpaceAddress = (AMD64Address) crb.asAddress(packSpace);

            int stackOffset = 0;
            for (AllocatableValue value : values) {
                // Move value into the temporary stack space.
                final AMD64Address target = displace(packSpaceAddress, stackOffset);
                move(crb, masm, target, value, scalarKind, asRegister(scratch), backupSlotAddress);

                // Increment address offset so that we place the next value further into the
                // temporary space.
                stackOffset += scalarKind.getSizeInBytes();
            }

            // Write memory into vector register.
            masm.vmovdqu(asRegister(result), packSpaceAddress);
        }
    }

}
