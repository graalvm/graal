package de.hpi.swa.trufflelsp.server;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.instrumentation.TruffleInstrument;
import com.oracle.truffle.api.instrumentation.TruffleInstrument.Env;
import com.oracle.truffle.api.nodes.LanguageInfo;
import com.oracle.truffle.api.source.Source;

import de.hpi.swa.trufflelsp.api.ContextAwareExecutor;
import de.hpi.swa.trufflelsp.api.ContextAwareExecutorRegistry;
import de.hpi.swa.trufflelsp.api.VirtualLanguageServerFileProvider;
import de.hpi.swa.trufflelsp.exceptions.DiagnosticsNotification;
import de.hpi.swa.trufflelsp.exceptions.UnknownLanguageException;
import de.hpi.swa.trufflelsp.server.request.CompletionRequestHandler;
import de.hpi.swa.trufflelsp.server.request.CoverageRequestHandler;
import de.hpi.swa.trufflelsp.server.request.DefinitionRequestHandler;
import de.hpi.swa.trufflelsp.server.request.HighlightRequestHandler;
import de.hpi.swa.trufflelsp.server.request.HoverRequestHandler;
import de.hpi.swa.trufflelsp.server.request.ReferencesRequestHandler;
import de.hpi.swa.trufflelsp.server.request.SignatureHelpRequestHandler;
import de.hpi.swa.trufflelsp.server.request.SourceCodeEvaluator;
import de.hpi.swa.trufflelsp.server.request.SymbolRequestHandler;
import de.hpi.swa.trufflelsp.server.utils.SourceUtils;
import de.hpi.swa.trufflelsp.server.utils.TextDocumentSurrogateMap;
import de.hpi.swa.trufflelsp.server.utils.TextDocumentSurrogate;

public class TruffleAdapter implements VirtualLanguageServerFileProvider, ContextAwareExecutorRegistry {

    private final TruffleInstrument.Env env;
    ContextAwareExecutor contextAwareExecutor;
    private SourceCodeEvaluator sourceCodeEvaluator;
    private CompletionRequestHandler completionHandler;
    private SymbolRequestHandler symbolHandler;
    private DefinitionRequestHandler definitionHandler;
    private HoverRequestHandler hoverHandler;
    private SignatureHelpRequestHandler signatureHelpHandler;
    private CoverageRequestHandler coverageHandler;
    private ReferencesRequestHandler referencesHandler;
    private HighlightRequestHandler highlightHandler;
    private TextDocumentSurrogateMap surrogateMap;

    public TruffleAdapter(Env env) {
        this.env = env;
    }

    public void register(ContextAwareExecutor executor) {
        this.contextAwareExecutor = executor;
    }

    public void initialize() {
        initSurrogateMap();
        createLSPRequestHandlers();
    }

    private void createLSPRequestHandlers() {
        this.sourceCodeEvaluator = new SourceCodeEvaluator(env, surrogateMap, contextAwareExecutor);
        this.completionHandler = new CompletionRequestHandler(env, surrogateMap, contextAwareExecutor, sourceCodeEvaluator);
        this.symbolHandler = new SymbolRequestHandler(env, surrogateMap, contextAwareExecutor);
        this.definitionHandler = new DefinitionRequestHandler(env, surrogateMap, contextAwareExecutor, sourceCodeEvaluator, symbolHandler);
        this.hoverHandler = new HoverRequestHandler(env, surrogateMap, contextAwareExecutor, completionHandler);
        this.signatureHelpHandler = new SignatureHelpRequestHandler(env, surrogateMap, contextAwareExecutor, sourceCodeEvaluator);
        this.coverageHandler = new CoverageRequestHandler(env, surrogateMap, contextAwareExecutor, sourceCodeEvaluator);
        this.highlightHandler = new HighlightRequestHandler(env, surrogateMap, contextAwareExecutor);
        this.referencesHandler = new ReferencesRequestHandler(env, surrogateMap, contextAwareExecutor, highlightHandler);
    }

    private void initSurrogateMap() {
        try {
            Future<Map<String, LanguageInfo>> futureMimeTypes = contextAwareExecutor.executeWithDefaultContext(() -> {
                Map<String, LanguageInfo> mimeType2LangInfo = new HashMap<>();
                for (LanguageInfo langInfo : env.getLanguages().values()) {
                    if (langInfo.isInternal()) {
                        continue;
                    }
                    langInfo.getMimeTypes().stream().forEach(mimeType -> mimeType2LangInfo.put(mimeType, langInfo));
                }
                return mimeType2LangInfo;
            });

            Future<Map<String, List<String>>> futureCompletionTriggerCharacters = contextAwareExecutor.executeWithDefaultContext(() -> {
                Map<String, List<String>> langId2CompletionTriggerCharacters = new HashMap<>();
                for (LanguageInfo langInfo : env.getLanguages().values()) {
                    if (langInfo.isInternal()) {
                        continue;
                    }
                    langId2CompletionTriggerCharacters.put(langInfo.getId(), env.getCompletionTriggerCharacters(langInfo.getId()));
                }
                return langId2CompletionTriggerCharacters;
            });

            Map<String, LanguageInfo> mimeType2LangInfo = futureMimeTypes.get();
            Map<String, List<String>> langId2CompletionTriggerCharacters = futureCompletionTriggerCharacters.get();

            this.surrogateMap = new TextDocumentSurrogateMap(langId2CompletionTriggerCharacters, mimeType2LangInfo);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    TextDocumentSurrogate getOrCreateSurrogate(URI uri, String text, LanguageInfo languageInfo) {
        TextDocumentSurrogate surrogate = surrogateMap.getOrCreateSurrogate(uri, languageInfo);
        surrogate.setEditorText(text);
        return surrogate;
    }

    public void didClose(URI uri) {
        surrogateMap.remove(uri);
    }

    public Future<CallTarget> parse(final String text, final String langId, final URI uri) {
        return contextAwareExecutor.executeWithDefaultContext(() -> parseWithEnteredContext(text, langId, uri));
    }

    protected CallTarget parseWithEnteredContext(final String text, final String langId, final URI uri) throws DiagnosticsNotification {
        LanguageInfo languageInfo = findLanguageInfo(langId, uri);
        TextDocumentSurrogate surrogate = getOrCreateSurrogate(uri, text, languageInfo);
        return parseWithEnteredContext(surrogate);
    }

    CallTarget parseWithEnteredContext(TextDocumentSurrogate surrogate) throws DiagnosticsNotification {
        return sourceCodeEvaluator.parse(surrogate);
    }

    public Future<?> reparse(URI uri) {
        TextDocumentSurrogate surrogate = surrogateMap.get(uri);
        return contextAwareExecutor.executeWithDefaultContext(() -> parseWithEnteredContext(surrogate));
    }

    /**
     * Special handling needed, because some LSP clients send a MIME type as langId.
     *
     * @param langId an id for a language, e.g. "sl" or "python", or a MIME type
     * @param uri of the concerning file
     * @return a language info
     */
    private LanguageInfo findLanguageInfo(final String langId, final URI uri) {
        if (env.getLanguages().containsKey(langId)) {
            return env.getLanguages().get(langId);
        }

        String possibleMimeType = langId;
        String actualLangId = Source.findLanguage(possibleMimeType);
        if (actualLangId == null) {
            try {
                actualLangId = Source.findLanguage(uri.toURL());
            } catch (IOException e) {
            }

            if (actualLangId == null) {
                actualLangId = langId;
            }
        }

        if (!env.getLanguages().containsKey(actualLangId)) {
            throw new UnknownLanguageException("Unknown language: " + actualLangId + ". Known languages are: " + env.getLanguages().keySet());
        }

        return env.getLanguages().get(actualLangId);
    }

    public Future<TextDocumentSurrogate> processChangesAndParse(List<? extends TextDocumentContentChangeEvent> list, URI uri) {
        return contextAwareExecutor.executeWithDefaultContext(() -> processChangesAndParseWithContextEntered(list, uri));
    }

    protected TextDocumentSurrogate processChangesAndParseWithContextEntered(List<? extends TextDocumentContentChangeEvent> list, URI uri) throws DiagnosticsNotification {
        TextDocumentSurrogate surrogate = surrogateMap.get(uri);

        if (list.isEmpty()) {
            return surrogate;
        }

        surrogate.getChangeEventsSinceLastSuccessfulParsing().addAll(list);
        surrogate.setLastChange(list.get(list.size() - 1));
        surrogate.setEditorText(SourceUtils.applyTextDocumentChanges(list, surrogate.getEditorText(), surrogate));

        sourceCodeEvaluator.parse(surrogate);

        if (surrogate.hasCoverageData()) {
            showCoverage(uri);
        }

        return surrogate;
    }

    public List<Future<?>> parseWorkspace(URI rootUri) {
        if (rootUri == null) {
            return new ArrayList<>();
        }
        Path rootPath = Paths.get(rootUri);
        if (!Files.isDirectory(rootPath)) {
            throw new IllegalArgumentException("Root URI is not referencing a directory. URI: " + rootUri);
        }

        Future<Map<String, LanguageInfo>> futureMimeTypes = contextAwareExecutor.executeWithDefaultContext(() -> {
            Map<String, LanguageInfo> mimeType2LangInfo = new HashMap<>();
            for (LanguageInfo langInfo : env.getLanguages().values()) {
                if (langInfo.isInternal()) {
                    continue;
                }
                langInfo.getMimeTypes().stream().forEach(mimeType -> mimeType2LangInfo.put(mimeType, langInfo));
            }
            return mimeType2LangInfo;
        });

        try {
            Map<String, LanguageInfo> mimeTypesAllLang = futureMimeTypes.get();
            try {
                WorkspaceWalker walker = new WorkspaceWalker(mimeTypesAllLang);
                System.out.println("Start walking file tree at: " + rootPath);
                Files.walkFileTree(rootPath, walker);
                return walker.parsingTasks;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    final class WorkspaceWalker implements FileVisitor<Path> {

        List<Future<?>> parsingTasks;
        private Map<String, LanguageInfo> mimeTypesAllLang;

        public WorkspaceWalker(Map<String, LanguageInfo> mimeTypesAllLang) {
            this.mimeTypesAllLang = mimeTypesAllLang;
            this.parsingTasks = new ArrayList<>();
        }

        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if (dir.endsWith(".git")) { // TODO(ds) where to define this?
                return FileVisitResult.SKIP_SUBTREE;
            }
            return FileVisitResult.CONTINUE;
        }

        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            URI uri = file.toUri();
            String mimeType = Source.findMimeType(uri.toURL());
            if (!mimeTypesAllLang.containsKey(mimeType)) {
                return FileVisitResult.CONTINUE;
            }
            TextDocumentSurrogate surrogate = getOrCreateSurrogate(uri, null, mimeTypesAllLang.get(mimeType));
            parsingTasks.add(contextAwareExecutor.executeWithDefaultContext(() -> parseWithEnteredContext(surrogate)));
            return FileVisitResult.CONTINUE;
        }

        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

    }

    public Future<List<? extends SymbolInformation>> documentSymbol(URI uri) {
        return contextAwareExecutor.executeWithDefaultContext(() -> symbolHandler.documentSymbolWithEnteredContext(uri));
    }

    public Future<List<? extends SymbolInformation>> workspaceSymbol(String query) {
        return contextAwareExecutor.executeWithDefaultContext(() -> symbolHandler.workspaceSymbolWithEnteredContext(query));
    }

    /**
     * Provides completions for a specific position in the document. If line or column are out of
     * range, items of global scope (top scope) are provided.
     *
     * @param uri
     * @param line 0-based line number
     * @param column 0-based column number (character offset)
     * @return a {@link Future} of {@link CompletionList} containing all completions for the cursor
     *         position
     */
    public Future<CompletionList> completion(final URI uri, int line, int column) {
        return contextAwareExecutor.executeWithDefaultContext(() -> completionHandler.completionWithEnteredContext(uri, line, column));
    }

    public Future<List<? extends Location>> definition(URI uri, int line, int character) {
        return contextAwareExecutor.executeWithDefaultContext(() -> definitionHandler.definitionWithEnteredContext(uri, line, character));
    }

    public Future<Hover> hover(URI uri, int line, int column) {
        return contextAwareExecutor.executeWithDefaultContext(() -> hoverHandler.hoverWithEnteredContext(uri, line, column));
    }

    public Future<SignatureHelp> signatureHelp(URI uri, int line, int character) {
        return contextAwareExecutor.executeWithNestedContext(() -> signatureHelpHandler.signatureHelpWithEnteredContext(uri, line, character), true);
    }

    public Future<Boolean> runCoverageAnalysis(final URI uri) {
        Future<Boolean> future = contextAwareExecutor.executeWithDefaultContext(() -> {
            contextAwareExecutor.resetContextCache(); // We choose coverage runs as checkpoints to
                                                      // clear the pooled context. A coverage run
                                                      // can be triggered by the user via the
                                                      // editor, so that the user can actively
                                                      // control the reset of the current cached
                                                      // context.
            Future<Boolean> futureCoverage = contextAwareExecutor.executeWithNestedContext(() -> coverageHandler.runCoverageAnalysisWithEnteredContext(uri), true);
            try {
                return futureCoverage.get();
            } catch (ExecutionException e) {
                if (e.getCause() instanceof Exception) {
                    throw (Exception) e.getCause();
                } else {
                    throw e;
                }
            }
        });
        return future;
    }

    public Future<?> showCoverage(URI uri) {
        return contextAwareExecutor.executeWithDefaultContext(() -> {
            coverageHandler.showCoverageWithEnteredContext(uri);
            return null;
        });
    }

    public Future<List<String>> getCompletionTriggerCharactersOfAllLanguages() {
        return contextAwareExecutor.executeWithDefaultContext(() -> completionHandler.getCompletionTriggerCharactersWithEnteredContext());
    }

    public Future<List<String>> getCompletionTriggerCharacters(String langId) {
        return contextAwareExecutor.executeWithDefaultContext(() -> completionHandler.getCompletionTriggerCharactersWithEnteredContext(langId));
    }

    /**
     * Clears all collected coverage data for all files. See {@link #clearCoverage(URI)} for
     * details.
     */
    public Future<?> clearCoverage() {
        return contextAwareExecutor.executeWithDefaultContext(() -> {
            System.out.println("Clearing and re-parsing all files with coverage data...");
            List<PublishDiagnosticsParams> params = new ArrayList<>();
            surrogateMap.getSurrogates().stream().forEach(surrogate -> {
                surrogate.clearCoverage();
                try {
                    sourceCodeEvaluator.parse(surrogate);
                    params.add(new PublishDiagnosticsParams(surrogate.getUri().toString(), Collections.emptyList()));
                } catch (DiagnosticsNotification e) {
                    params.addAll(e.getDiagnosticParamsCollection());
                }
            });
            System.out.println("Clearing and re-parsing done.");

            throw new DiagnosticsNotification(params);
        });
    }

    /**
     * Clears the coverage data for a specific URI. Clearing means removing all Diagnostics used to
     * highlight covered code. To avoid hiding syntax errors, the URIs source is parsed again. If
     * errors occur during parsing, a {@link DiagnosticsNotification} is thrown. If not, we still
     * have to clear all Diagnostics by throwing an empty {@link DiagnosticsNotification}
     * afterwards.
     *
     * @param uri to source to clear coverage data for
     */
    public Future<?> clearCoverage(URI uri) {
        return contextAwareExecutor.executeWithDefaultContext(() -> {
            TextDocumentSurrogate surrogate = surrogateMap.get(uri);
            surrogate.clearCoverage();
            sourceCodeEvaluator.parse(surrogate);

            throw new DiagnosticsNotification(new PublishDiagnosticsParams(uri.toString(), Collections.emptyList()));
        });
    }

    public Future<List<? extends Location>> references(URI uri, int line, int character) {
        return contextAwareExecutor.executeWithDefaultContext(() -> referencesHandler.referencesWithEnteredContext(uri, line, character));
    }

    public Future<List<? extends DocumentHighlight>> documentHighlight(URI uri, int line, int character) {
        return contextAwareExecutor.executeWithDefaultContext(() -> highlightHandler.highlightWithEnteredContext(uri, line, character));
    }

    public String getSourceText(Path path) {
        if (surrogateMap == null) {
            return null;
        }

        TextDocumentSurrogate surrogate = surrogateMap.get(path.toUri());
        return surrogate != null ? surrogate.getEditorText() : null;
    }

    public boolean isVirtualFile(Path path) {
        return surrogateMap.containsSurrogate(path.toUri());
    }
}
