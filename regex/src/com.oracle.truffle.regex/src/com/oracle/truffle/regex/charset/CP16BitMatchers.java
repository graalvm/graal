/*
 * Copyright (c) 2020, 2020, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.oracle.truffle.regex.charset;

import java.util.Iterator;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.regex.tregex.TRegexOptions;
import com.oracle.truffle.regex.tregex.buffer.ByteArrayBuffer;
import com.oracle.truffle.regex.tregex.buffer.CompilationBuffer;
import com.oracle.truffle.regex.tregex.buffer.IntRangesBuffer;
import com.oracle.truffle.regex.tregex.buffer.ObjectArrayBuffer;
import com.oracle.truffle.regex.tregex.matchers.AnyMatcher;
import com.oracle.truffle.regex.tregex.matchers.BitSetMatcher;
import com.oracle.truffle.regex.tregex.matchers.CharMatcher;
import com.oracle.truffle.regex.tregex.matchers.EmptyMatcher;
import com.oracle.truffle.regex.tregex.matchers.HybridBitSetMatcher;
import com.oracle.truffle.regex.tregex.matchers.InvertibleCharMatcher;
import com.oracle.truffle.regex.tregex.matchers.MultiBitSetMatcher;
import com.oracle.truffle.regex.tregex.matchers.ProfilingCharMatcher;
import com.oracle.truffle.regex.tregex.matchers.RangeListMatcher;
import com.oracle.truffle.regex.tregex.matchers.RangeTreeMatcher;
import com.oracle.truffle.regex.tregex.matchers.SingleCharMatcher;
import com.oracle.truffle.regex.tregex.matchers.SingleRangeMatcher;
import com.oracle.truffle.regex.tregex.matchers.TwoCharMatcher;
import com.oracle.truffle.regex.util.CompilationFinalBitSet;

/**
 * Helper class for converting 16-bit code point sets to {@link CharMatcher}s.
 */
public class CP16BitMatchers {

    /**
     * Create a new {@link CharMatcher} from the given code point set. The given set must contain
     * either none or all of the code points above {@code 0xffff}. Code points above {@code 0xffff}
     * are cut off.
     */
    public static CharMatcher createMatcher(CodePointSet cps, CompilationBuffer compilationBuffer) {
        if (cps.matchesMinAndMax() || cps.inverseIsSameHighByte16Bit()) {
            return createMatcher(cps.createInverse(), compilationBuffer, true, true);
        }
        return createMatcher(cps, compilationBuffer, false, true);
    }

    public static int highByte(int c) {
        return (c >> Byte.SIZE) & 0xff;
    }

    public static int lowByte(int c) {
        return c & 0xff;
    }

    private static CharMatcher createMatcher(CodePointSet cps, CompilationBuffer compilationBuffer, boolean inverse, boolean tryHybrid) {
        if (cps.numberOf16BitRanges() == 0) {
            return EmptyMatcher.create(inverse);
        }
        if (cps.matchesEverything()) {
            return AnyMatcher.create(inverse);
        }
        if (cps.matchesSingleChar()) {
            assert cps.getMin() <= Character.MAX_VALUE;
            return SingleCharMatcher.create(inverse, cps.getMin());
        }
        if (cps.valueCountEquals(2)) {
            assert cps.getMax() <= Character.MAX_VALUE;
            return TwoCharMatcher.create(inverse, cps.getMin(), cps.getMax());
        }
        int size = cps.numberOf16BitRanges();
        if (size == 1) {
            return SingleRangeMatcher.create(inverse, cps.getLo16(0), cps.getHi16(0));
        }
        if (preferRangeListMatcherOverBitSetMatcher(cps, size)) {
            return RangeListMatcher.create(inverse, toCharArray(cps, size));
        }
        InvertibleCharMatcher bitSetMatcher = convertToBitSetMatcher(cps, compilationBuffer, inverse);
        if (bitSetMatcher != null) {
            return bitSetMatcher;
        }
        CharMatcher charMatcher;
        if (size > 100) {
            charMatcher = MultiBitSetMatcher.fromCodePointSet(inverse, cps);
        } else if (tryHybrid) {
            charMatcher = createHybridMatcher(cps, compilationBuffer, inverse);
        } else {
            if (size <= 10) {
                charMatcher = RangeListMatcher.create(inverse, toCharArray(cps, size));
            } else {
                assert size <= 100;
                charMatcher = RangeTreeMatcher.fromRanges(inverse, toCharArray(cps, size));
            }
        }
        return ProfilingCharMatcher.create(createMatcher(cps.createIntersection(Constants.BYTE_RANGE, compilationBuffer), compilationBuffer, inverse, false), charMatcher);
    }

    private static boolean preferRangeListMatcherOverBitSetMatcher(CodePointSet cps, int size) {
        // for up to two ranges, RangeListMatcher is faster than any BitSet matcher
        // also, up to four single character checks are still faster than a bit set
        return size <= 2 || cps.valueCountMax(4);
    }

    private static InvertibleCharMatcher convertToBitSetMatcher(CodePointSet cps, CompilationBuffer compilationBuffer, boolean inverse) {
        int highByte = highByte(cps.getMin());
        if (highByte(cps.getHi16(cps.size16() - 1)) != highByte) {
            return null;
        }
        CompilationFinalBitSet bs = compilationBuffer.getByteSizeBitSet();
        for (int i = 0; i < cps.numberOf16BitRanges(); i++) {
            assert highByte(cps.getLo16(i)) == highByte && highByte(cps.getHi16(i)) == highByte;
            bs.setRange(lowByte(cps.getLo16(i)), lowByte(cps.getHi16(i)));
        }
        return BitSetMatcher.create(inverse, highByte, bs.copy());
    }

    private static CharMatcher createHybridMatcher(CodePointSet cps, CompilationBuffer compilationBuffer, boolean inverse) {
        int size = cps.size16();
        assert size >= 1;
        IntRangesBuffer rest = compilationBuffer.getIntRangesBuffer1();
        ByteArrayBuffer highBytes = compilationBuffer.getByteArrayBuffer();
        ObjectArrayBuffer<CompilationFinalBitSet> bitSets = compilationBuffer.getObjectBuffer1();
        // index of lowest range on current plane
        int lowestOCP = 0;
        boolean lowestRangeCanBeDeleted = !rangeCrossesPlanes(cps, 0);
        int curPlane = highByte(cps.getHi16(0));
        for (int i = 0; i < size; i++) {
            if (highByte(cps.getLo16(i)) != curPlane) {
                if (isOverBitSetConversionThreshold(i - lowestOCP)) {
                    addBitSet(cps, rest, highBytes, bitSets, curPlane, lowestOCP, i, lowestRangeCanBeDeleted);
                } else {
                    cps.appendRangesTo(rest, lowestOCP, i);
                }
                curPlane = highByte(cps.getLo16(i));
                lowestOCP = i;
                lowestRangeCanBeDeleted = !rangeCrossesPlanes(cps, i);
            }
            if (highByte(cps.getHi16(i)) != curPlane) {
                if (lowestOCP != i) {
                    if (isOverBitSetConversionThreshold((i + 1) - lowestOCP)) {
                        addBitSet(cps, rest, highBytes, bitSets, curPlane, lowestOCP, i + 1, lowestRangeCanBeDeleted);
                        lowestRangeCanBeDeleted = highByte(cps.getHi16(i)) - highByte(cps.getLo16(i)) == 1;
                    } else {
                        cps.appendRangesTo(rest, lowestOCP, i);
                        lowestRangeCanBeDeleted = !rangeCrossesPlanes(cps, i);
                    }
                } else {
                    lowestRangeCanBeDeleted = !rangeCrossesPlanes(cps, i);
                }
                curPlane = highByte(cps.getHi16(i));
                lowestOCP = i;
            }
        }
        if (isOverBitSetConversionThreshold(size - lowestOCP)) {
            addBitSet(cps, rest, highBytes, bitSets, curPlane, lowestOCP, size, lowestRangeCanBeDeleted);
        } else {
            cps.appendRangesTo(rest, lowestOCP, size);
        }
        if (highBytes.length() == 0) {
            assert rest.length() == size * 2;
            return createMatcher(cps, compilationBuffer, inverse, false);
        }
        CharMatcher restMatcher = createMatcher(CodePointSet.create(rest), compilationBuffer, false, false);
        return HybridBitSetMatcher.create(inverse, highBytes.toArray(), bitSets.toArray(new CompilationFinalBitSet[bitSets.length()]), restMatcher);
    }

    private static boolean isOverBitSetConversionThreshold(int nRanges) {
        return nRanges >= TRegexOptions.TRegexRangeToBitSetConversionThreshold;
    }

    private static void addBitSet(CodePointSet ranges, IntRangesBuffer rest, ByteArrayBuffer highBytes, ObjectArrayBuffer<CompilationFinalBitSet> bitSets,
                    int curPlane, int lowestOCP, int i, boolean lowestRangeCanBeDeleted) {
        highBytes.add((byte) curPlane);
        bitSets.add(convertToBitSet(ranges, curPlane, lowestOCP, i));
        if (!lowestRangeCanBeDeleted) {
            ranges.addRangeTo(rest, lowestOCP);
        }
    }

    private static boolean rangeCrossesPlanes(CodePointSet ranges, int i) {
        return highByte(ranges.getLo16(i)) != highByte(ranges.getHi16(i));
    }

    private static CompilationFinalBitSet convertToBitSet(CodePointSet ranges, int highByte, int iMinArg, int iMaxArg) {
        assert iMaxArg - iMinArg > 1;
        CompilationFinalBitSet bs;
        int iMax = iMaxArg;
        if (rangeCrossesPlanes(ranges, iMaxArg - 1)) {
            bs = new CompilationFinalBitSet(0xff);
            iMax--;
            bs.setRange(lowByte(ranges.getLo16(iMaxArg - 1)), 0xff);
        } else {
            bs = new CompilationFinalBitSet(lowByte(ranges.getHi16(iMaxArg - 1)));
        }
        int iMin = iMinArg;
        if (rangeCrossesPlanes(ranges, iMinArg)) {
            assert highByte(ranges.getHi16(iMinArg)) == highByte;
            iMin++;
            bs.setRange(0, lowByte(ranges.getHi16(iMinArg)));
        }
        for (int i = iMin; i < iMax; i++) {
            assert highByte(ranges.getLo16(i)) == highByte && highByte(ranges.getHi16(i)) == highByte;
            bs.setRange(lowByte(ranges.getLo16(i)), lowByte(ranges.getHi16(i)));
        }
        return bs;
    }

    private static char[] toCharArray(CodePointSet cps, int size) {
        int length = size * 2;
        Iterator<Range> it = cps.iterator16Bit();
        char[] arr = new char[length];
        int i = 0;
        while (it.hasNext()) {
            Range r = it.next();
            assert r.lo <= Character.MAX_VALUE && r.hi <= Character.MAX_VALUE;
            arr[i] = (char) r.lo;
            arr[i + 1] = (char) r.hi;
            i += 2;
        }
        return arr;
    }

    @TruffleBoundary
    public static String rangesToString(char[] ranges) {
        return rangesToString(ranges, false);
    }

    @TruffleBoundary
    public static String rangesToString(char[] ranges, boolean numeric) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ranges.length; i += 2) {
            if (numeric) {
                sb.append("[").append((int) ranges[i]).append("-").append((int) ranges[i + 1]).append("]");
            } else {
                sb.append(Range.toString(ranges[i], ranges[i + 1]));
            }
        }
        return sb.toString();
    }
}
