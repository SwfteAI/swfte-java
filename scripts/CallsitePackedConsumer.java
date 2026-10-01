import com.sun.net.httpserver.HttpServer;
import com.swfte.sdk.CallSite;
import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.exceptions.SwfteException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

/** Runs against the copied packaged JAR, never target/classes or SDK source. */
public final class CallsitePackedConsumer {
    public static void main(String[] args) throws Exception {
        Path loaded = Path.of(SwfteClient.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        if (!loaded.equals(Path.of(args[0]).toRealPath())) throw new AssertionError("SDK was not loaded from isolated packaged JAR");
        String jarSha256=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(loaded)));
        List<String[]> seen = new ArrayList<>();
        Map<String,String> executions = new HashMap<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            exchange.getRequestBody().readAllBytes();
            String path = exchange.getRequestURI().getRawPath();
            seen.add(new String[]{exchange.getRequestMethod(),path,exchange.getRequestHeaders().getFirst("X-Swfte-Callsite")});
            String json;
            if(path.endsWith("/status")) {
                String[] pieces=path.split("/");String eid=pieces[pieces.length-2];
                json="{\"execution\":{\"executionId\":\""+eid+"\",\"status\":\"SUCCEEDED\",\"outputData\":{\"marker\":\"snapshot-"+executions.get(eid)+"\"}}}";
            } else {
                String version=path.contains("/versions/")?URLDecoder.decode(path.split("/versions/")[1].split("/")[0],StandardCharsets.UTF_8):"live";
                String eid="packed_"+executions.size();executions.put(eid,version);
                json="{\"executionId\":\""+eid+"\",\"status\":\"PENDING\"}";
            }
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
            Map<String,Object> integer = client.workflows().invokeVersionAndWait("wf_packed", 3, null, 5000, 1, false, CallSite.of(id)).getOutputs();
            if (!Map.of("marker", "snapshot-3").equals(integer)) throw new AssertionError("wrong integer snapshot outputs");
            assertPost(seen.get(seen.size()-2),"/v2/workflows/wf_packed/versions/3/invoke",id);
            client.workflows().invokeVersion("wf_packed","1.0.7",null);
            assertPost(seen.get(seen.size()-1),"/v2/workflows/wf_packed/versions/1.0.7/invoke",null);
            Map<String,Object> semantic=client.workflows().invokeVersionAndWait("wf_packed","1.0.7",null,5000,1,false,CallSite.of(id)).getOutputs();
            if(!Map.of("marker","snapshot-1.0.7").equals(semantic)) throw new AssertionError("semantic pin was not preserved");
            String build="1.0.7-rc.2+build.09";
            Map<String,Object> built=client.workflows().invokeVersionAndWait("wf_packed",build,null,5000,1,false,CallSite.of(id)).getOutputs();
            if(!Map.of("marker","snapshot-"+build).equals(built)) throw new AssertionError("build identity was not preserved");
            assertPost(seen.get(seen.size()-2),"/v2/workflows/wf_packed/versions/1.0.7-rc.2%2Bbuild.09/invoke",id);
            int count=seen.size();
            for(String bad:new String[]{"5","1.0.7/","1.0.7?x=1","1.0.7#x","../1.0.7","1.0.7%2Fextra","1.0.7\n","1.0.7+"+"a".repeat(123)}) {
                boolean invoked=false;try {client.workflows().invokeVersion("wf_packed",bad,null);} catch(SwfteException expected){invoked=true;}
                boolean waited=false;try {client.workflows().invokeVersionAndWait("wf_packed",bad,null);} catch(SwfteException expected){waited=true;}
                if(!invoked || !waited || seen.size()!=count) throw new AssertionError("malformed pin reached transport");
            }
            if(seen.size()!=7 || seen.stream().filter(row->row[1].endsWith("/status")).anyMatch(row->row[2]!=null)) throw new AssertionError("wrong poll attribution/count");
            System.out.println("SDK_JAVA_PACKED_OK isolated-jar requests="+seen.size()+" semanticStrings=exact badStringTransportRequests=0 jarSha256="+jarSha256);
        } finally { server.stop(0); }
    }
    private static void assertPost(String[] seen,String path,String id) {
        if(!"POST".equals(seen[0]) || !path.equals(seen[1]) || !java.util.Objects.equals(id,seen[2])) throw new AssertionError("wrong invocation attribution/pin");
    }
}
