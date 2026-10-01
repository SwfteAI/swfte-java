package com.swfte.sdk;

import com.sun.net.httpserver.HttpServer;
import com.swfte.sdk.exceptions.ApiException;
import com.swfte.sdk.exceptions.SwfteException;
import com.swfte.sdk.models.WorkflowExecution;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
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
    private final Map<String,Object> executions = new HashMap<>();
    private final String id = "cs_" + "a".repeat(24);
    @BeforeEach void start() throws Exception {
        server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/", exchange -> {
            exchange.getRequestBody().readAllBytes();
            String path=exchange.getRequestURI().getRawPath();
            seen.add(new String[]{exchange.getRequestMethod(),path,exchange.getRequestHeaders().getFirst("X-Swfte-Callsite")});
            if(redirect!=null) { exchange.getResponseHeaders().add("Location",redirect); exchange.sendResponseHeaders(302,-1); exchange.close(); return; }
            int status=200; String json;
            if("B".equals(exchange.getRequestHeaders().getFirst("X-Workspace-Id")) || (path.contains("/versions/9/") || path.contains("/versions/9.9.9/"))) {
                status=404; json="{\"error\":\"VERSION_NOT_PUBLISHED\"}";
            } else if(path.endsWith("/status")) {
                String[] pieces=path.split("/"); Object version=executions.get(pieces[pieces.length-2]);
                String versionJson=version instanceof Number?version.toString():"\""+version+"\"";
                json="{\"execution\":{\"executionId\":\"ex\",\"status\":\"SUCCEEDED\",\"workflowVersion\":"+versionJson+",\"outputData\":{\"marker\":\"snapshot-"+version+"\"}}}";
            } else {
                String segment=path.contains("/versions/")?URLDecoder.decode(path.split("/versions/")[1].split("/")[0],StandardCharsets.UTF_8):null;
                Object version=segment==null?live:segment.matches("[0-9]+")?Integer.valueOf(segment):segment;
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

    @Test void semanticStringOverloadsPreserveExactPublishedIdentityAndPostOnlyAttribution() {
        SwfteClient c=client("A"); live=4;
        c.workflows().invokeVersion("wf_shared","1.0.7",null);
        assertEquals("/v2/workflows/wf_shared/versions/1.0.7/invoke",seen.get(0)[1]); assertNull(seen.get(0)[2]);
        c.workflows().invokeVersion("wf_shared","1.0.7",null,CallSite.of(id));
        assertEquals(id,seen.get(1)[2]);
        assertEquals(Map.of("marker","snapshot-1.0.7"),c.workflows().invokeVersionAndWait("wf_shared","1.0.7",null).getOutputs());
        assertEquals(Map.of("marker","snapshot-1.0.7"),c.workflows().invokeVersionAndWait("wf_shared","1.0.7",null,CallSite.of(id)).getOutputs());
        assertEquals(Map.of("marker","snapshot-1.0.7"),c.workflows().invokeVersionAndWait("wf_shared","1.0.7",null,5000,1,false).getOutputs());
        String build="1.0.7-rc.2+build.09";
        WorkflowExecution result=c.workflows().invokeVersionAndWait("wf_shared",build,null,5000,1,false,CallSite.of(id));
        assertEquals(Map.of("marker","snapshot-"+build),result.getOutputs());
        String[] post=seen.stream().filter(row->row[1].endsWith("/invoke")).reduce((a,b)->b).orElseThrow();
        assertEquals("/v2/workflows/wf_shared/versions/1.0.7-rc.2%2Bbuild.09/invoke",post[1]); assertEquals(id,post[2]);
        String boundary="1.0.7+"+"a".repeat(122); assertEquals(128,boundary.length());
        c.workflows().invokeVersion("wf_shared",boundary,null);
        assertEquals("/v2/workflows/wf_shared/versions/1.0.7%2B"+"a".repeat(122)+"/invoke",seen.get(seen.size()-1)[1]);
        assertEquals(404,assertThrows(ApiException.class,()->c.workflows().invokeVersion("wf_shared","9.9.9",null)).getStatusCode());
        assertEquals(404,assertThrows(ApiException.class,()->client("B").workflows().invokeVersion("wf_shared","1.0.7",null)).getStatusCode());
        assertTrue(seen.stream().filter(row->row[1].endsWith("/status")).allMatch(row->row[2]==null));
        assertTrue(seen.stream().noneMatch(row->row[1].equals("/v2/workflows/wf_shared/invoke")));
    }
    @Test void malformedSemanticStringsHaveZeroEffectsForInvokeAndEveryWaitEntry() {
        SwfteClient c=client("A");
        for(String bad:new String[]{null,"","5","01.0.7","1.0","1.0.7/","1.0.7?x=1","1.0.7#x","../1.0.7",
                ".","..","1.0.7%2Fextra"," 1.0.7","1.0.7 ","1.0.7\n","\n1.0.7","1.0.7\r","1.0.7\0",
                "١.0.7","1.0.7+","1.0.7-","1.0.7+"+"a".repeat(123)}) {
            assertThrows(SwfteException.class,()->c.workflows().invokeVersion("wf_shared",bad,null));
            assertThrows(SwfteException.class,()->c.workflows().invokeVersion("wf_shared",bad,null,CallSite.of(id)));
            assertThrows(SwfteException.class,()->c.workflows().invokeVersionAndWait("wf_shared",bad,null));
            assertThrows(SwfteException.class,()->c.workflows().invokeVersionAndWait("wf_shared",bad,null,CallSite.of(id)));
            assertThrows(SwfteException.class,()->c.workflows().invokeVersionAndWait("wf_shared",bad,null,5000,1,false));
            assertThrows(SwfteException.class,()->c.workflows().invokeVersionAndWait("wf_shared",bad,null,5000,1,false,CallSite.of(id)));
            assertTrue(seen.isEmpty());
        }
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
