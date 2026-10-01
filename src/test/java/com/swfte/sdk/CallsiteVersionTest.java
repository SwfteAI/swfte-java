package com.swfte.sdk;

import com.sun.net.httpserver.HttpServer;
import com.swfte.sdk.exceptions.ApiException;
import com.swfte.sdk.exceptions.SwfteException;
import com.swfte.sdk.models.WorkflowExecution;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

/** Actual loopback wire checks. Snapshot responses are fixtures, not production route credit. */
class CallsiteVersionTest {
    private HttpServer server;
    private int live = 3;
    private String redirect;
    private final List<String[]> seen = new ArrayList<>();
    private final Map<String,Integer> executions = new HashMap<>();
    private final String id = "cs_" + "a".repeat(24);
    @BeforeEach void start() throws Exception {
        server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/", exchange -> {
            exchange.getRequestBody().readAllBytes();
            String path=exchange.getRequestURI().getRawPath();
            seen.add(new String[]{exchange.getRequestMethod(),path,exchange.getRequestHeaders().getFirst("X-Swfte-Callsite")});
            if(redirect!=null) { exchange.getResponseHeaders().add("Location",redirect); exchange.sendResponseHeaders(302,-1); exchange.close(); return; }
            int status=200; String json;
            if("B".equals(exchange.getRequestHeaders().getFirst("X-Workspace-Id")) || path.contains("/versions/9/")) {
                status=404; json="{\"error\":\"VERSION_NOT_PUBLISHED\"}";
            } else if(path.endsWith("/status")) {
                String[] pieces=path.split("/"); int version=executions.get(pieces[pieces.length-2]);
                json="{\"execution\":{\"executionId\":\"ex\",\"status\":\"SUCCEEDED\",\"workflowVersion\":"+version+",\"outputData\":{\"marker\":\"snapshot-"+version+"\"}}}";
            } else {
                int version=path.contains("/versions/")?Integer.parseInt(path.split("/versions/")[1].split("/")[0]):live;
                String eid="ex_"+executions.size(); executions.put(eid,version);
                json="{\"executionId\":\""+eid+"\",\"status\":\"PENDING\",\"response\":\"ok\",\"sessionId\":\"session\"}";
            }
            byte[] bytes=json.getBytes(StandardCharsets.UTF_8); exchange.getResponseHeaders().add("Content-Type","application/json");
            exchange.sendResponseHeaders(status,bytes.length); exchange.getResponseBody().write(bytes); exchange.close();
        }); server.start();
    }
    @AfterEach void stop() {server.stop(0);}
    private SwfteClient client(String workspace) {
        return SwfteClient.builder().apiKey("unit-test-key").apiBaseUrl("http://127.0.0.1:"+server.getAddress().getPort())
                .workspaceId(workspace).maxRetries(1).build();
    }
    @Test void immutablePinSurvivesPromotionAndPollsHaveNoAttribution() {
        SwfteClient c=client("A"); live=4;
        WorkflowExecution pinned=c.workflows().invokeVersionAndWait("wf_shared",3,null,5000,1,false,CallSite.of(id));
        WorkflowExecution current=c.workflows().invokeAndWait("wf_shared",null,5000,1);
        assertEquals(Map.of("marker","snapshot-3"),pinned.getOutputs()); assertEquals(Map.of("marker","snapshot-4"),current.getOutputs());
        assertEquals("/v2/workflows/wf_shared/versions/3/invoke",seen.get(0)[1]); assertEquals(id,seen.get(0)[2]);
        assertTrue(seen.stream().filter(row->row[1].endsWith("/status")).allMatch(row->row[2]==null));
        assertEquals(404,assertThrows(ApiException.class,()->c.workflows().invokeVersion("wf_shared",9,null)).getStatusCode());
        assertEquals(404,assertThrows(ApiException.class,()->client("B").workflows().invokeVersion("wf_shared",3,null)).getStatusCode());
    }
    @Test void everyNewOverloadDefaultsOffAndInvalidPinsNeverReachTransport() {
        SwfteClient c=client("A");
        assertThrows(SwfteException.class,()->c.workflows().invokeVersion("wf_shared",0,null));
        assertThrows(SwfteException.class,()->c.workflows().invokeVersion("wf_shared",-1,null)); assertTrue(seen.isEmpty());
        c.workflows().invokeVersion("wf /shared",3,null); assertTrue(seen.get(0)[1].contains("wf%20%2Fshared")); assertNull(seen.get(0)[2]);
        c.workflows().invokeVersion("wf_shared",3,null,CallSite.of(id+"\n")); assertNull(seen.get(1)[2]);
        c.workflows().invokeVersionAndWait("wf_shared",3,null);
        c.workflows().invokeVersionAndWait("wf_shared",3,null,CallSite.of(id));
        c.workflows().invokeVersionAndWait("wf_shared",3,null,5000,1,false);
        c.chatflows().builder().test("cf_shared",null,CallSite.of(id)); assertEquals(id,seen.get(seen.size()-1)[2]);
        c.chatflows().builder().test("cf_shared",null); assertNull(seen.get(seen.size()-1)[2]);
    }
    @Test void realRedirectDoesNotForwardCredentialOrCallsiteToSecondListener() throws Exception {
        AtomicInteger forwarded=new AtomicInteger(); HttpServer canary=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        canary.createContext("/",exchange->{forwarded.incrementAndGet(); exchange.sendResponseHeaders(200,-1);exchange.close();});canary.start();
        try {redirect="http://127.0.0.1:"+canary.getAddress().getPort()+"/canary";
            assertThrows(SwfteException.class,()->client("A").workflows().invokeVersion("wf_shared",3,null,CallSite.of(id)));
            assertEquals(id,seen.get(0)[2]); assertEquals(0,forwarded.get());
        } finally {canary.stop(0);}
    }
}
