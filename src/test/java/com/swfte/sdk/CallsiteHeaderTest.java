package com.swfte.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import com.swfte.sdk.models.AgentChatOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@code X-Swfte-Callsite} (code map, CONTRACT §6): sent only for a valid explicit id, or under
 * opt-in stack capture outside production when the local caller map names the calling line.
 *
 * <p>Environment and system properties are injected through {@link CallsiteResolver}, so nothing
 * here reads or changes the real environment.</p>
 */
class CallsiteHeaderTest {

    private static final String ID_A = "cs_" + "a1".repeat(12);
    private static final String ID_B = "cs_" + "b2".repeat(12);
    private static final String ID_C = "cs_" + "c3".repeat(12);
    /** Where this file sits under the scan root; the SDK matches on the package path + file name. */
    private static final String SELF = "src/test/java/com/swfte/sdk/CallsiteHeaderTest.java";

    @TempDir
    Path tmp;

    private HttpServer server;
    private final List<Recorded> recorded = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, String> env = new HashMap<>();
    private final Map<String, String> props = new HashMap<>();
    private final List<String> warnings = new ArrayList<>();

    private static final class Recorded {
        final String method;
        final String path;
        final String callsite;

        Recorded(String method, String path, String callsite) {
            this.method = method;
            this.path = path;
            this.callsite = callsite;
        }

        @Override
        public String toString() {
            return method + " " + path + " callsite=" + callsite;
        }
    }

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getRawPath();
            exchange.getRequestBody().readAllBytes();
            recorded.add(new Recorded(exchange.getRequestMethod(), path,
                exchange.getRequestHeaders().getFirst(CallSite.HEADER)));
            String json = path.endsWith("/status")
                ? "{\"execution\":{\"executionId\":\"ex_1\",\"workflowId\":\"wf_1\",\"status\":\"SUCCEEDED\"},\"nodeExecutions\":[],\"progress\":100}"
                : "{\"executionId\":\"ex_1\",\"workflowId\":\"wf_1\",\"status\":\"PENDING\",\"response\":\"hi\",\"sessionId\":\"s_1\"}";
            byte[] payload = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, payload.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(payload);
            }
        });
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    private SwfteClient client() {
        return SwfteClient.builder()
            .apiKey("pat_test")
            .baseUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/v2/gateway")
            .maxRetries(1)
            .callsiteResolver(new CallsiteResolver(env::get, props::get, tmp, warnings::add))
            .build();
    }

    /** The line of the statement that called this method. */
    private static int lineHere() {
        return StackWalker.getInstance().walk(s -> s.skip(1).findFirst()).get().getLineNumber();
    }

    /** Writes a caller map and returns its path. */
    private Path writeMap(Path file, Map<String, String> entries) throws IOException {
        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("version", 1);
        doc.put("root", tmp.toAbsolutePath().toString());
        doc.put("entries", entries);
        Files.createDirectories(file.getParent());
        Files.write(file, new ObjectMapper().writeValueAsBytes(doc));
        return file;
    }

    private Path writeMap(Map<String, String> entries) throws IOException {
        Path file = writeMap(tmp.resolve("maps").resolve("callers-" + System.nanoTime() + ".json"), entries);
        env.put(CallsiteResolver.ENV_CALLERS, file.toString());
        return file;
    }

    private static Map<String, String> entries(Object... keyValues) {
        Map<String, String> m = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            m.put(SELF + ":" + keyValues[i], (String) keyValues[i + 1]);
        }
        return m;
    }

    private Recorded last() {
        assertFalse(recorded.isEmpty(), "no request reached the server");
        return recorded.get(recorded.size() - 1);
    }

    /** Two application classes that call the same SDK method on the same workflow. */
    static final class BillingJob {
        static int line;

        static void run(SwfteClient c) {
            line = lineHere(); c.workflows().invoke("wf_shared", null);
        }
    }

    static final class ReportJob {
        static int line;

        static void run(SwfteClient c) {
            line = lineHere(); c.workflows().invoke("wf_shared", null);
        }
    }

    /** One call line that can also carry an explicit id. */
    static final class ExplicitJob {
        static int line;

        static void run(SwfteClient c, CallSite cs) {
            line = lineHere(); c.workflows().invoke("wf_1", null, cs);
        }
    }

    /** Every artifact-invoking method family without a call-site id. */
    private static void callEveryFamilyWithoutId(SwfteClient c) {
        c.workflows().invoke("wf_1", null);
        c.workflows().invokeAndWait("wf_1", null, 5000, 1);
        c.workflows().execute("wf_1", null);
        c.agents().chat("ag_1", "hello", AgentChatOptions.builder().build());
        c.chatflows().startSession("cf_1", null);
    }

    @Test
    void optOut_noHeaderByDefault_evenWithAMapThatNamesTheLine() throws IOException {
        SwfteClient c = client();
        callEveryFamilyWithoutId(c);
        BillingJob.run(c);
        assertTrue(recorded.size() >= 6);
        for (Recorded r : recorded) {
            assertNull(r.callsite, "header sent without opt-in: " + r);
        }

        // A map that names the call line changes nothing while stack capture is off...
        writeMap(entries(BillingJob.line, ID_A));
        BillingJob.run(c);
        assertNull(last().callsite);

        // ...and is picked up the moment it is switched on, so the silence above was the opt-out.
        env.put(CallsiteResolver.ENV_STACK, "1");
        BillingJob.run(c);
        assertEquals(ID_A, last().callsite);
    }

    @Test
    void explicitId_isSentByEveryMethodFamily() {
        SwfteClient c = client();
        CallSite cs = CallSite.of(ID_A);

        c.workflows().invoke("wf_1", null, cs);
        assertEquals("/v2/workflows/wf_1/invoke", last().path);
        assertEquals(ID_A, last().callsite);

        c.workflows().execute("wf_1", null, cs);
        assertEquals("/v2/workflows/wf_1/execute", last().path);
        assertEquals(ID_A, last().callsite);

        c.workflows().execute("wf_1", null, true, cs);
        assertEquals("/v2/workflows/wf_1/execute", last().path);
        assertEquals(ID_A, last().callsite);

        c.agents().chat("ag_1", "hello", null, cs);
        assertEquals("/v1/agents/ag_1/chat/sdk-user", last().path);
        assertEquals(ID_A, last().callsite);

        c.chatflows().startSession("cf_1", null, cs);
        assertEquals("/v2/chatflows/cf_1/sessions", last().path);
        assertEquals(ID_A, last().callsite);

        // invokeAndWait: the invoke POST carries the id, the status polls do not.
        for (int overload = 0; overload < 3; overload++) {
            recorded.clear();
            if (overload == 0) c.workflows().invokeAndWait("wf_1", null, cs);
            if (overload == 1) c.workflows().invokeAndWait("wf_1", null, 5000, 1, cs);
            if (overload == 2) c.workflows().invokeAndWait("wf_1", null, 5000, 1, false, cs);
            assertEquals(2, recorded.size(), "overload " + overload + ": " + recorded);
            assertEquals("/v2/workflows/wf_1/invoke", recorded.get(0).path);
            assertEquals(ID_A, recorded.get(0).callsite);
            assertEquals("/v2/workflows/executions/ex_1/status", recorded.get(1).path);
            assertNull(recorded.get(1).callsite);
        }
    }

    @Test
    void invalidIds_areNeverSent_explicitlyOrFromTheMap() throws IOException {
        SwfteClient c = client();
        // Stack capture on with a valid map entry for the call line: an invalid explicit id must
        // still send nothing (explicit wins, and it is not a fallback to the stack).
        env.put(CallsiteResolver.ENV_STACK, "1");
        ExplicitJob.run(c, null);
        writeMap(entries(ExplicitJob.line, ID_B));

        List<String> invalid = Arrays.asList(
            null, "", "cs_", "cs_" + "A1".repeat(12), "CS_" + "a1".repeat(12), "cs_" + "a".repeat(23),
            "cs_" + "a".repeat(25), "cs_" + "g".repeat(24), " " + ID_A, ID_A + "\n", "cx_" + "a".repeat(24),
            ID_A + "; X-Evil: 1");
        for (String bad : invalid) {
            recorded.clear();
            ExplicitJob.run(c, CallSite.of(bad));
            c.agents().chat("ag_1", "hello", null, CallSite.of(bad));
            c.chatflows().startSession("cf_1", null, CallSite.of(bad));
            c.workflows().execute("wf_1", null, CallSite.of(bad));
            assertEquals(4, recorded.size());
            for (Recorded r : recorded) {
                assertNull(r.callsite, "invalid id " + bad + " sent: " + r);
            }
            assertFalse(CallSite.of(bad).isValid());
        }

        // A map entry holding an invalid id is ignored too.
        writeMap(entries(ExplicitJob.line, "cs_" + "Z".repeat(24)));
        ExplicitJob.run(c, null);
        assertNull(last().callsite);
    }

    @Test
    void stackCapture_twoCallersOfTheSameMethodGetTwoIds() throws IOException {
        SwfteClient c = client();
        env.put(CallsiteResolver.ENV_STACK, "1");

        // First pass records the call lines; no map yet, so nothing is sent.
        BillingJob.run(c);
        assertNull(last().callsite);
        ReportJob.run(c);
        assertNull(last().callsite);
        assertNotEquals(BillingJob.line, ReportJob.line);

        writeMap(entries(BillingJob.line, ID_A, ReportJob.line, ID_B));
        BillingJob.run(c);
        String billing = last().callsite;
        ReportJob.run(c);
        String report = last().callsite;
        BillingJob.run(c);
        String billingAgain = last().callsite;

        assertEquals(ID_A, billing);
        assertEquals(ID_B, report);
        assertEquals(ID_A, billingAgain);
        assertEquals("/v2/workflows/wf_shared/invoke", last().path);
    }

    @Test
    void stackCapture_refusedInProduction_withOneWarning_butExplicitIsHonoured() throws IOException {
        SwfteClient c = client();
        env.put(CallsiteResolver.ENV_STACK, "1");
        ExplicitJob.run(c, null);
        writeMap(entries(ExplicitJob.line, ID_B));
        ExplicitJob.run(c, null);
        assertEquals(ID_B, last().callsite, "precondition: the map resolves outside production");

        props.put(CallsiteResolver.PROP_ENV, "production");
        ExplicitJob.run(c, null);
        assertNull(last().callsite);
        ExplicitJob.run(c, null);
        assertNull(last().callsite);
        assertEquals(1, warnings.size(), "exactly one refusal warning: " + warnings);

        ExplicitJob.run(c, CallSite.of(ID_C));
        assertEquals(ID_C, last().callsite);

        // SWFTE_ENV in the environment refuses the same way (property opt-in this time).
        props.clear();
        env.remove(CallsiteResolver.ENV_STACK);
        props.put(CallsiteResolver.PROP_STACK, "1");
        ExplicitJob.run(c, null);
        assertEquals(ID_B, last().callsite, "system-property opt-in works outside production");
        env.put(CallsiteResolver.ENV_ENV, "production");
        ExplicitJob.run(c, null);
        assertNull(last().callsite);
        c.agents().chat("ag_1", "hello", null, CallSite.of(ID_A));
        assertEquals(ID_A, last().callsite);
        assertEquals(1, warnings.size(), "still one warning: " + warnings);
    }

    @Test
    void explicitId_winsOverStackCapture() throws IOException {
        SwfteClient c = client();
        env.put(CallsiteResolver.ENV_STACK, "1");
        ExplicitJob.run(c, null);
        writeMap(entries(ExplicitJob.line, ID_B));
        ExplicitJob.run(c, null);
        assertEquals(ID_B, last().callsite);

        ExplicitJob.run(c, CallSite.of(ID_A));
        assertEquals(ID_A, last().callsite);
    }

    @Test
    void missingOrUnusableMap_sendsNothing() throws IOException {
        SwfteClient c = client();
        env.put(CallsiteResolver.ENV_STACK, "1");
        ExplicitJob.run(c, null);
        int line = ExplicitJob.line;

        // No map anywhere (no env path, nothing under <cwd>/.swfte/codemap).
        ExplicitJob.run(c, null);
        assertNull(last().callsite);

        // Env points at a file that does not exist.
        env.put(CallsiteResolver.ENV_CALLERS, tmp.resolve("nope.json").toString());
        ExplicitJob.run(c, null);
        assertNull(last().callsite);

        // Malformed JSON, and a wrong version.
        Path bad = tmp.resolve("bad.json");
        Files.write(bad, "{not json".getBytes(StandardCharsets.UTF_8));
        env.put(CallsiteResolver.ENV_CALLERS, bad.toString());
        ExplicitJob.run(c, null);
        assertNull(last().callsite);
        Path v2 = tmp.resolve("v2.json");
        Files.write(v2, ("{\"version\":2,\"root\":\"/x\",\"entries\":{\"" + SELF + ":" + line + "\":\"" + ID_A + "\"}}")
            .getBytes(StandardCharsets.UTF_8));
        env.put(CallsiteResolver.ENV_CALLERS, v2.toString());
        ExplicitJob.run(c, null);
        assertNull(last().callsite);

        // Same file name and line, but another package: not this caller.
        Map<String, String> other = new LinkedHashMap<>();
        other.put("src/main/java/com/acme/CallsiteHeaderTest.java:" + line, ID_A);
        writeMap(other);
        ExplicitJob.run(c, null);
        assertNull(last().callsite);

        // Two source roots claiming the same package path + file + line with different ids: ambiguous.
        Map<String, String> twice = new LinkedHashMap<>();
        twice.put("a/" + SELF + ":" + line, ID_A);
        twice.put("b/" + SELF + ":" + line, ID_B);
        writeMap(twice);
        ExplicitJob.run(c, null);
        assertNull(last().callsite);
    }

    @Test
    void defaultMapLocation_isUnderTheWorkingDirectory() throws IOException {
        SwfteClient c = client();
        env.put(CallsiteResolver.ENV_STACK, "1");
        ExplicitJob.run(c, null);
        writeMap(tmp.resolve(".swfte").resolve("codemap").resolve("callers.json"), entries(ExplicitJob.line, ID_C));
        assertNull(env.get(CallsiteResolver.ENV_CALLERS));
        ExplicitJob.run(c, null);
        assertEquals(ID_C, last().callsite);

        // Only an exact "1" opts in.
        env.put(CallsiteResolver.ENV_STACK, "true");
        ExplicitJob.run(c, null);
        assertNull(last().callsite);
    }

    @Test
    void callSiteValueType() {
        assertTrue(CallSite.isValidId(ID_A));
        assertEquals(27, ID_A.length());
        assertEquals(CallSite.of(ID_A), CallSite.of(ID_A));
        assertEquals("X-Swfte-Callsite", CallSite.HEADER);
        assertFalse(CallSite.of(null).isValid());
    }
}
