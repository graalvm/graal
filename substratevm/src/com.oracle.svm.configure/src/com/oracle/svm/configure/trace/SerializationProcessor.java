/*
 * Copyright (c) 2019, 2020, Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020 Alibaba Group Holding Limited. All Rights Reserved.
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
package com.oracle.svm.configure.trace;

import com.oracle.svm.configure.config.SerializationConfiguration;

import java.util.List;
import java.util.Map;

public class SerializationProcessor extends AbstractProcessor {
    private final SerializationConfiguration serializationConfiguration;

    public SerializationProcessor(SerializationConfiguration serializationConfiguration) {
        this.serializationConfiguration = serializationConfiguration;
    }

    public SerializationConfiguration getSerializationConfiguration() {
        return serializationConfiguration;
    }

    @Override
    void processEntry(Map<String, ?> entry) {
        boolean invalidResult = Boolean.FALSE.equals(entry.get("result"));
        if (invalidResult) {
            return;
        }
        String function = (String) entry.get("function");
        List<?> args = (List<?>) entry.get("args");
        if ("ObjectStreamClass.<init>".equals(function)) {
            expectSize(args, 5);
            if ((args.get(1) instanceof List) && (args.get(2) instanceof List)) {
                List<?> paramTypes = (List<?>) args.get(1);
                List<?> checkedExceptions = (List<?>) args.get(2);
                serializationConfiguration.add((String) args.get(0), paramTypes.toArray(new String[0]), checkedExceptions.toArray(new String[0]), (Integer) args.get(3), (String) args.get(4));
            } else {
                throw new IllegalArgumentException("The second and third arguments should all be List.");
            }
        }
    }
}
