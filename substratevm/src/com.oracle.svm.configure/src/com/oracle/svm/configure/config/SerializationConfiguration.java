/*
 * Copyright (c) 2020, 2020, Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020, 2020, Alibaba Group Holding Limited. All rights reserved.
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
package com.oracle.svm.configure.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.oracle.svm.configure.json.JsonPrintable;
import com.oracle.svm.configure.json.JsonWriter;
import org.graalvm.nativeimage.impl.RuntimeSerializationSupport;

public class SerializationConfiguration implements JsonPrintable, RuntimeSerializationSupport {

    private final Set<SerializationConfigurationType> serializations = ConcurrentHashMap.newKeySet();

    @Override
    public void printJson(JsonWriter writer) throws IOException {
        writer.append('[').indent();
        String prefix = "";
        List<SerializationConfigurationType> list = new ArrayList<>(serializations);
        list.sort(Comparator.comparing(SerializationConfigurationType::getQualifiedJavaName)
                        .thenComparing(SerializationConfigurationType::getQualifiedCustomTargetConstructorJavaName));
        for (SerializationConfigurationType type : list) {
            writer.append(prefix).newline();
            type.printJson(writer);
            prefix = ",";
        }
        writer.unindent().newline();
        writer.append(']').newline();
    }

    @Override
    public void register(Class<?>... classes) {
        for (Class<?> clazz : classes) {
            register(clazz, null);
        }
    }

    @Override
    public void register(Class<?> clazz, Class<?> customTargetConstructorClazz) {
        register(clazz.getName(), customTargetConstructorClazz == null ? null : customTargetConstructorClazz.getName());
    }

    @Override
    public void register(String className, String customTargetConstructorClassName) {
        String convertedClassName = SignatureUtil.toInternalClassName(className);
        String convertedCustomTargetConstructorClassName = SignatureUtil.toInternalClassName(customTargetConstructorClassName);
        serializations.add(new SerializationConfigurationType(convertedClassName, convertedCustomTargetConstructorClassName));
    }
}
