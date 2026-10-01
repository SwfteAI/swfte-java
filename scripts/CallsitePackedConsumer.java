import com.sun.net.httpserver.HttpServer;
import com.swfte.sdk.CallSite;
import com.swfte.sdk.SwfteClient;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Runs against the copied packaged JAR, never target/classes or SDK source. */
public final class CallsitePackedConsumer {
    public static void main(String[] args) throws Exception {
        Path loaded = Path.of(SwfteClient.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        if (!loaded.equals(Path.of(args[0]).toRealPath())) throw new AssertionError("SDK was not loaded from isolated packaged JAR");
        List<String[]> seen = new ArrayList<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            exchange.getRequestBody().readAllBytes();
            String path = exchange.getRequestURI().getRawPath();
            seen.add(new String[]{path, exchange.getRequestHeaders().getFirst("X-Swfte-Callsite")});
            String json = path.endsWith("/status")
                    ? "{\"execution\":{\"executionId\":\"packed\",\"status\":\"SUCCEEDED\",\"outputData\":{\"marker\":\"snapshot-3\"}}}"
                    : "{\"executionId\":\"packed\",\"status\":\"PENDING\"}";
            byte[] data = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, data.length);
            exchange.getResponseBody().write(data); exchange.close();
        });
        server.start();
        try {
            String id = "cs_" + "a".repeat(24);
            SwfteClient client = SwfteClient.builder().apiKey("packed-unit-key")
                    .apiBaseUrl("http://127.0.0.1:" + server.getAddress().getPort()).maxRetries(1).build();
            Map<String,Object> outputs = client.workflows().invokeVersionAndWait("wf_packed", 3, null, 5000, 1, false, CallSite.of(id)).getOutputs();
            if (!Map.of("marker", "snapshot-3").equals(outputs)) throw new AssertionError("wrong snapshot outputs");
            if (seen.size() != 2 || !"/v2/workflows/wf_packed/versions/3/invoke".equals(seen.get(0)[0])
                    || !id.equals(seen.get(0)[1]) || seen.get(1)[1] != null) throw new AssertionError("wrong invocation attribution");
            System.out.println("SDK_JAVA_PACKED_OK isolated-jar requests=" + seen.size());
        } finally { server.stop(0); }
    }
}
