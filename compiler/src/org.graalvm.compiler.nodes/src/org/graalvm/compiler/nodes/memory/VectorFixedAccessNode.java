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
package org.graalvm.compiler.nodes.memory;

import org.graalvm.compiler.core.common.type.Stamp;
import org.graalvm.compiler.graph.IterableNodeType;
import org.graalvm.compiler.graph.NodeClass;
import org.graalvm.compiler.nodeinfo.InputType;
import org.graalvm.compiler.nodeinfo.NodeInfo;
import org.graalvm.compiler.nodes.FixedWithNextNode;
import org.graalvm.compiler.nodes.extended.GuardingNode;
import org.graalvm.compiler.nodes.memory.address.AddressNode;
import org.graalvm.word.LocationIdentity;

import static org.graalvm.compiler.nodeinfo.NodeCycles.CYCLES_8;
import static org.graalvm.compiler.nodeinfo.NodeSize.SIZE_16;

@NodeInfo(cycles = CYCLES_8, size = SIZE_16)
public abstract class VectorFixedAccessNode extends FixedWithNextNode implements VectorAccess, IterableNodeType  {
    public static final NodeClass<VectorFixedAccessNode> TYPE = NodeClass.create(VectorFixedAccessNode.class);

    @OptionalInput(InputType.Guard) protected GuardingNode guard;

    @Input(InputType.Association) AddressNode address;
    protected final LocationIdentity[] locations;

    protected boolean nullCheck;
    protected BarrierType barrierType;

    @Override
    public AddressNode getAddress() {
        return address;
    }

    public void setAddress(AddressNode address) {
        updateUsages(this.address, address);
        this.address = address;
    }

    @Override
    public LocationIdentity[] getLocationIdentities() {
        return locations;
    }

    public boolean getNullCheck() {
        return nullCheck;
    }

    @Override
    public boolean canNullCheck() {
        return nullCheck;
    }

    protected VectorFixedAccessNode(NodeClass<? extends VectorFixedAccessNode> c, AddressNode address, LocationIdentity[] locations, Stamp stamp) {
        this(c, address, locations, stamp, BarrierType.NONE);
    }

    protected VectorFixedAccessNode(NodeClass<? extends VectorFixedAccessNode> c, AddressNode address, LocationIdentity[] locations, Stamp stamp, BarrierType barrierType) {
        this(c, address, locations, stamp, null, barrierType, false);
    }

    protected VectorFixedAccessNode(NodeClass<? extends VectorFixedAccessNode> c, AddressNode address, LocationIdentity[] locations, Stamp stamp, GuardingNode guard, BarrierType barrierType, boolean nullCheck) {
        super(c, stamp);
        this.address = address;
        this.locations = locations;
        this.guard = guard;
        this.barrierType = barrierType;
        this.nullCheck = nullCheck;
    }

    @Override
    public GuardingNode getGuard() {
        return guard;
    }

    @Override
    public void setGuard(GuardingNode guard) {
        updateUsagesInterface(this.guard, guard);
        this.guard = guard;
    }

    @Override
    public BarrierType getBarrierType() {
        return barrierType;
    }

    public int numElements() {
        return locations.length;
    }
}
