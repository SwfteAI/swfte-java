import com.sun.net.httpserver.HttpServer;
import com.swfte.sdk.CallSite;
import com.swfte.sdk.CallsiteResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.exceptions.SwfteException;
import com.swfte.sdk.exceptions.ApiException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.concurrent.ConcurrentHashMap;
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
    private static final Map<String,Object> INPUT=Map.of("label","workflow-wire","nested",Map.of("enabled",true,"values",java.util.Arrays.asList(1,"two",null)));
    public static void main(String[] args) throws Exception {
        Path loaded = Path.of(SwfteClient.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toRealPath();
        if (!Files.isRegularFile(loaded) || !loaded.getFileName().toString().endsWith(".jar") || !loaded.equals(Path.of(args[0]).toRealPath())) throw new AssertionError("SDK was not loaded from isolated packaged JAR");
        String jarSha256=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(loaded)));
        Map<Integer,Object> bodies=new HashMap<>();
        List<String[]> seen = new ArrayList<>();
        Map<String,String> executions = new HashMap<>();
        Map<String,String> published=new ConcurrentHashMap<>();
        for(String label:List.of("3","1.0.7","1.0.7-rc.2+build.09")) published.put(label,"PUBLISHED");
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            byte[] raw=exchange.getRequestBody().readAllBytes();bodies.put(seen.size(),raw.length==0?null:new ObjectMapper().readValue(raw,Object.class));
            String path = exchange.getRequestURI().getRawPath();
            String wirePath=path+(exchange.getRequestURI().getRawQuery()==null?"":"?"+exchange.getRequestURI().getRawQuery());
            seen.add(new String[]{exchange.getRequestMethod(),wirePath,exchange.getRequestHeaders().getFirst("X-Swfte-Callsite")});
            String json; int status=200;
            String version=path.contains("/versions/")?URLDecoder.decode(path.split("/versions/")[1].split("/")[0],StandardCharsets.UTF_8):"live";
            if("B".equals(exchange.getRequestHeaders().getFirst("X-Workspace-Id")) || (path.contains("/versions/") && !List.of("PUBLISHED","DEPRECATED").contains(published.getOrDefault(version,"")))) {
                status=404; json="{\"error\":\"VERSION_NOT_PUBLISHED\"}";
            } else if(path.endsWith("/status")) {
                String[] pieces=path.split("/");String eid=pieces[pieces.length-2];
                json="{\"execution\":{\"executionId\":\""+eid+"\",\"status\":\"SUCCEEDED\",\"outputData\":{\"marker\":\"snapshot-"+executions.get(eid)+"\"}}}";
            } else {
                String eid="packed_"+executions.size();executions.put(eid,version);
                json="{\"executionId\":\""+eid+"\",\"status\":\"PENDING\",\"sessionId\":\"session\",\"response\":\"ok\",\"runId\":\"run\"}";
            }
            byte[] data = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, data.length);
            exchange.getResponseBody().write(data); exchange.close();
        });
        server.start();
        try {
            String id = "cs_" + "a".repeat(24);
            SwfteClient client = SwfteClient.builder().apiKey("packed-unit-key")
                    .apiBaseUrl("http://127.0.0.1:" + server.getAddress().getPort()).workspaceId("A").maxRetries(1).callsiteResolver(new CallsiteResolver(name->null,name->null,null,message->{})).build();
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
            List<String> labels=List.of("v3","v4","custom@v3:release","latest","5","01.0.7","1.0","1.0.7+","1.0.7-","3");
            for(String label:labels) published.remove(label);
            for(String label:labels) {
                String route="/v2/workflows/wf_packed/versions/"+URLEncoder.encode(label,StandardCharsets.UTF_8).replace("+","%20")+"/invoke";
                int before=seen.size(),oldExecutions=executions.size();
                assertAbsent(()->client.workflows().invokeVersion("wf_packed",label,null));
                assertAbsent(()->client.workflows().invokeVersion("wf_packed",label,null,CallSite.of(id)));
                assertAbsent(()->client.workflows().invokeVersionAndWait("wf_packed",label,null));
                assertAbsent(()->client.workflows().invokeVersionAndWait("wf_packed",label,null,CallSite.of(id)));
                assertAbsent(()->client.workflows().invokeVersionAndWait("wf_packed",label,null,5000,1,false));
                assertAbsent(()->client.workflows().invokeVersionAndWait("wf_packed",label,null,5000,1,false,CallSite.of(id)));
                if(executions.size()!=oldExecutions || seen.size()!=before+6) throw new AssertionError("absent label created an execution or polled");
                for(int i=before;i<seen.size();i++) assertPost(seen.get(i),route,(i-before)%2==0?null:id);
                published.put(label,"PUBLISHED");
                client.workflows().invokeVersion("wf_packed",label,null);
                assertPost(seen.get(seen.size()-1),route,null);
                client.workflows().invokeVersion("wf_packed",label,null,CallSite.of(id));
                assertPost(seen.get(seen.size()-1),route,id);
                Map<String,Object> marker=Map.of("marker","snapshot-"+label);
                if(!marker.equals(client.workflows().invokeVersionAndWait("wf_packed",label,null).getOutputs())) throw new AssertionError("wrong default snapshot");
                assertPost(seen.get(seen.size()-2),route,null);
                if(!marker.equals(client.workflows().invokeVersionAndWait("wf_packed",label,null,CallSite.of(id)).getOutputs())) throw new AssertionError("wrong attributed snapshot");
                assertPost(seen.get(seen.size()-2),route,id);
                if(!marker.equals(client.workflows().invokeVersionAndWait("wf_packed",label,null,5000,1,false).getOutputs())) throw new AssertionError("wrong timed snapshot");
                assertPost(seen.get(seen.size()-2),route,null);
                if(!marker.equals(client.workflows().invokeVersionAndWait("wf_packed",label,null,5000,1,false,CallSite.of(id)).getOutputs())) throw new AssertionError("wrong full-options snapshot");
                assertPost(seen.get(seen.size()-2),route,id);
            }
            published.put("draft-v3","DRAFT"); int oldExecutions=executions.size();
            assertAbsent(()->client.workflows().invokeVersionAndWait("wf_packed","draft-v3",null));
            assertAbsent(()->client.workflows().invokeVersionAndWait("wf_packed","unknown-v3",null));
            SwfteClient foreign=SwfteClient.builder().apiKey("packed-unit-key")
                    .apiBaseUrl("http://127.0.0.1:"+server.getAddress().getPort()).workspaceId("B").maxRetries(1).callsiteResolver(new CallsiteResolver(name->null,name->null,null,message->{})).build();
            assertAbsent(()->foreign.workflows().invokeVersion("wf_packed","v3",null));
            if(executions.size()!=oldExecutions) throw new AssertionError("unpublished or foreign pin ran");
            published.put("retired@v3:release","DEPRECATED");
            if(!Map.of("marker","snapshot-retired@v3:release").equals(client.workflows().invokeVersionAndWait("wf_packed","retired@v3:release",null,5000,1,false).getOutputs())) throw new AssertionError("wrong exact deprecated record");
            int first=seen.size();
            client.workflows().execute("wf_packed",null);
            client.workflows().execute("wf_packed",null,false);
            client.workflows().execute("wf_packed",null,CallSite.of(id));
            client.workflows().execute("wf_packed",null,false,CallSite.of(id));
            client.workflows().invoke("wf_packed",null);
            client.workflows().invoke("wf_packed",null,CallSite.of(id));
            client.workflows().invokeAndWait("wf_packed",null);
            client.workflows().invokeAndWait("wf_packed",null,5000,1);
            client.workflows().invokeAndWait("wf_packed",null,5000,1,false);
            client.workflows().invokeAndWait("wf_packed",null,CallSite.of(id));
            client.workflows().invokeAndWait("wf_packed",null,5000,1,CallSite.of(id));
            client.workflows().invokeAndWait("wf_packed",null,5000,1,false,CallSite.of(id));
            client.agents().chat("ag_packed","hello");
            client.agents().chat("ag_packed","hello",null);
            client.agents().chat("ag_packed","hello",null,CallSite.of(id));
            client.chatflows().startSession("cf_packed",null);
            client.chatflows().startSession("cf_packed",null,CallSite.of(id));
            client.chatflows().builder().test("cf_packed",null);
            client.chatflows().builder().test("cf_packed",null,CallSite.of(id));
            List<String[]> branches=seen.subList(first,seen.size());
            if(branches.size()!=25) throw new AssertionError("missing an installed runtime overload");
            List<String[]> posts=branches.stream().filter(row->"POST".equals(row[0])).toList();
            String[] attribution={null,null,id,id,null,id,null,null,null,id,id,id,null,null,id,null,id,null,id};
            if(posts.size()!=attribution.length) throw new AssertionError("wrong artifact POST count");
            for(int i=0;i<posts.size();i++) if(!java.util.Objects.equals(attribution[i],posts.get(i)[2])) throw new AssertionError("wrong default/explicit attribution branch");

            int count=seen.size();
            for(String bad:new String[]{"","---._+:@",".","..","1.0.7/","1.0.7?x=1","1.0.7#x","../1.0.7","1.0.7%2Fextra","1.0.7\n","1.0.7\r","1.0.7\0"," v3","v3 ","v3\\extra","١.0.7","1.0.7+"+"a".repeat(123)}) {
                boolean invoked=false;try {client.workflows().invokeVersion("wf_packed",bad,null);} catch(SwfteException expected){invoked=true;}
                boolean waited=false;try {client.workflows().invokeVersionAndWait("wf_packed",bad,null);} catch(SwfteException expected){waited=true;}
                if(!invoked || !waited || seen.size()!=count) throw new AssertionError("malformed pin reached transport");
            }
            if(seen.size()!=197 || seen.stream().filter(row->row[1].endsWith("/status")).anyMatch(row->row[2]!=null)) throw new AssertionError("wrong poll attribution/count");
            int boundaryStart=seen.size(),boundaryExecutions=executions.size();
        for(String wf:new String[]{null,"",".","..","../other","a/b","a\\b","a?x=1","a#x","a%2fother"," a","a ","a\n","a\r","a\0","é","a+b","a".repeat(129)}) {
            for(Runnable run:workflowEntries(client,wf)) {try {run.run();throw new AssertionError("unsafe workflow ID accepted");}catch(SwfteException expected){}}
            if(seen.size()!=boundaryStart || executions.size()!=boundaryExecutions) throw new AssertionError("unsafe ID transport");
        }
        for(String wf:List.of("wf_shared","@:-","...","wf@release:v1","a".repeat(128))) {
            int boundaryFirst=seen.size();for(Runnable run:workflowEntries(client,wf)) run.run();
            String prefix="/v2/workflows/"+URLEncoder.encode(wf,StandardCharsets.UTF_8);
            List<String[]> rows=seen.subList(boundaryFirst,seen.size());
            for(int index=0;index<rows.size();index++) if(!java.util.Objects.equals("POST".equals(rows.get(index)[0])?INPUT:null,bodies.get(boundaryFirst+index))) throw new AssertionError("wrong workflow input wire body");
            List<String[]> boundaryPosts=rows.stream().filter(r->"POST".equals(r[0])).toList();
            if(boundaryPosts.size()!=24) throw new AssertionError("missing workflow overload");
            for(int i=0;i<boundaryPosts.size();i++) {
                String suffix=i<4?"/execute":i<6||i>=10&&i<16?"/invoke":i<8||i>=16&&i<20?"/versions/3/invoke":"/versions/1.0.7/invoke";
                if(!(prefix+suffix+(i==1||i==3?"?skipValidation=true":"")).equals(boundaryPosts.get(i)[1])) throw new AssertionError("wrong ID route");
                boolean explicit=i==2||i==3||i==5||i==7||i==9||i>=13&&i<16||i==17||i==19||i==21||i==23;
                if(!java.util.Objects.equals(explicit?id:null,boundaryPosts.get(i)[2])) throw new AssertionError("wrong ID attribution");
            }
            if(rows.stream().filter(r->"GET".equals(r[0])).anyMatch(r->r[2]!=null)) throw new AssertionError("poll attribution");
        }

            System.out.println("SDK_JAVA_PACKED_OK isolated-jar requests="+seen.size()+" semanticStrings=exact legacyStrings=exact-published-records unsafeStringTransportRequests=0 jarSha256="+jarSha256);
        } finally { server.stop(0); }
    }
    private static void assertAbsent(Runnable request) {
        try {request.run();} catch(ApiException error) {
            if(error.getStatusCode()!=404) throw new AssertionError("wrong exact-record refusal",error);
            return;
        }
        throw new AssertionError("unpublished pin was accepted");
    }
    private static void assertPost(String[] seen,String path,String id) {
        if(!"POST".equals(seen[0]) || !path.equals(seen[1]) || !java.util.Objects.equals(id,seen[2])) throw new AssertionError("wrong invocation attribution/pin");
    }

    private static List<Runnable> workflowEntries(SwfteClient c,String wf) {
        return List.of(
            ()->c.workflows().execute(wf,INPUT),()->c.workflows().execute(wf,INPUT,true),
            ()->c.workflows().execute(wf,INPUT,CallSite.of("cs_"+"a".repeat(24))),()->c.workflows().execute(wf,INPUT,true,CallSite.of("cs_"+"a".repeat(24))),
            ()->c.workflows().invoke(wf,INPUT),()->c.workflows().invoke(wf,INPUT,CallSite.of("cs_"+"a".repeat(24))),
            ()->c.workflows().invokeVersion(wf,3,INPUT),()->c.workflows().invokeVersion(wf,3,INPUT,CallSite.of("cs_"+"a".repeat(24))),
            ()->c.workflows().invokeVersion(wf,"1.0.7",INPUT),()->c.workflows().invokeVersion(wf,"1.0.7",INPUT,CallSite.of("cs_"+"a".repeat(24))),
            ()->c.workflows().invokeAndWait(wf,INPUT),()->c.workflows().invokeAndWait(wf,INPUT,5000,1),
            ()->c.workflows().invokeAndWait(wf,INPUT,5000,1,false),()->c.workflows().invokeAndWait(wf,INPUT,CallSite.of("cs_"+"a".repeat(24))),
            ()->c.workflows().invokeAndWait(wf,INPUT,5000,1,CallSite.of("cs_"+"a".repeat(24))),()->c.workflows().invokeAndWait(wf,INPUT,5000,1,false,CallSite.of("cs_"+"a".repeat(24))),
            ()->c.workflows().invokeVersionAndWait(wf,3,INPUT),()->c.workflows().invokeVersionAndWait(wf,3,INPUT,CallSite.of("cs_"+"a".repeat(24))),
            ()->c.workflows().invokeVersionAndWait(wf,3,INPUT,5000,1,false),()->c.workflows().invokeVersionAndWait(wf,3,INPUT,5000,1,false,CallSite.of("cs_"+"a".repeat(24))),
            ()->c.workflows().invokeVersionAndWait(wf,"1.0.7",INPUT),()->c.workflows().invokeVersionAndWait(wf,"1.0.7",INPUT,CallSite.of("cs_"+"a".repeat(24))),
            ()->c.workflows().invokeVersionAndWait(wf,"1.0.7",INPUT,5000,1,false),()->c.workflows().invokeVersionAndWait(wf,"1.0.7",INPUT,5000,1,false,CallSite.of("cs_"+"a".repeat(24))));
    }
}
