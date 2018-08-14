suite = {
    "mxversion": "5.196.3",
    "name": "substratevm",
    "version" : "1.0.0-rc12",
    "release" : False,
    "url" : "https://github.com/oracle/graal/tree/master/substratevm",

    "developer" : {
        "name" : "SubstrateVM developers",
        "email" : "graal-dev@openjdk.java.net",
        "organization" : "Graal",
        "organizationUrl" : "http://openjdk.java.net/projects/graal",
    },
    "scm" : {
        "url" : "https://github.com/oracle/graal",
        "read" : "https://github.com/oracle/graal.git",
        "write" : "git@github.com:oracle/graal.git",
    },

    "defaultLicense" : "GPLv2-CPE",

    "versionConflictResolution": "latest",

    "imports": {
        "suites": [
            {
                "name": "compiler",
                "subdir": True,
                "urls" : [
                    {"url" : "https://curio.ssw.jku.at/nexus/content/repositories/snapshots", "kind" : "binary"},
                ]
            },
            {
                "name": "regex",
                "subdir": True,
                "urls" : [
                    {"url" : "https://curio.ssw.jku.at/nexus/content/repositories/snapshots", "kind" : "binary"},
                ]
            },
        ]
    },

    "projects": {
        "com.oracle.svm.core": {
            "subDir": "src",
            "sourceDirs": [
                "src",
                "headers",
            ],
            "dependencies": [
                "sdk:GRAAL_SDK",
                "compiler:GRAAL",
            ],
            "javaCompliance": "8+",
            "checkstyleVersion" : "8.8",
            "annotationProcessors": [
                "compiler:GRAAL_NODEINFO_PROCESSOR",
                "compiler:GRAAL_REPLACEMENTS_PROCESSOR",
                "compiler:GRAAL_OPTIONS_PROCESSOR",
            ],
            "workingSets": "SVM",
        },

        "com.oracle.svm.core.jdk8": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": ["com.oracle.svm.core"],
            "javaCompliance": "8",
            "checkstyle": "com.oracle.svm.core",
            "workingSets": "SVM",
        },

        "com.oracle.svm.core.jdk9": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": ["com.oracle.svm.core"],
            "imports" : [
                "jdk.internal.misc",
                "jdk.internal.perf",
            ],
            "javaCompliance": "9+",
            "multiReleaseJarVersion": "9",
            "checkstyle": "com.oracle.svm.core",
            "workingSets": "SVM",
        },

        "com.oracle.svm.core.jdk9.posix": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "com.oracle.svm.core.jdk9",
                "com.oracle.svm.core.posix"
                ],
            "imports" : [
                "jdk.internal.misc",
                "jdk.internal.perf",
            ],
            "javaCompliance": "9+",
            "multiReleaseJarVersion": "9",
            "checkstyle": "com.oracle.svm.core",
            "workingSets": "SVM",
        },

        "com.oracle.svm.core.genscavenge": {
            "subDir": "src",
            "sourceDirs": [
                "src",
            ],
            "dependencies": [
                "com.oracle.svm.core.graal",
            ],
            "checkstyle": "com.oracle.svm.core",
            "javaCompliance": "8+",
            "annotationProcessors": [
                "compiler:GRAAL_NODEINFO_PROCESSOR",
                "compiler:GRAAL_REPLACEMENTS_PROCESSOR",
                "compiler:GRAAL_OPTIONS_PROCESSOR",
            ],
            "workingSets": "SVM",
        },

        "com.oracle.svm.core.graal": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "com.oracle.svm.core",
                "compiler:GRAAL",
            ],
            "checkstyle": "com.oracle.svm.core",
            "javaCompliance": "8+",
            "annotationProcessors": [
                "compiler:GRAAL_NODEINFO_PROCESSOR",
                "compiler:GRAAL_REPLACEMENTS_PROCESSOR",
                "compiler:GRAAL_OPTIONS_PROCESSOR",
            ],
            "workingSets": "SVM",
        },

        "com.oracle.svm.core.graal.amd64": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "com.oracle.svm.core.graal",
            ],
            "checkstyle": "com.oracle.svm.core",
            "javaCompliance": "8+",
            "annotationProcessors": [
                "compiler:GRAAL_NODEINFO_PROCESSOR",
                "compiler:GRAAL_REPLACEMENTS_PROCESSOR",
                "compiler:GRAAL_OPTIONS_PROCESSOR",
            ],
            "workingSets": "SVM",
        },

        "com.oracle.svm.core.posix": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": ["com.oracle.svm.core"],
            "checkstyle": "com.oracle.svm.core",
            "javaCompliance": "8+",
            "annotationProcessors": [
                "compiler:GRAAL_NODEINFO_PROCESSOR",
                "compiler:GRAAL_REPLACEMENTS_PROCESSOR",
                "compiler:GRAAL_OPTIONS_PROCESSOR",
            ],
            "workingSets": "SVM",
            "findbugs": "false",
        },

        "com.oracle.svm.core.windows": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "com.oracle.svm.hosted",
            ],
            "checkstyle": "com.oracle.svm.core",
            "javaCompliance": "8+",
            "annotationProcessors": [
                "compiler:GRAAL_NODEINFO_PROCESSOR",
                "compiler:GRAAL_REPLACEMENTS_PROCESSOR",
                "compiler:GRAAL_OPTIONS_PROCESSOR",
            ],
            "workingSets": "SVM",
            "findbugs": "false",
        },

        "com.oracle.graal.pointsto": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "compiler:GRAAL",
            ],
            "checkstyle": "com.oracle.graal.pointsto",
            "javaCompliance": "8+",
            "checkstyleVersion" : "8.8",
            "annotationProcessors": [
                "compiler:GRAAL_NODEINFO_PROCESSOR",
                "compiler:GRAAL_REPLACEMENTS_PROCESSOR",
                "compiler:GRAAL_OPTIONS_PROCESSOR",
            ],
            "workingSets": "SVM",
        },

        "com.oracle.svm.hosted": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "com.oracle.objectfile",
                "com.oracle.svm.core.graal.amd64",
                "com.oracle.graal.pointsto",
            ],
            "javaCompliance": "8+",
            "checkstyleVersion" : "8.8",
            "annotationProcessors": [
                "compiler:GRAAL_NODEINFO_PROCESSOR",
                "compiler:GRAAL_REPLACEMENTS_PROCESSOR",
                "compiler:GRAAL_OPTIONS_PROCESSOR",
            ],
            "workingSets": "SVM",
        },

        "com.oracle.svm.native.libchelper": {
            "subDir": "src",
            "native": "static_lib",
            "os_arch": {
                "solaris": {
                    "<others>": {
                        "ignore": "solaris is not supported",
                    },
                },
                "windows": {
                    "<others>": {
                        "cflags": ["-Zi", "-O2", "-D_LITTLE_ENDIAN"],
                    },
                },
                "<others>": {
                    "sparcv9": {
                        "ignore": "sparcv9 is not supported",
                    },
                    "<others>": {
                        "cflags": ["-g", "-fPIC", "-O2", "-D_LITTLE_ENDIAN"],
                    },
                },
            },
        },

        "com.oracle.svm.native.strictmath": {
            "subDir": "src",
            "native": "static_lib",
            "os_arch": {
                "solaris": {
                    "<others>": {
                        "ignore": "solaris is not supported",
                    },
                },
                "windows": {
                    "<others>": {
                        "cflags": ["-O1", "-D_LITTLE_ENDIAN"],
                    },
                },
                "<others>": {
                    "sparcv9": {
                        "ignore": "sparcv9 is not supported",
                    },
                    "<others>": {
                        "cflags": ["-fPIC", "-O1", "-D_LITTLE_ENDIAN"],
                    },
                },
            },
        },

        "com.oracle.svm.native.jvm": {
            "subDir": "src",
            "native": "static_lib",
            "os_arch" : {
                "windows": {
                    "amd64" : {
                        "cflags": ["-MD", "-Zi", "-O2"],
                    },
                },
                "<others>": {
                    "<others>": {
                        "ignore": "only windows is supported",
                    },
                },
            },
        },

        "com.oracle.svm.jni": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "com.oracle.svm.hosted",
            ],
            "checkstyle": "com.oracle.svm.core",
            "workingSets": "SVM",
            "annotationProcessors": [
                "compiler:GRAAL_NODEINFO_PROCESSOR",
                "compiler:GRAAL_REPLACEMENTS_PROCESSOR",
                "compiler:GRAAL_OPTIONS_PROCESSOR",
            ],
            "javaCompliance": "8+",
            "findbugs": "false",
        },

        "com.oracle.svm.driver": {
            "subDir": "src",
            "sourceDirs": [
                "src",
                "resources"
            ],
            "dependencies": [
                "com.oracle.svm.graal",
                "com.oracle.svm.reflect",
            ],
            "checkstyle": "com.oracle.svm.driver",
            "checkstyleVersion" : "8.8",
            "workingSets": "SVM",
            "annotationProcessors": [
                "compiler:GRAAL_NODEINFO_PROCESSOR",
                "compiler:GRAAL_REPLACEMENTS_PROCESSOR",
                "compiler:GRAAL_OPTIONS_PROCESSOR",
            ],
            "javaCompliance": "8+",
            "findbugs": "false",
        },

        "com.oracle.svm.junit": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "com.oracle.svm.reflect",
                "mx:JUNIT_TOOL",
            ],
            "checkstyle": "com.oracle.svm.core",
            "workingSets": "SVM",
            "annotationProcessors": [
                "compiler:GRAAL_OPTIONS_PROCESSOR",
            ],
            "javaCompliance": "8+",
            "findbugs": "false",
        },

        "com.oracle.svm.jmh": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "com.oracle.svm.reflect",
                "mx:JMH_1_21",
            ],
            "checkstyle": "com.oracle.svm.core",
            "workingSets": "SVM",
            "annotationProcessors": [
                "compiler:GRAAL_OPTIONS_PROCESSOR",
            ],
            "javaCompliance": "8+",
            "findbugs": "false",
        },

        "com.oracle.svm.test": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "mx:JUNIT_TOOL",
                "sdk:GRAAL_SDK",
            ],
            "checkstyle": "com.oracle.svm.core",
            "workingSets": "SVM",
            "annotationProcessors": [
                "compiler:GRAAL_OPTIONS_PROCESSOR",
            ],
            "javaCompliance": "8+",
            "findbugs": "false",
        },

        "com.oracle.svm.reflect": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "com.oracle.svm.hosted",
            ],
            "checkstyle": "com.oracle.svm.core",
            "workingSets": "SVM",
            "annotationProcessors": [
                "compiler:GRAAL_OPTIONS_PROCESSOR",
            ],
            "javaCompliance": "8+",
            "findbugs": "false",
        },

        "com.oracle.svm.tutorial" : {
            "subDir": "src",
            "sourceDirs" : ["src"],
            "dependencies" : ["com.oracle.svm.core"],
            "checkstyle" : "com.oracle.svm.truffle",
            "javaCompliance" : "8+",
            "annotationProcessors" : [
                "compiler:GRAAL_NODEINFO_PROCESSOR",
                "compiler:GRAAL_REPLACEMENTS_PROCESSOR",
                "compiler:GRAAL_OPTIONS_PROCESSOR",
            ],
            "workingSets" : "SVM",
            "findbugs" : "false",
        },

        "com.oracle.objectfile" : {
            "subDir": "src",
            "sourceDirs" : ["src"],
            "dependencies" : [],
            "checkstyle" : "com.oracle.svm.hosted",
            "javaCompliance" : "8+",
            "annotationProcessors" : ["compiler:GRAAL_OPTIONS_PROCESSOR"],
            "workingSets" : "SVM",
            "findbugs" : "false",
        },

        "com.oracle.svm.graal": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "com.oracle.svm.hosted",
                "truffle:TRUFFLE_API",
            ],
            "checkstyle": "com.oracle.svm.hosted",
            "javaCompliance": "8+",
            "annotationProcessors": [
                "compiler:GRAAL_NODEINFO_PROCESSOR",
                "compiler:GRAAL_REPLACEMENTS_PROCESSOR",
                "compiler:GRAAL_OPTIONS_PROCESSOR",
            ],
        },

        "com.oracle.svm.thirdparty": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "com.oracle.svm.core",
            ],
            "checkstyle": "com.oracle.svm.truffle",
            "javaCompliance": "8+",
            "annotationProcessors": [
                "compiler:GRAAL_NODEINFO_PROCESSOR",
                "compiler:GRAAL_REPLACEMENTS_PROCESSOR",
                "compiler:GRAAL_OPTIONS_PROCESSOR",
            ],
            "workingSets": "SVM",
        },

        "com.oracle.svm.truffle": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "com.oracle.svm.graal",
            ],
            "checkstyle": "com.oracle.svm.truffle",
            "javaCompliance": "8+",
            "checkstyleVersion" : "8.8",
            "annotationProcessors": [
                "compiler:GRAAL_SERVICEPROVIDER_PROCESSOR",
                "compiler:GRAAL_NODEINFO_PROCESSOR",
                "compiler:GRAAL_REPLACEMENTS_PROCESSOR",
                "compiler:GRAAL_OPTIONS_PROCESSOR",
            ],
            "workingSets": "SVM",
        },

        "com.oracle.svm.truffle.nfi": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "com.oracle.svm.truffle",
                "truffle:TRUFFLE_NFI",
                "com.oracle.svm.core.posix", # Posix dependency is temporary, see GR-11751
            ],
            "checkstyle": "com.oracle.svm.truffle",
            "javaCompliance": "8+",
            "annotationProcessors": [
                "truffle:TRUFFLE_DSL_PROCESSOR",
            ],
            "workingSets": "SVM",
        },

        "com.oracle.svm.libffi": {
            "subDir": "src",
            "native": True,
            "vpath": True,
            "results": [
                "libffi.a",
                "include/ffi.h",
                "include/ffitarget.h",
                "include/trufflenfi.h",
                "include/svm_libffi.h",
            ],
            "buildEnv": {
                "LIBFFI_SRC": "<path:truffle:LIBFFI>",
                "TRUFFLE_NFI": "<path:truffle:TRUFFLE_NFI_NATIVE>",
                "ARCH": "<arch>",
                "OS": "<os>"
            },
            "buildDependencies": [
                "truffle:TRUFFLE_NFI_NATIVE",
            ],
        },

        "com.oracle.svm.jline": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "com.oracle.svm.core",
                "truffle:JLINE",
            ],
            "checkstyle": "com.oracle.svm.truffle",
            "javaCompliance": "8+",
            "annotationProcessors": [
                "compiler:GRAAL_NODEINFO_PROCESSOR",
                "compiler:GRAAL_REPLACEMENTS_PROCESSOR",
                "compiler:GRAAL_OPTIONS_PROCESSOR",
            ],
            "workingSets": "SVM",
            "findbugs": "false",
        },

        "com.oracle.svm.polyglot": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "generatedDependencies": [
                "com.oracle.svm.graal",
            ],
            "checkstyle": "com.oracle.svm.truffle",
            "javaCompliance": "8+",
            "annotationProcessors": [
                "compiler:GRAAL_NODEINFO_PROCESSOR",
                "compiler:GRAAL_REPLACEMENTS_PROCESSOR",
                "compiler:GRAAL_OPTIONS_PROCESSOR",
            ],
            "workingSets": "SVM",
            "findbugs": "false",
        },

        "org.graalvm.polyglot.nativeapi" : {
            "subDir": "src",
            "sourceDirs" : [
                "src",
                "resources",
            ],
            "dependencies" : [
                "sdk:GRAAL_SDK",
                "com.oracle.svm.hosted",
            ],
            "buildDependencies" : [
                "org.graalvm.polyglot.nativeapi.native",
            ],
            "checkstyle": "org.graalvm.polyglot.nativeapi",
            "checkstyleVersion" : "8.8",
            "javaCompliance" : "8+",
            "annotationProcessors" : [
                "compiler:GRAAL_NODEINFO_PROCESSOR",
                "compiler:GRAAL_REPLACEMENTS_PROCESSOR",
                "compiler:GRAAL_OPTIONS_PROCESSOR",
            ],
            "workingSets" : "SVM",
            "findbugs": "false",
        },

        "org.graalvm.polyglot.nativeapi.native" : {
            "subDir" : "src",
            "native" : True,
            "vpath": True,
            "results" : ["<os>-<arch>/polyglot-nativeapi.o"],
            "buildEnv": {
                "ARCH": "<arch>",
                "OS": "<os>"
            },
            "os_arch": {
                "solaris": {
                    "<others>": {
                        "ignore": "solaris is not supported",
                    },
                },
                "windows": {
                    "<others>": {
                        "ignore": "windows is not supported",  # necessary until GR-12705 is resolved
                    },
                },
                "<others>": {
                    "sparcv9": {
                        "ignore": "sparcv9 is not supported",
                    },
                    "<others>": {
                        "ignore": False,
                    },
                },
            },
        },
    },

    "distributions": {
        #
        # External Distributions
        #
        "SVM": {
            "subDir": "src",
            "description" : "SubstrateVM image builder components",
            "dependencies": [
                "com.oracle.svm.graal",  # necessary until Truffle is fully supported on Windows (GR-7941)
                "com.oracle.svm.truffle",  # necessary until Truffle is fully supported on Windows (GR-7941)
                "com.oracle.svm.hosted",
                "com.oracle.svm.truffle.nfi",
                "com.oracle.svm.core",
                "com.oracle.svm.core.jdk8",
                "com.oracle.svm.core.jdk9",
                "com.oracle.svm.core.posix",
                "com.oracle.svm.core.jdk9.posix",
                "com.oracle.svm.core.windows",
                "com.oracle.svm.core.genscavenge",
            ],
            "overlaps" : [
                "SVM_CORE", "SVM_HOSTED",
            ],
            "distDependencies": [
                "SVM_HOSTED_NATIVE",
                "sdk:GRAAL_SDK",
                "OBJECTFILE",
                "POINTSTO",
                "mx:JUNIT_TOOL",
                "truffle:TRUFFLE_NFI",
                "compiler:GRAAL",
            ],
        },

        "SVM_CORE": {
            "subDir": "src",
            "dependencies": [
                "com.oracle.svm.core",
                "com.oracle.svm.core.graal",
                "com.oracle.svm.core.graal.amd64",
                "com.oracle.svm.core.genscavenge",
            ],
            "distDependencies": [
                "sdk:GRAAL_SDK",
                "compiler:GRAAL",
            ],
            "exclude": [
            ],
            "maven": False
        },

        "SVM_HOSTED": {
            "subDir": "src",
            "dependencies": [
                "com.oracle.svm.truffle",
            ],
            "distDependencies": [
                "sdk:GRAAL_SDK",
                "compiler:GRAAL",
                "OBJECTFILE",
                "POINTSTO",
            ],
            "overlaps" : [
                "SVM_CORE",
            ],
            "exclude": [
            ],
            "maven": False
        },

        "LIBRARY_SUPPORT": {
            "subDir": "src",
            "description" : "SubstrateVM basic library-support components",
            "dependencies": [
                "com.oracle.svm.jni",
                "com.oracle.svm.jline",
                "com.oracle.svm.junit",
                "com.oracle.svm.polyglot",
                "com.oracle.svm.thirdparty",
            ],
            "distDependencies": [
                "sdk:GRAAL_SDK",
                "SVM",
                "OBJECTFILE",
                "compiler:GRAAL",
            ],
            "exclude": [
                "truffle:JLINE",
            ],
        },

        "OBJECTFILE": {
            "subDir": "src",
            "description" : "SubstrateVM object file writing library",
            "dependencies": [
                "com.oracle.objectfile"
            ],
        },

        #
        # Native Projects
        #
        "SVM_HOSTED_NATIVE": {
            "dependencies": [
                "com.oracle.svm.native.libchelper",
                "com.oracle.svm.native.strictmath",
                "com.oracle.svm.native.jvm",
                "com.oracle.svm.libffi"
            ],
            "native": True,
            "platformDependent" : True,
            "platforms" : [
                "linux-amd64",
                "darwin-amd64",
                "windows-amd64",
            ],
            "description" : "SubstrateVM image builder native components",
            "relpath": True,
            "auto_prefix": True,
            "output": "clibraries",
            "maven": True
        },

        #
        # Internal Distributions
        #
        "SVM_DRIVER": {
            "subDir": "src",
            "description" : "SubstrateVM native-image building tool",
            "mainClass": "com.oracle.svm.driver.NativeImage",
            "dependencies": [
                "com.oracle.svm.driver",
            ],
            "distDependencies": [
                "LIBRARY_SUPPORT",
            ],
        },

        "POINTSTO": {
            "subDir": "src",
            "description" : "SubstrateVM static analysis to find ahead-of-time the code",
            "dependencies": [
                "com.oracle.graal.pointsto",
            ],
            "distDependencies": [
                "compiler:GRAAL",
            ],
            "exclude": [
            ],
        },

        "SVM_TESTS" : {
          "relpath" : True,
          "dependencies" : [
            "com.oracle.svm.test",
          ],
          "distDependencies": [
            "mx:JUNIT_TOOL",
            "sdk:GRAAL_SDK",
          ],
          "testDistribution" : True,
        },

        "POLYGLOT_NATIVE_API" : {
            "dependencies": [
                "org.graalvm.polyglot.nativeapi",
            ],
            "distDependencies": [
                "sdk:GRAAL_SDK",
                "SVM",
            ],
            "maven": False
        },

        "POLYGLOT_NATIVE_API_HEADERS" : {
            "native" : True,
            "platformDependent" : True,
            "description" : "polyglot.nativeapi header files for the GraalVM build process",
            "layout" : {
                "./" : [
                    "extracted-dependency:POLYGLOT_NATIVE_API/*.h",
                ],
            },
        },

        "POLYGLOT_NATIVE_API_SUPPORT" : {
            "native" : True,
            "platformDependent" : True,
            "description" : "polyglot.nativeapi support distribution for the GraalVM",
            "os_arch" : {
                "linux": {
                    "amd64" : {
                         "layout" : {
                             "./" : [
                                 "dependency:org.graalvm.polyglot.nativeapi.native/<os>-<arch>/*.o",
                             ],
                         },
                    },
                },
                "darwin": {
                    "amd64" : {
                         "layout" : {
                             "./" : [
                                 "dependency:org.graalvm.polyglot.nativeapi.native/<os>-<arch>/*.o",
                             ],
                         },
                    },
                },
                "windows": {
                    "amd64" : {
                         "layout" : {
                             "./" : [
                                 "dependency:org.graalvm.polyglot.nativeapi.native/<os>-<arch>/*.obj",
                             ],
                         },
                    },
                },
            },
        },

        "SVM_GRAALVM_SUPPORT" : {
            "native" : True,
            "platformDependent" : True,
            "description" : "SubstrateVM support distribution for the GraalVM",
            "layout" : {
                "bin/rebuild-images" : "file:mx.substratevm/rebuild-images.sh",
                "clibraries/" : ["extracted-dependency:substratevm:SVM_HOSTED_NATIVE"],
                "builder/clibraries/" : ["extracted-dependency:substratevm:SVM_HOSTED_NATIVE"],
            },
        },

        "NATIVE_IMAGE_JUNIT_SUPPORT" : {
            "native" : True,
            "description" : "Native-image based junit testing support",
            "layout" : {
                "native-image.properties" : "file:mx.substratevm/tools-junit.properties",
            },
        },
    },
}
