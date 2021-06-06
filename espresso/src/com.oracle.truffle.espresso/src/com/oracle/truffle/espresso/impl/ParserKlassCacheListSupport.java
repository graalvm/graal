/*
 * Copyright (c) 2021, 2021, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.espresso.impl;

import com.oracle.truffle.espresso.descriptors.Symbol;
import com.oracle.truffle.espresso.descriptors.Types;
import com.oracle.truffle.espresso.meta.EspressoError;
import com.oracle.truffle.espresso.runtime.JavaVersion;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class ParserKlassCacheListSupport {

    private List<Symbol<Symbol.Type>> typeList;
    private final Types types;

    public ParserKlassCacheListSupport(Types types) {
        this.types = types;
    }

    public void processFile(Path path) {
        if (path.toString().isEmpty()) {
            return;
        }
        try {
            List<Symbol<Symbol.Type>> typeList = Files.readAllLines(path)
                    .stream()
                    .filter(s -> !s.isEmpty() && !s.startsWith("//"))
                    .map(types::getOrCreate)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            setTypeList(typeList);
        } catch (IOException e) {
            throw EspressoError.unexpected("ParserKlassCacheListProvider failed reading the class list from the specified file", e);
        }
    }

    public List<Symbol<Symbol.Type>> getTypeList(JavaVersion version) {
        if (typeList == null) {
            if (version.java8OrEarlier()) {
                setDefaultJava8ClassList();
            } else {
                setDefaultJava11ClassList();
            }
        }
        return typeList;
    }

    private void setTypeList(List<Symbol<Symbol.Type>> typeList) {
        this.typeList = Collections.unmodifiableList(typeList);
    }

    private void setDefaultJava8ClassList() {
        typeList = Collections.unmodifiableList(Arrays.asList(
                types.getOrCreate("Lcom/oracle/truffle/espresso/polyglot/ArityException;"),
                types.getOrCreate("Lcom/oracle/truffle/espresso/polyglot/ExceptionType;"),
                types.getOrCreate("Lcom/oracle/truffle/espresso/polyglot/ForeignException;"),
                types.getOrCreate("Lcom/oracle/truffle/espresso/polyglot/InteropException;"),
                types.getOrCreate("Lcom/oracle/truffle/espresso/polyglot/InvalidArrayIndexException;"),
                types.getOrCreate("Lcom/oracle/truffle/espresso/polyglot/Polyglot;"),
                types.getOrCreate("Lcom/oracle/truffle/espresso/polyglot/UnknownIdentifierException;"),
                types.getOrCreate("Lcom/oracle/truffle/espresso/polyglot/UnsupportedMessageException;"),
                types.getOrCreate("Lcom/oracle/truffle/espresso/polyglot/UnsupportedTypeException;"),
                types.getOrCreate("Ljava/io/BufferedInputStream;"),
                types.getOrCreate("Ljava/io/BufferedOutputStream;"),
                types.getOrCreate("Ljava/io/BufferedReader;"),
                types.getOrCreate("Ljava/io/BufferedWriter;"),
                types.getOrCreate("Ljava/io/Closeable;"),
                types.getOrCreate("Ljava/io/DefaultFileSystem;"),
                types.getOrCreate("Ljava/io/ExpiringCache$1;"),
                types.getOrCreate("Ljava/io/ExpiringCache$Entry;"),
                types.getOrCreate("Ljava/io/ExpiringCache;"),
                types.getOrCreate("Ljava/io/File$PathStatus;"),
                types.getOrCreate("Ljava/io/File;"),
                types.getOrCreate("Ljava/io/FileDescriptor$1;"),
                types.getOrCreate("Ljava/io/FileDescriptor;"),
                types.getOrCreate("Ljava/io/FileInputStream$1;"),
                types.getOrCreate("Ljava/io/FileInputStream;"),
                types.getOrCreate("Ljava/io/FileOutputStream;"),
                types.getOrCreate("Ljava/io/FilePermission$1;"),
                types.getOrCreate("Ljava/io/FilePermission;"),
                types.getOrCreate("Ljava/io/FilePermissionCollection;"),
                types.getOrCreate("Ljava/io/FileReader;"),
                types.getOrCreate("Ljava/io/FileSystem;"),
                types.getOrCreate("Ljava/io/FilterInputStream;"),
                types.getOrCreate("Ljava/io/FilterOutputStream;"),
                types.getOrCreate("Ljava/io/Flushable;"),
                types.getOrCreate("Ljava/io/InputStream;"),
                types.getOrCreate("Ljava/io/InputStreamReader;"),
                types.getOrCreate("Ljava/io/ObjectStreamField;"),
                types.getOrCreate("Ljava/io/OutputStream;"),
                types.getOrCreate("Ljava/io/OutputStreamWriter;"),
                types.getOrCreate("Ljava/io/PrintStream;"),
                types.getOrCreate("Ljava/io/Reader;"),
                types.getOrCreate("Ljava/io/Serializable;"),
                types.getOrCreate("Ljava/io/UnixFileSystem;"),
                types.getOrCreate("Ljava/io/Writer;"),
                types.getOrCreate("Ljava/lang/AbstractMethodError;"),
                types.getOrCreate("Ljava/lang/AbstractStringBuilder;"),
                types.getOrCreate("Ljava/lang/Appendable;"),
                types.getOrCreate("Ljava/lang/ArithmeticException;"),
                types.getOrCreate("Ljava/lang/ArrayIndexOutOfBoundsException;"),
                types.getOrCreate("Ljava/lang/ArrayStoreException;"),
                types.getOrCreate("Ljava/lang/AssertionStatusDirectives;"),
                types.getOrCreate("Ljava/lang/AutoCloseable;"),
                types.getOrCreate("Ljava/lang/Boolean;"),
                types.getOrCreate("Ljava/lang/BootstrapMethodError;"),
                types.getOrCreate("Ljava/lang/Byte;"),
                types.getOrCreate("Ljava/lang/Character;"),
                types.getOrCreate("Ljava/lang/CharacterData;"),
                types.getOrCreate("Ljava/lang/CharacterDataLatin1;"),
                types.getOrCreate("Ljava/lang/CharSequence;"),
                types.getOrCreate("Ljava/lang/Class$1;"),
                types.getOrCreate("Ljava/lang/Class$3;"),
                types.getOrCreate("Ljava/lang/Class$AnnotationData;"),
                types.getOrCreate("Ljava/lang/Class$Atomic;"),
                types.getOrCreate("Ljava/lang/Class$MethodArray;"),
                types.getOrCreate("Ljava/lang/Class$ReflectionData;"),
                types.getOrCreate("Ljava/lang/Class;"),
                types.getOrCreate("Ljava/lang/ClassCastException;"),
                types.getOrCreate("Ljava/lang/ClassCircularityError;"),
                types.getOrCreate("Ljava/lang/ClassFormatError;"),
                types.getOrCreate("Ljava/lang/ClassLoader$3;"),
                types.getOrCreate("Ljava/lang/ClassLoader$NativeLibrary;"),
                types.getOrCreate("Ljava/lang/ClassLoader$ParallelLoaders;"),
                types.getOrCreate("Ljava/lang/ClassLoader;"),
                types.getOrCreate("Ljava/lang/ClassNotFoundException;"),
                types.getOrCreate("Ljava/lang/ClassValue$ClassValueMap;"),
                types.getOrCreate("Ljava/lang/Cloneable;"),
                types.getOrCreate("Ljava/lang/CloneNotSupportedException;"),
                types.getOrCreate("Ljava/lang/Comparable;"),
                types.getOrCreate("Ljava/lang/Double;"),
                types.getOrCreate("Ljava/lang/Enum;"),
                types.getOrCreate("Ljava/lang/Error;"),
                types.getOrCreate("Ljava/lang/Exception;"),
                types.getOrCreate("Ljava/lang/ExceptionInInitializerError;"),
                types.getOrCreate("Ljava/lang/Float;"),
                types.getOrCreate("Ljava/lang/IllegalAccessError;"),
                types.getOrCreate("Ljava/lang/IllegalArgumentException;"),
                types.getOrCreate("Ljava/lang/IllegalMonitorStateException;"),
                types.getOrCreate("Ljava/lang/IncompatibleClassChangeError;"),
                types.getOrCreate("Ljava/lang/IndexOutOfBoundsException;"),
                types.getOrCreate("Ljava/lang/InstantiationError;"),
                types.getOrCreate("Ljava/lang/InstantiationException;"),
                types.getOrCreate("Ljava/lang/Integer;"),
                types.getOrCreate("Ljava/lang/InternalError;"),
                types.getOrCreate("Ljava/lang/InterruptedException;"),
                types.getOrCreate("Ljava/lang/invoke/CallSite;"),
                types.getOrCreate("Ljava/lang/invoke/LambdaForm;"),
                types.getOrCreate("Ljava/lang/invoke/MemberName;"),
                types.getOrCreate("Ljava/lang/invoke/MethodHandle;"),
                types.getOrCreate("Ljava/lang/invoke/MethodHandleNatives;"),
                types.getOrCreate("Ljava/lang/invoke/MethodHandles;"),
                types.getOrCreate("Ljava/lang/invoke/MethodType;"),
                types.getOrCreate("Ljava/lang/Iterable;"),
                types.getOrCreate("Ljava/lang/LinkageError;"),
                types.getOrCreate("Ljava/lang/Long;"),
                types.getOrCreate("Ljava/lang/management/MemoryUsage;"),
                types.getOrCreate("Ljava/lang/management/ThreadInfo;"),
                types.getOrCreate("Ljava/lang/Math;"),
                types.getOrCreate("Ljava/lang/NegativeArraySizeException;"),
                types.getOrCreate("Ljava/lang/NoClassDefFoundError;"),
                types.getOrCreate("Ljava/lang/NoSuchFieldError;"),
                types.getOrCreate("Ljava/lang/NoSuchFieldException;"),
                types.getOrCreate("Ljava/lang/NoSuchMethodError;"),
                types.getOrCreate("Ljava/lang/NoSuchMethodException;"),
                types.getOrCreate("Ljava/lang/NullPointerException;"),
                types.getOrCreate("Ljava/lang/Number;"),
                types.getOrCreate("Ljava/lang/OutOfMemoryError;"),
                types.getOrCreate("Ljava/lang/Readable;"),
                types.getOrCreate("Ljava/lang/ref/Finalizer$FinalizerThread;"),
                types.getOrCreate("Ljava/lang/ref/Finalizer;"),
                types.getOrCreate("Ljava/lang/ref/FinalReference;"),
                types.getOrCreate("Ljava/lang/ref/PhantomReference;"),
                types.getOrCreate("Ljava/lang/ref/Reference$1;"),
                types.getOrCreate("Ljava/lang/ref/Reference$Lock;"),
                types.getOrCreate("Ljava/lang/ref/Reference$ReferenceHandler;"),
                types.getOrCreate("Ljava/lang/ref/Reference;"),
                types.getOrCreate("Ljava/lang/ref/ReferenceQueue$Lock;"),
                types.getOrCreate("Ljava/lang/ref/ReferenceQueue$Null;"),
                types.getOrCreate("Ljava/lang/ref/ReferenceQueue;"),
                types.getOrCreate("Ljava/lang/ref/SoftReference;"),
                types.getOrCreate("Ljava/lang/ref/WeakReference;"),
                types.getOrCreate("Ljava/lang/reflect/AccessibleObject;"),
                types.getOrCreate("Ljava/lang/reflect/AnnotatedElement;"),
                types.getOrCreate("Ljava/lang/reflect/Array;"),
                types.getOrCreate("Ljava/lang/reflect/Constructor;"),
                types.getOrCreate("Ljava/lang/reflect/Executable;"),
                types.getOrCreate("Ljava/lang/reflect/Field;"),
                types.getOrCreate("Ljava/lang/reflect/GenericDeclaration;"),
                types.getOrCreate("Ljava/lang/reflect/InvocationTargetException;"),
                types.getOrCreate("Ljava/lang/reflect/Member;"),
                types.getOrCreate("Ljava/lang/reflect/Method;"),
                types.getOrCreate("Ljava/lang/reflect/Modifier;"),
                types.getOrCreate("Ljava/lang/reflect/Parameter;"),
                types.getOrCreate("Ljava/lang/reflect/ReflectAccess;"),
                types.getOrCreate("Ljava/lang/reflect/ReflectPermission;"),
                types.getOrCreate("Ljava/lang/reflect/Type;"),
                types.getOrCreate("Ljava/lang/ReflectiveOperationException;"),
                types.getOrCreate("Ljava/lang/Runnable;"),
                types.getOrCreate("Ljava/lang/Runtime;"),
                types.getOrCreate("Ljava/lang/RuntimeException;"),
                types.getOrCreate("Ljava/lang/RuntimePermission;"),
                types.getOrCreate("Ljava/lang/SecurityException;"),
                types.getOrCreate("Ljava/lang/Short;"),
                types.getOrCreate("Ljava/lang/Shutdown;"),
                types.getOrCreate("Ljava/lang/Shutdown$Lock;"),
                types.getOrCreate("Ljava/lang/StackOverflowError;"),
                types.getOrCreate("Ljava/lang/StackTraceElement;"),
                types.getOrCreate("Ljava/lang/String$CaseInsensitiveComparator;"),
                types.getOrCreate("Ljava/lang/String;"),
                types.getOrCreate("Ljava/lang/StringBuffer;"),
                types.getOrCreate("Ljava/lang/StringBuilder;"),
                types.getOrCreate("Ljava/lang/StringCoding$StringDecoder;"),
                types.getOrCreate("Ljava/lang/StringCoding$StringEncoder;"),
                types.getOrCreate("Ljava/lang/StringCoding;"),
                types.getOrCreate("Ljava/lang/StringIndexOutOfBoundsException;"),
                types.getOrCreate("Ljava/lang/System$2;"),
                types.getOrCreate("Ljava/lang/System;"),
                types.getOrCreate("Ljava/lang/SystemClassLoaderAction;"),
                types.getOrCreate("Ljava/lang/Terminator$1;"),
                types.getOrCreate("Ljava/lang/Terminator;"),
                types.getOrCreate("Ljava/lang/Thread$UncaughtExceptionHandler;"),
                types.getOrCreate("Ljava/lang/Thread;"),
                types.getOrCreate("Ljava/lang/ThreadDeath;"),
                types.getOrCreate("Ljava/lang/ThreadGroup;"),
                types.getOrCreate("Ljava/lang/ThreadLocal$ThreadLocalMap$Entry;"),
                types.getOrCreate("Ljava/lang/ThreadLocal$ThreadLocalMap;"),
                types.getOrCreate("Ljava/lang/ThreadLocal;"),
                types.getOrCreate("Ljava/lang/Throwable;"),
                types.getOrCreate("Ljava/lang/UnsatisfiedLinkError;"),
                types.getOrCreate("Ljava/lang/UnsupportedClassVersionError;"),
                types.getOrCreate("Ljava/lang/UnsupportedOperationException;"),
                types.getOrCreate("Ljava/lang/VerifyError;"),
                types.getOrCreate("Ljava/lang/VirtualMachineError;"),
                types.getOrCreate("Ljava/lang/Void;"),
                types.getOrCreate("Ljava/net/Parts;"),
                types.getOrCreate("Ljava/net/URI$Parser;"),
                types.getOrCreate("Ljava/net/URI;"),
                types.getOrCreate("Ljava/net/URL;"),
                types.getOrCreate("Ljava/net/URLClassLoader$1;"),
                types.getOrCreate("Ljava/net/URLClassLoader$7;"),
                types.getOrCreate("Ljava/net/URLClassLoader;"),
                types.getOrCreate("Ljava/net/URLConnection;"),
                types.getOrCreate("Ljava/net/URLStreamHandler;"),
                types.getOrCreate("Ljava/net/URLStreamHandlerFactory;"),
                types.getOrCreate("Ljava/nio/Bits$1;"),
                types.getOrCreate("Ljava/nio/Bits;"),
                types.getOrCreate("Ljava/nio/Buffer;"),
                types.getOrCreate("Ljava/nio/ByteBuffer;"),
                types.getOrCreate("Ljava/nio/ByteBufferAsLongBufferL;"),
                types.getOrCreate("Ljava/nio/ByteOrder;"),
                types.getOrCreate("Ljava/nio/CharBuffer;"),
                types.getOrCreate("Ljava/nio/charset/Charset;"),
                types.getOrCreate("Ljava/nio/charset/CharsetDecoder;"),
                types.getOrCreate("Ljava/nio/charset/CharsetEncoder;"),
                types.getOrCreate("Ljava/nio/charset/CoderResult$1;"),
                types.getOrCreate("Ljava/nio/charset/CoderResult$2;"),
                types.getOrCreate("Ljava/nio/charset/CoderResult$Cache;"),
                types.getOrCreate("Ljava/nio/charset/CoderResult;"),
                types.getOrCreate("Ljava/nio/charset/CodingErrorAction;"),
                types.getOrCreate("Ljava/nio/charset/spi/CharsetProvider;"),
                types.getOrCreate("Ljava/nio/DirectByteBuffer;"),
                types.getOrCreate("Ljava/nio/file/attribute/BasicFileAttributes;"),
                types.getOrCreate("Ljava/nio/file/attribute/PosixFileAttributes;"),
                types.getOrCreate("Ljava/nio/file/FileSystem;"),
                types.getOrCreate("Ljava/nio/file/Path;"),
                types.getOrCreate("Ljava/nio/file/spi/FileSystemProvider;"),
                types.getOrCreate("Ljava/nio/file/Watchable;"),
                types.getOrCreate("Ljava/nio/HeapByteBuffer;"),
                types.getOrCreate("Ljava/nio/HeapCharBuffer;"),
                types.getOrCreate("Ljava/nio/LongBuffer;"),
                types.getOrCreate("Ljava/nio/MappedByteBuffer;"),
                types.getOrCreate("Ljava/security/AccessControlContext;"),
                types.getOrCreate("Ljava/security/AccessController;"),
                types.getOrCreate("Ljava/security/AllPermission;"),
                types.getOrCreate("Ljava/security/BasicPermission;"),
                types.getOrCreate("Ljava/security/BasicPermissionCollection;"),
                types.getOrCreate("Ljava/security/cert/Certificate;"),
                types.getOrCreate("Ljava/security/CodeSource;"),
                types.getOrCreate("Ljava/security/Guard;"),
                types.getOrCreate("Ljava/security/Permission;"),
                types.getOrCreate("Ljava/security/PermissionCollection;"),
                types.getOrCreate("Ljava/security/Permissions;"),
                types.getOrCreate("Ljava/security/Principal;"),
                types.getOrCreate("Ljava/security/PrivilegedAction;"),
                types.getOrCreate("Ljava/security/PrivilegedActionException;"),
                types.getOrCreate("Ljava/security/PrivilegedExceptionAction;"),
                types.getOrCreate("Ljava/security/ProtectionDomain$2;"),
                types.getOrCreate("Ljava/security/ProtectionDomain$JavaSecurityAccessImpl;"),
                types.getOrCreate("Ljava/security/ProtectionDomain$Key;"),
                types.getOrCreate("Ljava/security/ProtectionDomain;"),
                types.getOrCreate("Ljava/security/SecureClassLoader;"),
                types.getOrCreate("Ljava/security/UnresolvedPermission;"),
                types.getOrCreate("Ljava/time/chrono/ChronoLocalDate;"),
                types.getOrCreate("Ljava/time/chrono/ChronoLocalDateTime;"),
                types.getOrCreate("Ljava/time/chrono/ChronoZonedDateTime;"),
                types.getOrCreate("Ljava/time/Duration;"),
                types.getOrCreate("Ljava/time/Instant;"),
                types.getOrCreate("Ljava/time/LocalDate;"),
                types.getOrCreate("Ljava/time/LocalDateTime;"),
                types.getOrCreate("Ljava/time/LocalTime;"),
                types.getOrCreate("Ljava/time/temporal/Temporal;"),
                types.getOrCreate("Ljava/time/temporal/TemporalAccessor;"),
                types.getOrCreate("Ljava/time/temporal/TemporalAdjuster;"),
                types.getOrCreate("Ljava/time/temporal/TemporalAmount;"),
                types.getOrCreate("Ljava/time/ZonedDateTime;"),
                types.getOrCreate("Ljava/time/ZoneId;"),
                types.getOrCreate("Ljava/util/AbstractCollection;"),
                types.getOrCreate("Ljava/util/AbstractList;"),
                types.getOrCreate("Ljava/util/AbstractMap;"),
                types.getOrCreate("Ljava/util/AbstractSet;"),
                types.getOrCreate("Ljava/util/ArrayList;"),
                types.getOrCreate("Ljava/util/Arrays;"),
                types.getOrCreate("Ljava/util/BitSet;"),
                types.getOrCreate("Ljava/util/Collection;"),
                types.getOrCreate("Ljava/util/Collections$EmptyList;"),
                types.getOrCreate("Ljava/util/Collections$EmptyMap;"),
                types.getOrCreate("Ljava/util/Collections$EmptySet;"),
                types.getOrCreate("Ljava/util/Collections$SetFromMap;"),
                types.getOrCreate("Ljava/util/Collections$SynchronizedCollection;"),
                types.getOrCreate("Ljava/util/Collections$SynchronizedSet;"),
                types.getOrCreate("Ljava/util/Collections$UnmodifiableCollection;"),
                types.getOrCreate("Ljava/util/Collections$UnmodifiableList;"),
                types.getOrCreate("Ljava/util/Collections$UnmodifiableRandomAccessList;"),
                types.getOrCreate("Ljava/util/Collections;"),
                types.getOrCreate("Ljava/util/Comparator;"),
                types.getOrCreate("Ljava/util/concurrent/atomic/AtomicInteger;"),
                types.getOrCreate("Ljava/util/concurrent/atomic/AtomicLong;"),
                types.getOrCreate("Ljava/util/concurrent/atomic/AtomicReferenceFieldUpdater$AtomicReferenceFieldUpdaterImpl$1;"),
                types.getOrCreate("Ljava/util/concurrent/atomic/AtomicReferenceFieldUpdater$AtomicReferenceFieldUpdaterImpl;"),
                types.getOrCreate("Ljava/util/concurrent/atomic/AtomicReferenceFieldUpdater;"),
                types.getOrCreate("Ljava/util/concurrent/ConcurrentHashMap$CollectionView;"),
                types.getOrCreate("Ljava/util/concurrent/ConcurrentHashMap$CounterCell;"),
                types.getOrCreate("Ljava/util/concurrent/ConcurrentHashMap$EntrySetView;"),
                types.getOrCreate("Ljava/util/concurrent/ConcurrentHashMap$KeySetView;"),
                types.getOrCreate("Ljava/util/concurrent/ConcurrentHashMap$Node;"),
                types.getOrCreate("Ljava/util/concurrent/ConcurrentHashMap$Segment;"),
                types.getOrCreate("Ljava/util/concurrent/ConcurrentHashMap$ValuesView;"),
                types.getOrCreate("Ljava/util/concurrent/ConcurrentHashMap;"),
                types.getOrCreate("Ljava/util/concurrent/ConcurrentMap;"),
                types.getOrCreate("Ljava/util/concurrent/locks/Lock;"),
                types.getOrCreate("Ljava/util/concurrent/locks/ReentrantLock;"),
                types.getOrCreate("Ljava/util/Date;"),
                types.getOrCreate("Ljava/util/Dictionary;"),
                types.getOrCreate("Ljava/util/Enumeration;"),
                types.getOrCreate("Ljava/util/HashMap$Node;"),
                types.getOrCreate("Ljava/util/HashMap$TreeNode;"),
                types.getOrCreate("Ljava/util/HashMap;"),
                types.getOrCreate("Ljava/util/Hashtable$Entry;"),
                types.getOrCreate("Ljava/util/Hashtable$EntrySet;"),
                types.getOrCreate("Ljava/util/Hashtable$Enumerator;"),
                types.getOrCreate("Ljava/util/Hashtable;"),
                types.getOrCreate("Ljava/util/Iterator;"),
                types.getOrCreate("Ljava/util/LinkedHashMap$Entry;"),
                types.getOrCreate("Ljava/util/LinkedHashMap;"),
                types.getOrCreate("Ljava/util/List;"),
                types.getOrCreate("Ljava/util/Locale$Cache;"),
                types.getOrCreate("Ljava/util/Locale$LocaleKey;"),
                types.getOrCreate("Ljava/util/Locale;"),
                types.getOrCreate("Ljava/util/Map$Entry;"),
                types.getOrCreate("Ljava/util/Map;"),
                types.getOrCreate("Ljava/util/Objects;"),
                types.getOrCreate("Ljava/util/Properties;"),
                types.getOrCreate("Ljava/util/RandomAccess;"),
                types.getOrCreate("Ljava/util/Set;"),
                types.getOrCreate("Ljava/util/Stack;"),
                types.getOrCreate("Ljava/util/StringTokenizer;"),
                types.getOrCreate("Ljava/util/Vector;"),
                types.getOrCreate("Ljava/util/WeakHashMap$Entry;"),
                types.getOrCreate("Ljava/util/WeakHashMap$KeySet;"),
                types.getOrCreate("Ljava/util/WeakHashMap;"),
                types.getOrCreate("Ljava/util/zip/ZipConstants;"),
                types.getOrCreate("Ljava/util/zip/ZipFile$1;"),
                types.getOrCreate("Ljava/util/zip/ZipFile;"),
                types.getOrCreate("Ljdk/internal/util/StaticProperty;"),
                types.getOrCreate("Lsun/launcher/LauncherHelper;"),
                types.getOrCreate("Lsun/management/ManagementFactory;"),
                types.getOrCreate("Lsun/misc/Cleaner;"),
                types.getOrCreate("Lsun/misc/InnocuousThread;"),
                types.getOrCreate("Lsun/misc/JavaIOFileDescriptorAccess;"),
                types.getOrCreate("Lsun/misc/JavaLangAccess;"),
                types.getOrCreate("Lsun/misc/JavaLangRefAccess;"),
                types.getOrCreate("Lsun/misc/JavaNetAccess;"),
                types.getOrCreate("Lsun/misc/JavaNioAccess;"),
                types.getOrCreate("Lsun/misc/JavaSecurityAccess;"),
                types.getOrCreate("Lsun/misc/JavaSecurityProtectionDomainAccess;"),
                types.getOrCreate("Lsun/misc/JavaUtilZipFileAccess;"),
                types.getOrCreate("Lsun/misc/Launcher$AppClassLoader$1;"),
                types.getOrCreate("Lsun/misc/Launcher$AppClassLoader;"),
                types.getOrCreate("Lsun/misc/Launcher$ExtClassLoader$1;"),
                types.getOrCreate("Lsun/misc/Launcher$ExtClassLoader;"),
                types.getOrCreate("Lsun/misc/Launcher$Factory;"),
                types.getOrCreate("Lsun/misc/Launcher;"),
                types.getOrCreate("Lsun/misc/MetaIndex;"),
                types.getOrCreate("Lsun/misc/NativeSignalHandler;"),
                types.getOrCreate("Lsun/misc/OSEnvironment;"),
                types.getOrCreate("Lsun/misc/Perf$GetPerfAction;"),
                types.getOrCreate("Lsun/misc/Perf;"),
                types.getOrCreate("Lsun/misc/PerfCounter$CoreCounters;"),
                types.getOrCreate("Lsun/misc/PerfCounter;"),
                types.getOrCreate("Lsun/misc/Resource;"),
                types.getOrCreate("Lsun/misc/SharedSecrets;"),
                types.getOrCreate("Lsun/misc/Signal;"),
                types.getOrCreate("Lsun/misc/SignalHandler;"),
                types.getOrCreate("Lsun/misc/Unsafe;"),
                types.getOrCreate("Lsun/misc/URLClassPath$3;"),
                types.getOrCreate("Lsun/misc/URLClassPath$FileLoader$1;"),
                types.getOrCreate("Lsun/misc/URLClassPath$FileLoader;"),
                types.getOrCreate("Lsun/misc/URLClassPath$JarLoader;"),
                types.getOrCreate("Lsun/misc/URLClassPath$Loader;"),
                types.getOrCreate("Lsun/misc/URLClassPath;"),
                types.getOrCreate("Lsun/misc/Version;"),
                types.getOrCreate("Lsun/misc/VM;"),
                types.getOrCreate("Lsun/net/util/IPAddressUtil;"),
                types.getOrCreate("Lsun/net/util/URLUtil;"),
                types.getOrCreate("Lsun/net/www/MessageHeader;"),
                types.getOrCreate("Lsun/net/www/ParseUtil;"),
                types.getOrCreate("Lsun/net/www/protocol/file/FileURLConnection;"),
                types.getOrCreate("Lsun/net/www/protocol/file/Handler;"),
                types.getOrCreate("Lsun/net/www/protocol/jar/Handler;"),
                types.getOrCreate("Lsun/net/www/URLConnection;"),
                types.getOrCreate("Lsun/nio/ByteBuffered;"),
                types.getOrCreate("Lsun/nio/ch/DirectBuffer;"),
                types.getOrCreate("Lsun/nio/cs/ArrayDecoder;"),
                types.getOrCreate("Lsun/nio/cs/ArrayEncoder;"),
                types.getOrCreate("Lsun/nio/cs/FastCharsetProvider;"),
                types.getOrCreate("Lsun/nio/cs/HistoricallyNamedCharset;"),
                types.getOrCreate("Lsun/nio/cs/StandardCharsets$Aliases;"),
                types.getOrCreate("Lsun/nio/cs/StandardCharsets$Cache;"),
                types.getOrCreate("Lsun/nio/cs/StandardCharsets$Classes;"),
                types.getOrCreate("Lsun/nio/cs/StandardCharsets;"),
                types.getOrCreate("Lsun/nio/cs/StreamDecoder;"),
                types.getOrCreate("Lsun/nio/cs/StreamEncoder;"),
                types.getOrCreate("Lsun/nio/cs/Unicode;"),
                types.getOrCreate("Lsun/nio/cs/UTF_8$Decoder;"),
                types.getOrCreate("Lsun/nio/cs/UTF_8$Encoder;"),
                types.getOrCreate("Lsun/nio/cs/UTF_8;"),
                types.getOrCreate("Lsun/nio/fs/AbstractFileSystemProvider;"),
                types.getOrCreate("Lsun/nio/fs/AbstractPath;"),
                types.getOrCreate("Lsun/nio/fs/DefaultFileSystemProvider;"),
                types.getOrCreate("Lsun/nio/fs/LinuxFileSystem;"),
                types.getOrCreate("Lsun/nio/fs/LinuxFileSystemProvider;"),
                types.getOrCreate("Lsun/nio/fs/UnixFileAttributes;"),
                types.getOrCreate("Lsun/nio/fs/UnixFileStoreAttributes;"),
                types.getOrCreate("Lsun/nio/fs/UnixFileSystem;"),
                types.getOrCreate("Lsun/nio/fs/UnixFileSystemProvider;"),
                types.getOrCreate("Lsun/nio/fs/UnixMountEntry;"),
                types.getOrCreate("Lsun/nio/fs/UnixNativeDispatcher$1;"),
                types.getOrCreate("Lsun/nio/fs/UnixNativeDispatcher;"),
                types.getOrCreate("Lsun/nio/fs/UnixPath;"),
                types.getOrCreate("Lsun/nio/fs/Util;"),
                types.getOrCreate("Lsun/reflect/annotation/AnnotationType;"),
                types.getOrCreate("Lsun/reflect/ConstantPool;"),
                types.getOrCreate("Lsun/reflect/ConstructorAccessor;"),
                types.getOrCreate("Lsun/reflect/ConstructorAccessorImpl;"),
                types.getOrCreate("Lsun/reflect/DelegatingClassLoader;"),
                types.getOrCreate("Lsun/reflect/DelegatingConstructorAccessorImpl;"),
                types.getOrCreate("Lsun/reflect/generics/repository/AbstractRepository;"),
                types.getOrCreate("Lsun/reflect/generics/repository/ClassRepository;"),
                types.getOrCreate("Lsun/reflect/generics/repository/GenericDeclRepository;"),
                types.getOrCreate("Lsun/reflect/LangReflectAccess;"),
                types.getOrCreate("Lsun/reflect/MagicAccessorImpl;"),
                types.getOrCreate("Lsun/reflect/MethodAccessor;"),
                types.getOrCreate("Lsun/reflect/MethodAccessorImpl;"),
                types.getOrCreate("Lsun/reflect/misc/ReflectUtil;"),
                types.getOrCreate("Lsun/reflect/NativeConstructorAccessorImpl;"),
                types.getOrCreate("Lsun/reflect/Reflection;"),
                types.getOrCreate("Lsun/reflect/ReflectionFactory$1;"),
                types.getOrCreate("Lsun/reflect/ReflectionFactory$GetReflectionFactoryAction;"),
                types.getOrCreate("Lsun/reflect/ReflectionFactory;"),
                types.getOrCreate("Lsun/security/action/GetPropertyAction;"),
                types.getOrCreate("Lsun/security/util/Debug;"),
                types.getOrCreate("Lsun/util/locale/BaseLocale$Cache;"),
                types.getOrCreate("Lsun/util/locale/BaseLocale$Key;"),
                types.getOrCreate("Lsun/util/locale/BaseLocale;"),
                types.getOrCreate("Lsun/util/locale/LocaleObjectCache$CacheEntry;"),
                types.getOrCreate("Lsun/util/locale/LocaleObjectCache;"),
                types.getOrCreate("Lsun/util/locale/LocaleUtils;"),
                types.getOrCreate("Lsun/util/PreHashedMap;")
        ));
    }

    private void setDefaultJava11ClassList() {
        // TODO
        typeList = Collections.unmodifiableList(new ArrayList<>());
    }
}
