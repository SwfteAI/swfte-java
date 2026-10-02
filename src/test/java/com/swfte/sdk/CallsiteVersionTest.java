package com.swfte.sdk;

import com.sun.net.httpserver.HttpServer;
import com.swfte.sdk.exceptions.ApiException;
import com.swfte.sdk.exceptions.SwfteException;
import com.swfte.sdk.models.WorkflowExecution;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.concurrent.ConcurrentHashMap;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

/** Actual loopback wire checks. Snapshot responses are fixtures, not production route credit. */
class CallsiteVersionTest {
    private static final Map<String,Object> INPUT=Map.of("label","workflow-wire","nested",Map.of("enabled",true,"values",java.util.Arrays.asList(1,"two",null)));
    private HttpServer server;
    private int live = 3;
    private String redirect;
    private final Map<Integer,Object> bodies=new HashMap<>();
    private final List<String[]> seen = new ArrayList<>();
    private final Map<String,Object> executions = new HashMap<>();
    private final Map<String,String> published = new ConcurrentHashMap<>();
    private final String id = "cs_" + "a".repeat(24);
    @BeforeEach void start() throws Exception {
        for(String label:List.of("3","1.0.7","1.0.7-rc.2+build.09","1.0.7+"+"a".repeat(122))) published.put(label,"PUBLISHED");
        server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/", exchange -> {
            byte[] raw=exchange.getRequestBody().readAllBytes();bodies.put(seen.size(),raw.length==0?null:new ObjectMapper().readValue(raw,Object.class));
            String path=exchange.getRequestURI().getRawPath();
            String wirePath=path+(exchange.getRequestURI().getRawQuery()==null?"":"?"+exchange.getRequestURI().getRawQuery());
            seen.add(new String[]{exchange.getRequestMethod(),wirePath,exchange.getRequestHeaders().getFirst("X-Swfte-Callsite")});
            if(redirect!=null) { exchange.getResponseHeaders().add("Location",redirect); exchange.sendResponseHeaders(302,-1); exchange.close(); return; }
            int status=200; String json;
            String segment=path.contains("/versions/")?URLDecoder.decode(path.split("/versions/")[1].split("/")[0],StandardCharsets.UTF_8):null;
            if("B".equals(exchange.getRequestHeaders().getFirst("X-Workspace-Id")) || (segment!=null && !List.of("PUBLISHED","DEPRECATED").contains(published.getOrDefault(segment,"")))) {
                status=404; json="{\"error\":\"VERSION_NOT_PUBLISHED\"}";
            } else if(path.endsWith("/status")) {
                String[] pieces=path.split("/"); Object version=executions.get(pieces[pieces.length-2]);
                String versionJson=version instanceof Number?version.toString():"\""+version+"\"";
                json="{\"execution\":{\"executionId\":\"ex\",\"status\":\"SUCCEEDED\",\"workflowVersion\":"+versionJson+",\"outputData\":{\"marker\":\"snapshot-"+version+"\"}}}";
            } else {
                Object version=segment==null?live:segment;
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
                .workspaceId(workspace).maxRetries(1).callsiteResolver(new CallsiteResolver(name->null,name->null,null,message->{})).build();
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
        c.workflows().invokeVersion("wf@shared:release",3,null); assertTrue(seen.get(0)[1].contains("wf%40shared%3Arelease")); assertNull(seen.get(0)[2]);
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
    @Test void unsafeRawVersionSegmentsHaveZeroEffectsForInvokeAndEveryWaitEntry() {
        SwfteClient c=client("A");
        for(String bad:new String[]{null,"","1.0.7/","1.0.7?x=1","1.0.7#x","../1.0.7",
                ".","..","1.0.7%2Fextra"," 1.0.7","1.0.7 ","1.0.7\n","\n1.0.7","1.0.7\r","1.0.7\0",
                "١.0.7","---._+:@","v3\\extra","1.0.7\t","1.0.7+"+"a".repeat(123)}) {
            assertThrows(SwfteException.class,()->c.workflows().invokeVersion("wf_shared",bad,null));
            assertThrows(SwfteException.class,()->c.workflows().invokeVersion("wf_shared",bad,null,CallSite.of(id)));
            assertThrows(SwfteException.class,()->c.workflows().invokeVersionAndWait("wf_shared",bad,null));
            assertThrows(SwfteException.class,()->c.workflows().invokeVersionAndWait("wf_shared",bad,null,CallSite.of(id)));
            assertThrows(SwfteException.class,()->c.workflows().invokeVersionAndWait("wf_shared",bad,null,5000,1,false));
            assertThrows(SwfteException.class,()->c.workflows().invokeVersionAndWait("wf_shared",bad,null,5000,1,false,CallSite.of(id)));
            assertTrue(seen.isEmpty());
        }
    }


    @Test void safeOpaqueLabelsRequireExactPublishedRecordsForEveryStringInvokeAndWaitOverload() {
        SwfteClient c=client("A"); live=4; published.remove("3");
        for(String label:List.of("v3","v4","custom@v3:release","latest","5","01.0.7","1.0","1.0.7+","1.0.7-","3")) {
            String route="/v2/workflows/wf_shared/versions/"+URLEncoder.encode(label,StandardCharsets.UTF_8).replace("+","%20")+"/invoke";
            int before=seen.size(),oldExecutions=executions.size();
            assertEquals(404,assertThrows(ApiException.class,()->c.workflows().invokeVersion("wf_shared",label,null)).getStatusCode());
            assertEquals(404,assertThrows(ApiException.class,()->c.workflows().invokeVersion("wf_shared",label,null,CallSite.of(id))).getStatusCode());
            assertEquals(404,assertThrows(ApiException.class,()->c.workflows().invokeVersionAndWait("wf_shared",label,null)).getStatusCode());
            assertEquals(404,assertThrows(ApiException.class,()->c.workflows().invokeVersionAndWait("wf_shared",label,null,CallSite.of(id))).getStatusCode());
            assertEquals(404,assertThrows(ApiException.class,()->c.workflows().invokeVersionAndWait("wf_shared",label,null,5000,1,false)).getStatusCode());
            assertEquals(404,assertThrows(ApiException.class,()->c.workflows().invokeVersionAndWait("wf_shared",label,null,5000,1,false,CallSite.of(id))).getStatusCode());
            assertEquals(oldExecutions,executions.size());
            assertEquals(before+6,seen.size());
            for(int i=before;i<seen.size();i++) {
                assertEquals("POST",seen.get(i)[0]); assertEquals(route,seen.get(i)[1]);
                assertEquals((i-before)%2==0?null:id,seen.get(i)[2]);
            }
            published.put(label,"PUBLISHED");
            c.workflows().invokeVersion("wf_shared",label,null);
            assertArrayEquals(new String[]{"POST",route,null},seen.get(seen.size()-1));
            c.workflows().invokeVersion("wf_shared",label,null,CallSite.of(id));
            assertArrayEquals(new String[]{"POST",route,id},seen.get(seen.size()-1));
            Map<String,Object> marker=Map.of("marker","snapshot-"+label);
            assertEquals(marker,c.workflows().invokeVersionAndWait("wf_shared",label,null).getOutputs());
            assertArrayEquals(new String[]{"POST",route,null},seen.get(seen.size()-2));
            assertEquals(marker,c.workflows().invokeVersionAndWait("wf_shared",label,null,CallSite.of(id)).getOutputs());
            assertArrayEquals(new String[]{"POST",route,id},seen.get(seen.size()-2));
            assertEquals(marker,c.workflows().invokeVersionAndWait("wf_shared",label,null,5000,1,false).getOutputs());
            assertArrayEquals(new String[]{"POST",route,null},seen.get(seen.size()-2));
            assertEquals(marker,c.workflows().invokeVersionAndWait("wf_shared",label,null,5000,1,false,CallSite.of(id)).getOutputs());
            assertArrayEquals(new String[]{"POST",route,id},seen.get(seen.size()-2));
        }
        published.put("draft-v3","DRAFT"); int oldExecutions=executions.size();
        for(String label:List.of("draft-v3","unknown-v3")) {
            assertEquals(404,assertThrows(ApiException.class,()->c.workflows().invokeVersionAndWait("wf_shared",label,null)).getStatusCode());
        }
        assertEquals(404,assertThrows(ApiException.class,()->client("B").workflows().invokeVersion("wf_shared","v3",null)).getStatusCode());
        assertEquals(oldExecutions,executions.size());
        published.put("retired@v3:release","DEPRECATED");
        assertEquals(Map.of("marker","snapshot-retired@v3:release"),c.workflows().invokeVersionAndWait("wf_shared","retired@v3:release",null,5000,1,false).getOutputs());
        assertTrue(seen.stream().filter(row->row[1].endsWith("/status")).allMatch(row->"GET".equals(row[0]) && row[2]==null));
        assertTrue(seen.stream().noneMatch(row->row[1].equals("/v2/workflows/wf_shared/invoke")));
    }


    @TempDir Path callerDirectory;
    private static int callingLine() {
        return StackWalker.getInstance().walk(frames->frames.skip(1).findFirst()).orElseThrow().getLineNumber();
    }
    private static final class PinCallerA {
        static int directLine,waitLine,builderLine;
        static void direct(SwfteClient c,Object pin,CallSite explicit) {
            directLine=callingLine(); if(pin instanceof Integer) {if(explicit==null)c.workflows().invokeVersion("wf_shared",(Integer)pin,null);else c.workflows().invokeVersion("wf_shared",(Integer)pin,null,explicit);} else {if(explicit==null)c.workflows().invokeVersion("wf_shared",(String)pin,null);else c.workflows().invokeVersion("wf_shared",(String)pin,null,explicit);}
        }
        static void wait(SwfteClient c,Object pin,CallSite explicit) {
            waitLine=callingLine(); if(pin instanceof Integer) {if(explicit==null)c.workflows().invokeVersionAndWait("wf_shared",(Integer)pin,null);else c.workflows().invokeVersionAndWait("wf_shared",(Integer)pin,null,explicit);} else {if(explicit==null)c.workflows().invokeVersionAndWait("wf_shared",(String)pin,null);else c.workflows().invokeVersionAndWait("wf_shared",(String)pin,null,explicit);}
        }
        static void builder(SwfteClient c,CallSite explicit) {
            builderLine=callingLine(); if(explicit==null)c.chatflows().builder().test("cf_shared",null);else c.chatflows().builder().test("cf_shared",null,explicit);
        }
    }
    private static final class PinCallerB {
        static int directLine,waitLine,builderLine;
        static void direct(SwfteClient c,Object pin,CallSite explicit) {
            directLine=callingLine(); if(pin instanceof Integer) {if(explicit==null)c.workflows().invokeVersion("wf_shared",(Integer)pin,null);else c.workflows().invokeVersion("wf_shared",(Integer)pin,null,explicit);} else {if(explicit==null)c.workflows().invokeVersion("wf_shared",(String)pin,null);else c.workflows().invokeVersion("wf_shared",(String)pin,null,explicit);}
        }
        static void wait(SwfteClient c,Object pin,CallSite explicit) {
            waitLine=callingLine(); if(pin instanceof Integer) {if(explicit==null)c.workflows().invokeVersionAndWait("wf_shared",(Integer)pin,null);else c.workflows().invokeVersionAndWait("wf_shared",(Integer)pin,null,explicit);} else {if(explicit==null)c.workflows().invokeVersionAndWait("wf_shared",(String)pin,null);else c.workflows().invokeVersionAndWait("wf_shared",(String)pin,null,explicit);}
        }
        static void builder(SwfteClient c,CallSite explicit) {
            builderLine=callingLine(); if(explicit==null)c.chatflows().builder().test("cf_shared",null);else c.chatflows().builder().test("cf_shared",null,explicit);
        }
    }
    private static void invokePinCallers(SwfteClient c,CallSite explicit) {
        for(Object pin:List.of(3,"v3")) {
            PinCallerA.direct(c,pin,explicit);PinCallerB.direct(c,pin,explicit);
            PinCallerA.wait(c,pin,explicit);PinCallerB.wait(c,pin,explicit);
        }
        PinCallerA.builder(c,explicit);PinCallerB.builder(c,explicit);
    }
    @Test void numericOpaqueAndBuilderStackCaptureRequireOptInAndRefuseProductionAtTheirCallingLines() throws Exception {
        Map<String,String> environment=new HashMap<>(),properties=new HashMap<>();
        List<String> warnings=new ArrayList<>();
        CallsiteResolver resolver=new CallsiteResolver(environment::get,properties::get,callerDirectory,warnings::add);
        SwfteClient c=SwfteClient.builder().apiKey("unit-test-key")
                .apiBaseUrl("http://127.0.0.1:"+server.getAddress().getPort()).workspaceId("A").maxRetries(1)
                .callsiteResolver(resolver).build();
        published.put("v3","PUBLISHED");
        invokePinCallers(c,null);
        assertTrue(seen.stream().allMatch(row->row[2]==null));
        String self="src/test/java/com/swfte/sdk/CallsiteVersionTest.java:",idA="cs_"+"a".repeat(24),idB="cs_"+"b".repeat(24);
        Map<String,String> entries=Map.of(
                self+PinCallerA.directLine,idA,self+PinCallerB.directLine,idB,
                self+PinCallerA.waitLine,idA,self+PinCallerB.waitLine,idB,
                self+PinCallerA.builderLine,idA,self+PinCallerB.builderLine,idB);
        assertNotEquals(PinCallerA.directLine,PinCallerB.directLine);
        assertNotEquals(PinCallerA.waitLine,PinCallerB.waitLine);
        Path callerMap=callerDirectory.resolve("callers.json");
        Files.write(callerMap,new ObjectMapper().writeValueAsBytes(Map.of("version",1,"root",callerDirectory.toAbsolutePath().toString(),"entries",entries)));
        environment.put(CallsiteResolver.ENV_CALLERS,callerMap.toString());
        environment.put(CallsiteResolver.ENV_STACK,"1");seen.clear();
        invokePinCallers(c,null);
        assertEquals(List.of(idA,idB,idA,idB,idA,idB,idA,idB,idA,idB),seen.stream().filter(row->"POST".equals(row[0])).map(row->row[2]).collect(java.util.stream.Collectors.toList()));
        assertTrue(seen.stream().filter(row->row[1].endsWith("/status")).allMatch(row->"GET".equals(row[0]) && row[2]==null));
        for(boolean propertyProduction:new boolean[]{true,false}) {
            environment.remove(CallsiteResolver.ENV_ENV);properties.remove(CallsiteResolver.PROP_ENV);
            if(propertyProduction) {
                properties.put(CallsiteResolver.PROP_ENV,"production");
            } else {
                environment.remove(CallsiteResolver.ENV_STACK);properties.put(CallsiteResolver.PROP_STACK,"1");
                seen.clear();PinCallerA.direct(c,"v3",null);assertEquals(idA,seen.get(0)[2]);
                environment.put(CallsiteResolver.ENV_ENV,"production");
            }
            seen.clear();invokePinCallers(c,null);
            assertTrue(seen.stream().allMatch(row->row[2]==null));
            seen.clear();invokePinCallers(c,CallSite.of(id));
            assertTrue(seen.stream().filter(row->"POST".equals(row[0])).allMatch(row->id.equals(row[2])));
            assertTrue(seen.stream().filter(row->"GET".equals(row[0])).allMatch(row->row[2]==null));
        }
        assertEquals(1,warnings.size());
    }

    @Test void realRedirectDoesNotForwardCredentialOrCallsiteToSecondListener() throws Exception {
        AtomicInteger forwarded=new AtomicInteger(); HttpServer canary=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        canary.createContext("/",exchange->{forwarded.incrementAndGet(); exchange.sendResponseHeaders(200,-1);exchange.close();});canary.start();
        try {redirect="http://127.0.0.1:"+canary.getAddress().getPort()+"/canary";
            assertThrows(SwfteException.class,()->client("A").workflows().invokeVersion("wf_shared",3,null,CallSite.of(id)));
            assertEquals(id,seen.get(0)[2]); assertEquals(0,forwarded.get());
        } finally {canary.stop(0);}
    }

    private List<Runnable> workflowEntries(SwfteClient c,String wf) {
        return List.of(
            ()->c.workflows().execute(wf,INPUT),()->c.workflows().execute(wf,INPUT,true),
            ()->c.workflows().execute(wf,INPUT,CallSite.of(id)),()->c.workflows().execute(wf,INPUT,true,CallSite.of(id)),
            ()->c.workflows().invoke(wf,INPUT),()->c.workflows().invoke(wf,INPUT,CallSite.of(id)),
            ()->c.workflows().invokeVersion(wf,3,INPUT),()->c.workflows().invokeVersion(wf,3,INPUT,CallSite.of(id)),
            ()->c.workflows().invokeVersion(wf,"1.0.7",INPUT),()->c.workflows().invokeVersion(wf,"1.0.7",INPUT,CallSite.of(id)),
            ()->c.workflows().invokeAndWait(wf,INPUT),()->c.workflows().invokeAndWait(wf,INPUT,5000,1),
            ()->c.workflows().invokeAndWait(wf,INPUT,5000,1,false),()->c.workflows().invokeAndWait(wf,INPUT,CallSite.of(id)),
            ()->c.workflows().invokeAndWait(wf,INPUT,5000,1,CallSite.of(id)),()->c.workflows().invokeAndWait(wf,INPUT,5000,1,false,CallSite.of(id)),
            ()->c.workflows().invokeVersionAndWait(wf,3,INPUT),()->c.workflows().invokeVersionAndWait(wf,3,INPUT,CallSite.of(id)),
            ()->c.workflows().invokeVersionAndWait(wf,3,INPUT,5000,1,false),()->c.workflows().invokeVersionAndWait(wf,3,INPUT,5000,1,false,CallSite.of(id)),
            ()->c.workflows().invokeVersionAndWait(wf,"1.0.7",INPUT),()->c.workflows().invokeVersionAndWait(wf,"1.0.7",INPUT,CallSite.of(id)),
            ()->c.workflows().invokeVersionAndWait(wf,"1.0.7",INPUT,5000,1,false),()->c.workflows().invokeVersionAndWait(wf,"1.0.7",INPUT,5000,1,false,CallSite.of(id)));
    }
    @Test void workflowIdentifierBoundaryRefusesBeforeTransportAndRetainsLegalPunctuation() {
        SwfteClient c=client("A");
        for(String wf:new String[]{null,"",".","..","../other","a/b","a\\b","a?x=1","a#x","a%2fother"," a","a ","a\n","a\r","a\0","é","a+b","a".repeat(129)}) {
            for(Runnable run:workflowEntries(c,wf)) assertThrows(SwfteException.class,run::run);
            assertTrue(seen.isEmpty());assertTrue(executions.isEmpty());
        }
        for(String wf:List.of("wf_shared","@:-","...","wf@release:v1","a".repeat(128))) {
            int first=seen.size();for(Runnable run:workflowEntries(c,wf)) run.run();
            String prefix="/v2/workflows/"+URLEncoder.encode(wf,StandardCharsets.UTF_8);
            List<String[]> rows=seen.subList(first,seen.size());
            for(int index=0;index<rows.size();index++) assertEquals("POST".equals(rows.get(index)[0])?INPUT:null,bodies.get(first+index));
            List<String[]> posts=rows.stream().filter(r->"POST".equals(r[0])).toList();
            assertEquals(24,posts.size());
            for(int i=0;i<posts.size();i++) {
                String suffix=i<4?"/execute":i<6||i>=10&&i<16?"/invoke":i<8||i>=16&&i<20?"/versions/3/invoke":"/versions/1.0.7/invoke";
                assertEquals(prefix+suffix+(i==1||i==3?"?skipValidation=true":""),posts.get(i)[1]);
                boolean explicit=i==2||i==3||i==5||i==7||i==9||i>=13&&i<16||i==17||i==19||i==21||i==23;
                assertEquals(explicit?id:null,posts.get(i)[2]);
            }
            assertTrue(rows.stream().filter(r->"GET".equals(r[0])).allMatch(r->r[2]==null));
        }
    }
}
