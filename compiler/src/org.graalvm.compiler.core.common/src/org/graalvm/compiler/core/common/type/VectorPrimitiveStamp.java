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
package org.graalvm.compiler.core.common.type;

import java.nio.ByteBuffer;
import java.util.Objects;

import org.graalvm.compiler.debug.GraalError;

import jdk.vm.ci.meta.Constant;
import jdk.vm.ci.meta.JavaKind;
import jdk.vm.ci.meta.MemoryAccessProvider;
import jdk.vm.ci.meta.MetaAccessProvider;
import jdk.vm.ci.meta.ResolvedJavaType;
import jdk.vm.ci.meta.SerializableConstant;

public abstract class VectorPrimitiveStamp extends ArithmeticStamp {

    private final PrimitiveStamp scalar;
    private final int elementCount;

    protected VectorPrimitiveStamp(PrimitiveStamp scalar, int elementCount, ArithmeticOpTable ops) {
        super(ops);
        this.scalar = scalar;
        this.elementCount = elementCount;
    }

    @Override
    public void accept(Visitor v) {
        v.visitInt(elementCount * scalar.getBits());
    }

    public int getElementCount() {
        return elementCount;
    }

    public PrimitiveStamp getScalar() {
        return scalar;
    }

    @Override
    public SerializableConstant deserialize(ByteBuffer buffer) {
        throw GraalError.shouldNotReachHere("deserialization not supported for integer vector");
    }

    @Override
    public ResolvedJavaType javaType(MetaAccessProvider metaAccess) {
        throw GraalError.shouldNotReachHere("vector has no java type");
    }

    @Override
    public JavaKind getStackKind() {
        return JavaKind.Illegal;
    }

    @Override
    public Constant readConstant(MemoryAccessProvider provider, Constant base, long displacement) {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        VectorPrimitiveStamp that = (VectorPrimitiveStamp) o;
        return elementCount == that.elementCount &&
                scalar.equals(that.scalar);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), scalar, elementCount);
    }

    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder();
        if (hasValues()) {
            str.append("v");
            str.append(getElementCount());
            str.append(" of ");
            str.append(getScalar().toString());
        } else {
            str.append("<empty>");
        }
        return str.toString();
    }
}
