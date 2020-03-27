/*
 * Copyright (c) 2020, 2020, Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020, Red Hat Inc. All rights reserved.
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

package com.oracle.objectfile.pecoff.cv;

import java.io.PrintStream;

final class CVStringTableRecord extends CVSymbolRecord {

    private final CVSymbolSectionImpl.CVStringTable stringTable;

    CVStringTableRecord(CVSections cvSections, CVSymbolSectionImpl.CVStringTable stringTable) {
        super(cvSections, DEBUG_S_STRINGTABLE);
        this.stringTable = stringTable;
    }

    int add(String string) {
        return stringTable.add(string);
    }

    @Override
    public int computeSize(int pos) {
        return computeContents(null, pos);
    }

    @Override
    public int computeContents(byte[] buffer, int initialPos) {
        int pos = initialPos;
        for (CVSymbolSectionImpl.CVStringTable.StringTableEntry entry : stringTable.values()) {
            pos = CVUtil.putUTF8StringBytes(entry.text, buffer, pos);
        }
        return pos;
    }

    @Override
    public String toString() {
        return String.format("CVStringTableRecord(type=0x%04x pos=0x%06x size=%d)", type, recordStartPosition, stringTable.size());
    }

    @Override
    public void dump(PrintStream out) {
        int idx = 0;
        out.format("%s:\n", this);
        for (CVSymbolSectionImpl.CVStringTable.StringTableEntry entry : stringTable.values()) {
            out.format("%4d 0x%08x %s\n", idx, entry.offset, entry.text);
            idx += 1;
        }
    }
}
