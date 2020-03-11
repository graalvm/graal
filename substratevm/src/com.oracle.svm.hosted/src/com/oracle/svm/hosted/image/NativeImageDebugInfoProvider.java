package com.oracle.svm.hosted.image;

import static com.oracle.objectfile.debuginfo.DebugInfoProvider.DebugFrameSizeChange.Type.CONTRACT;
import static com.oracle.objectfile.debuginfo.DebugInfoProvider.DebugFrameSizeChange.Type.EXTEND;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.graalvm.compiler.code.CompilationResult;
import org.graalvm.compiler.code.SourceMapping;
import org.graalvm.compiler.debug.DebugContext;
import org.graalvm.compiler.graph.NodeSourcePosition;
import org.graalvm.nativeimage.ImageSingletons;

import com.oracle.graal.pointsto.infrastructure.OriginalClassProvider;
import com.oracle.graal.pointsto.meta.AnalysisType;
import com.oracle.objectfile.debuginfo.DebugInfoProvider;
import com.oracle.svm.hosted.image.sources.SourceManager;
import com.oracle.svm.hosted.meta.HostedMethod;
import com.oracle.svm.hosted.meta.HostedType;

import jdk.vm.ci.code.site.Mark;
import jdk.vm.ci.meta.LineNumberTable;
import jdk.vm.ci.meta.ResolvedJavaMethod;
import jdk.vm.ci.meta.ResolvedJavaType;

/**
 * Implementation of the DebugInfoProvider API interface that allows type, code and heap data info
 * to be passed to an ObjectFile when generation of debug info is enabled.
 */
class NativeImageDebugInfoProvider implements DebugInfoProvider {
    private final DebugContext debugContext;
    private final NativeImageCodeCache codeCache;
    @SuppressWarnings("unused") private final NativeImageHeap heap;

    NativeImageDebugInfoProvider(DebugContext debugContext, NativeImageCodeCache codeCache, NativeImageHeap heap) {
        super();
        this.debugContext = debugContext;
        this.codeCache = codeCache;
        this.heap = heap;
    }

    @Override
    public Stream<DebugTypeInfo> typeInfoProvider() {
        return Stream.empty();
    }

    @Override
    public Stream<DebugCodeInfo> codeInfoProvider() {
        return codeCache.compilations.entrySet().stream().map(entry -> new NativeImageDebugCodeInfo(entry.getKey(), entry.getValue()));
    }

    @Override
    public Stream<DebugDataInfo> dataInfoProvider() {
        return Stream.empty();
    }

    /**
     * implementation of the DebugCodeInfo API interface that allows code info to be passed to an
     * ObjectFile when generation of debug info is enabled.
     */
    private class NativeImageDebugCodeInfo implements DebugCodeInfo {
        private final HostedMethod method;
        private final ResolvedJavaType javaType;
        private final CompilationResult compilation;
        private Path fullFilePath;

        NativeImageDebugCodeInfo(HostedMethod method, CompilationResult compilation) {
            this.method = method;
            HostedType declaringClass = method.getDeclaringClass();
            Class<?> clazz = declaringClass.getJavaClass();
            this.javaType = declaringClass.getWrapped();
            this.compilation = compilation;
            fullFilePath = ImageSingletons.lookup(SourceManager.class).findAndCacheSource(javaType, clazz);
        }

        @SuppressWarnings("try")
        @Override
        public void debugContext(Consumer<DebugContext> action) {
            try (DebugContext.Scope s = debugContext.scope("DebugCodeInfo", method)) {
                action.accept(debugContext);
            } catch (Throwable e) {
                throw debugContext.handle(e);
            }
        }

        @Override
        public String fileName() {
            if (fullFilePath != null) {
                return fullFilePath.getFileName().toString();
            }
            return "";
        }

        @Override
        public Path filePath() {
            if (fullFilePath != null) {
                return fullFilePath.getParent();
            }
            return null;
        }

        @Override
        public String className() {
            return javaType.toClassName();
        }

        @Override
        public String methodName() {
            return method.format("%n");
        }

        @Override
        public String paramNames() {
            return method.format("%P");
        }

        @Override
        public String returnTypeName() {
            return method.format("%R");
        }

        @Override
        public int addressLo() {
            return method.getCodeAddressOffset();
        }

        @Override
        public int addressHi() {
            return method.getCodeAddressOffset() + compilation.getTargetCodeSize();
        }

        @Override
        public int line() {
            LineNumberTable lineNumberTable = method.getLineNumberTable();
            if (lineNumberTable != null) {
                return lineNumberTable.getLineNumber(0);
            }
            return -1;
        }

        @Override
        public Stream<DebugLineInfo> lineInfoProvider() {
            if (fileName().length() == 0) {
                return Stream.empty();
            }
            return compilation.getSourceMappings().stream().map(sourceMapping -> new NativeImageDebugLineInfo(sourceMapping));
        }

        @Override
        public int getFrameSize() {
            return compilation.getTotalFrameSize();
        }

        @Override
        public List<DebugFrameSizeChange> getFrameSizeChanges() {
            List<DebugFrameSizeChange> frameSizeChanges = new LinkedList<>();
            for (Mark mark : compilation.getMarks()) {
                // we only need to observe stack increment or decrement points
                if (mark.id.equals("PROLOGUE_DECD_RSP")) {
                    NativeImageDebugFrameSizeChange sizeChange = new NativeImageDebugFrameSizeChange(mark.pcOffset, EXTEND);
                    frameSizeChanges.add(sizeChange);
                    // } else if (mark.id.equals("PROLOGUE_END")) {
                    // can ignore these
                    // } else if (mark.id.equals("EPILOGUE_START")) {
                    // can ignore these
                } else if (mark.id.equals("EPILOGUE_INCD_RSP")) {
                    NativeImageDebugFrameSizeChange sizeChange = new NativeImageDebugFrameSizeChange(mark.pcOffset, CONTRACT);
                    frameSizeChanges.add(sizeChange);
                    // } else if(mark.id.equals("EPILOGUE_END")) {
                }
            }
            return frameSizeChanges;
        }
    }

    /**
     * implementation of the DebugLineInfo API interface that allows line number info to be passed
     * to an ObjectFile when generation of debug info is enabled.
     */
    private class NativeImageDebugLineInfo implements DebugLineInfo {
        private final int bci;
        private final ResolvedJavaMethod method;
        private final int lo;
        private final int hi;
        private Path fullFilePath;

        NativeImageDebugLineInfo(SourceMapping sourceMapping) {
            NodeSourcePosition position = sourceMapping.getSourcePosition();
            int posbci = position.getBCI();
            this.bci = (posbci >= 0 ? posbci : 0);
            this.method = position.getMethod();
            this.lo = sourceMapping.getStartOffset();
            this.hi = sourceMapping.getEndOffset();
            computeFullFilePath();
        }

        @Override
        public String fileName() {
            if (fullFilePath != null) {
                return fullFilePath.getFileName().toString();
            }
            return null;
        }

        @Override
        public Path filePath() {
            if (fullFilePath != null) {
                return fullFilePath.getParent();
            }
            return null;
        }

        @Override
        public String className() {
            return method.format("%H");
        }

        @Override
        public String methodName() {
            return method.format("%n");
        }

        @Override
        public int addressLo() {
            return lo;
        }

        @Override
        public int addressHi() {
            return hi;
        }

        @Override
        public int line() {
            LineNumberTable lineNumberTable = method.getLineNumberTable();
            if (lineNumberTable != null) {
                return lineNumberTable.getLineNumber(bci);
            }
            return -1;
        }

        private void computeFullFilePath() {
            ResolvedJavaType declaringClass = method.getDeclaringClass();
            Class<?> clazz = null;
            if (declaringClass instanceof OriginalClassProvider) {
                clazz = ((OriginalClassProvider) declaringClass).getJavaClass();
            }
            /*
             * HostedType and AnalysisType punt calls to getSourceFilename to the wrapped class so
             * for consistency we need to do the path lookup relative to the wrapped class
             */
            if (declaringClass instanceof HostedType) {
                declaringClass = ((HostedType) declaringClass).getWrapped();
            }
            if (declaringClass instanceof AnalysisType) {
                declaringClass = ((AnalysisType) declaringClass).getWrapped();
            }
            fullFilePath = ImageSingletons.lookup(SourceManager.class).findAndCacheSource(declaringClass, clazz);
        }

    }

    /**
     * implementation of the DebugFrameSizeChange API interface that allows stack frame size change
     * info to be passed to an ObjectFile when generation of debug info is enabled.
     */
    private class NativeImageDebugFrameSizeChange implements DebugFrameSizeChange {
        private int offset;
        private Type type;

        NativeImageDebugFrameSizeChange(int offset, Type type) {
            this.offset = offset;
            this.type = type;
        }

        @Override
        public int getOffset() {
            return offset;
        }

        @Override
        public Type getType() {
            return type;
        }
    }
}
