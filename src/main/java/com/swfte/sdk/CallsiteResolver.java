package com.swfte.sdk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Decides the {@value CallSite#HEADER} value for one artifact invocation (code map, runtime
 * attribution).
 *
 * <ol>
 *   <li>An explicit {@link CallSite} always wins: sent when valid, otherwise nothing is sent.</li>
 *   <li>With no explicit id, stack capture runs only when opted in ({@code SWFTE_CALLSITE_STACK=1}
 *       or system property {@code swfte.callsite.stack=1}) and the runtime is not production
 *       (system property {@code swfte.env} or env {@code SWFTE_ENV} equal to {@code production};
 *       there it is refused with a single warning). It takes the first stack frame outside this
 *       SDK and looks up {@code <package path>/<source file>:<line>} in the local caller map written
 *       by {@code swfte scan}: env {@code SWFTE_CODEMAP_CALLERS}, else
 *       {@code <cwd>/.swfte/codemap/callers.json}. An entry matches when its path ends with that
 *       package path and file name; exactly one match is required.</li>
 *   <li>Otherwise (the default) no header.</li>
 * </ol>
 *
 * <p>The caller map is read locally and never uploaded; only the id it names is sent.</p>
 *
 * <p>Environment and system-property reads go through the functions given to the constructor,
 * so tests and embedders can supply their own; {@link #system()} reads the real ones.</p>
 */
public final class CallsiteResolver {

    public static final String ENV_STACK = "SWFTE_CALLSITE_STACK";
    public static final String PROP_STACK = "swfte.callsite.stack";
    public static final String ENV_ENV = "SWFTE_ENV";
    public static final String PROP_ENV = "swfte.env";
    public static final String ENV_CALLERS = "SWFTE_CODEMAP_CALLERS";

    private static final Logger LOG = Logger.getLogger(CallsiteResolver.class.getName());
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String SDK_PACKAGE_PREFIX = "com.swfte.sdk.";
    private static final URL SDK_LOCATION = locationOf(CallsiteResolver.class);
    private static final StackWalker WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    private static final CallsiteResolver SYSTEM = new CallsiteResolver(
        System::getenv, System::getProperty, null, message -> LOG.warning(message));

    private final Function<String, String> env;
    private final Function<String, String> props;
    private final Path cwd;
    private final Consumer<String> warn;
    private final AtomicBoolean productionWarned = new AtomicBoolean(false);
    private volatile CallerMap cache;

    /**
     * @param env environment lookup ({@code System::getenv} in production code)
     * @param props system-property lookup ({@code System::getProperty})
     * @param cwd directory holding the default {@code .swfte/codemap/callers.json};
     *            {@code null} means the process working directory
     * @param warn receives the one production-refusal warning; {@code null} logs it
     */
    public CallsiteResolver(Function<String, String> env, Function<String, String> props, Path cwd, Consumer<String> warn) {
        this.env = env != null ? env : name -> null;
        this.props = props != null ? props : name -> null;
        this.cwd = cwd;
        this.warn = warn != null ? warn : message -> LOG.warning(message);
    }

    /** The process-wide resolver reading the real environment and system properties. */
    public static CallsiteResolver system() {
        return SYSTEM;
    }

    /**
     * The header value for this call, or {@code null} to send none. Call from inside the SDK
     * method the application invoked; the stack walk skips every SDK frame.
     */
    public String resolve(CallSite explicit) {
        if (explicit != null) {
            return explicit.isValid() ? explicit.getId() : null;
        }
        if (!isOn(env.apply(ENV_STACK)) && !isOn(props.apply(PROP_STACK))) {
            return null;
        }
        if (isProduction(props.apply(PROP_ENV)) || isProduction(env.apply(ENV_ENV))) {
            if (productionWarned.compareAndSet(false, true)) {
                warn.accept("swfte: " + ENV_STACK + " is ignored in production; call-site stack capture is off. "
                    + "Pass an explicit CallSite to attribute calls.");
            }
            return null;
        }
        Optional<StackWalker.StackFrame> frame = WALKER.walk(s -> s.filter(f -> !isSdkFrame(f)).findFirst());
        if (!frame.isPresent()) {
            return null;
        }
        CallerMap map = callerMap();
        return map == null ? null : map.lookup(frame.get());
    }

    private static boolean isOn(String value) {
        return value != null && "1".equals(value.trim());
    }

    private static boolean isProduction(String value) {
        return value != null && "production".equalsIgnoreCase(value.trim());
    }

    /**
     * An SDK frame is a class in {@code com.swfte.sdk} loaded from the SDK's own jar or classes
     * directory, so application code that happens to share the package name (or the SDK's own
     * tests) still counts as the caller.
     */
    private static boolean isSdkFrame(StackWalker.StackFrame f) {
        if (!f.getClassName().startsWith(SDK_PACKAGE_PREFIX)) {
            return false;
        }
        URL location = locationOf(f.getDeclaringClass());
        return SDK_LOCATION == null || location == null || SDK_LOCATION.equals(location);
    }

    private static URL locationOf(Class<?> type) {
        try {
            CodeSource source = type.getProtectionDomain().getCodeSource();
            return source == null ? null : source.getLocation();
        } catch (SecurityException e) {
            return null;
        }
    }

    private Path callerMapPath() {
        String configured = env.apply(ENV_CALLERS);
        if (configured != null && !configured.trim().isEmpty()) {
            return Paths.get(configured.trim());
        }
        Path base = cwd != null ? cwd : Paths.get("").toAbsolutePath();
        return base.resolve(".swfte").resolve("codemap").resolve("callers.json");
    }

    /** The caller map, re-read only when the file's path, size or modification time changes. */
    private CallerMap callerMap() {
        Path path = callerMapPath();
        long modified;
        long size;
        try {
            if (!Files.isRegularFile(path)) {
                return null;
            }
            modified = Files.getLastModifiedTime(path).toMillis();
            size = Files.size(path);
        } catch (IOException | SecurityException e) {
            return null;
        }
        CallerMap cached = cache;
        if (cached != null && cached.path.equals(path) && cached.modified == modified && cached.size == size) {
            return cached;
        }
        CallerMap loaded = CallerMap.load(path, modified, size);
        cache = loaded;
        return loaded;
    }

    /** {@code callers.json}: {@code {"version":1,"root":"...","entries":{"<path>:<line>":"cs_..."}}}. */
    private static final class CallerMap {
        final Path path;
        final long modified;
        final long size;
        /** {@code <file name>:<line>} to the full entries sharing it, as {path, id} pairs. */
        private final Map<String, List<String[]>> byFileLine;

        private CallerMap(Path path, long modified, long size, Map<String, List<String[]>> byFileLine) {
            this.path = path;
            this.modified = modified;
            this.size = size;
            this.byFileLine = byFileLine;
        }

        static CallerMap load(Path path, long modified, long size) {
            Map<String, List<String[]>> index = new HashMap<>();
            try {
                JsonNode root = MAPPER.readTree(path.toFile());
                JsonNode entries = root == null ? null : root.get("entries");
                if (root != null && root.path("version").asInt(0) == 1 && entries != null && entries.isObject()) {
                    Iterator<Map.Entry<String, JsonNode>> it = entries.fields();
                    while (it.hasNext()) {
                        Map.Entry<String, JsonNode> e = it.next();
                        String key = e.getKey();
                        String id = e.getValue().isTextual() ? e.getValue().asText() : null;
                        if (!CallSite.isValidId(id)) {
                            continue; // never send an id the map got wrong
                        }
                        int slash = key.lastIndexOf('/');
                        String fileLine = slash >= 0 ? key.substring(slash + 1) : key;
                        index.computeIfAbsent(fileLine, k -> new ArrayList<>()).add(new String[] {key, id});
                    }
                }
            } catch (IOException | RuntimeException e) {
                index = Collections.emptyMap();
            }
            return new CallerMap(path, modified, size, index);
        }

        String lookup(StackWalker.StackFrame frame) {
            String file = frame.getFileName();
            int line = frame.getLineNumber();
            if (file == null || file.isEmpty() || line <= 0) {
                return null;
            }
            String fileLine = file + ":" + line;
            List<String[]> candidates = byFileLine.get(fileLine);
            if (candidates == null) {
                return null;
            }
            String className = frame.getClassName();
            int dot = className.lastIndexOf('.');
            String suffix = (dot >= 0 ? className.substring(0, dot).replace('.', '/') + "/" : "") + fileLine;
            String found = null;
            for (String[] candidate : candidates) {
                String key = candidate[0];
                if (key.equals(suffix) || key.endsWith("/" + suffix)) {
                    if (found != null && !found.equals(candidate[1])) {
                        return null; // ambiguous: two source roots hold the same package path
                    }
                    found = candidate[1];
                }
            }
            return found;
        }
    }
}
