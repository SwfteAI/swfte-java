package com.swfte.sdk;

import com.swfte.sdk.*;
import com.swfte.sdk.exceptions.SwfteException;
import com.swfte.sdk.models.AgentChatOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;

class NonWorkflowRouteBoundaryTest {
 static final String ID="cs_"+"a".repeat(24),OTHER="cs_"+"b".repeat(24),EXPLICIT="cs_"+"c".repeat(24);
 static final ObjectMapper JSON=new ObjectMapper();
 static final Map<String,Object> INPUT=Map.of("label","nonworkflow-wire","nested",Map.of("enabled",true,"values",Arrays.asList(1,"two",null)));
 static void check(boolean value,String reason){if(!value)throw new AssertionError(reason);}
 static void refused(Runnable run){try{run.run();}catch(SwfteException expected){return;}throw new AssertionError("expected refusal");}
 static String encoded(String s){return URLEncoder.encode(s,StandardCharsets.UTF_8).replace("+","%20");}
 static List<Runnable> entries(SwfteClient c,String value){return List.of(
  ()->c.agents().chat(value,"hello"),()->c.agents().chat(value,"hello",AgentChatOptions.builder().conversationId("conversation").userId("legacy user").build()),
  ()->c.agents().chat(value,"hello",AgentChatOptions.builder().conversationId("conversation").userId("legacy user").build(),CallSite.of(ID)),
  ()->c.chatflows().startSession(value,INPUT),()->c.chatflows().startSession(value,INPUT,CallSite.of(ID)),
  ()->c.chatflows().builder().test(value,INPUT),()->c.chatflows().builder().test(value,INPUT,CallSite.of(ID)));}
 static List<Runnable> readers(SwfteClient c,String value){return List.of(()->c.chatflows().getSession(value),()->c.chatflows().listSessions(value),()->c.chatflows().stats(value));}
 static class CallerA {
  static int agent,session,builder;
  static void run(SwfteClient c,CallSite cs){
   agent=StackWalker.getInstance().walk(s->s.findFirst().orElseThrow().getLineNumber());c.agents().chat("agent","hello",null,cs);
   session=StackWalker.getInstance().walk(s->s.findFirst().orElseThrow().getLineNumber());c.chatflows().startSession("flow",null,cs);
   builder=StackWalker.getInstance().walk(s->s.findFirst().orElseThrow().getLineNumber());c.chatflows().builder().test("flow",null,cs);
  }
 }
 static class CallerB {
  static int agent,session,builder;
  static void run(SwfteClient c,CallSite cs){
   agent=StackWalker.getInstance().walk(s->s.findFirst().orElseThrow().getLineNumber());c.agents().chat("agent","hello",null,cs);
   session=StackWalker.getInstance().walk(s->s.findFirst().orElseThrow().getLineNumber());c.chatflows().startSession("flow",null,cs);
   builder=StackWalker.getInstance().walk(s->s.findFirst().orElseThrow().getLineNumber());c.chatflows().builder().test("flow",null,cs);
  }
 }
 static void callers(SwfteClient c,CallSite cs){CallerA.run(c,cs);CallerB.run(c,cs);}
 static List<String> headers(List<Map<String,Object>> seen,int first){return seen.subList(first,seen.size()).stream().map(r->(String)r.get("header")).toList();}
 static int exercise() throws Exception {
  List<Map<String,Object>> seen=new ArrayList<>(),forwarded=new ArrayList<>();int[] redirect={0};
  HttpServer canary=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0),server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
  canary.createContext("/",e->{Map<String,Object> row=new HashMap<>();row.put("path",e.getRequestURI().toString());row.put("headers",e.getRequestHeaders());row.put("body",new String(e.getRequestBody().readAllBytes(),StandardCharsets.UTF_8));forwarded.add(row);e.sendResponseHeaders(200,-1);e.close();});canary.start();
  server.createContext("/",e->{String path=e.getRequestURI().toString();byte[] body=e.getRequestBody().readAllBytes();Map<String,Object> row=new HashMap<>();row.put("path",path);row.put("method",e.getRequestMethod());row.put("header",e.getRequestHeaders().getFirst("X-Swfte-Callsite"));row.put("body",body.length==0?null:JSON.readValue(body,Map.class));seen.add(row);
   int status=redirect[0]>0?redirect[0]:path.contains("missing")||"B".equals(e.getRequestHeaders().getFirst("X-Workspace-Id"))?404:path.contains("native-unavailable")?501:200;
   if(redirect[0]>0)e.getResponseHeaders().add("Location","http://127.0.0.1:"+canary.getAddress().getPort()+"/capture");
   byte[] reply="{\"response\":\"ok\",\"sessionId\":\"cfs_fixture\",\"runId\":\"fixture-only\"}".getBytes(StandardCharsets.UTF_8);e.getResponseHeaders().add("Content-Type","application/json");e.sendResponseHeaders(status,reply.length);e.getResponseBody().write(reply);e.close();});server.start();
  Path temp=Files.createTempDirectory("nonworkflow-callers-");Map<String,String> env=new HashMap<>(),props=new HashMap<>();
  CallsiteResolver resolver=new CallsiteResolver(env::get,props::get,temp,s->{});
  String base="http://127.0.0.1:"+server.getAddress().getPort();SwfteClient c=SwfteClient.builder().apiKey("unit-only-key").apiBaseUrl(base).workspaceId("A").maxRetries(1).callsiteResolver(resolver).build();
  try {
   for(String value:new String[]{null,"",".","..","a\n","a\r","a\0","a\u007f","\ud800","\udc00"}){for(Runnable run:entries(c,value))refused(run);for(Runnable run:readers(c,value))refused(run);check(seen.isEmpty(),"unsafe transport");}
   for(String value:List.of("legacy /\\?#%2e: @é😀","...","@:-","system-agent","avima-runtime-agent","a".repeat(256))){int first=seen.size();for(Runnable run:entries(c,value))run.run();String component=encoded(value);
    String[] routes={"/v1/agents/"+component+"/chat/sdk-user","/v1/agents/"+component+"/chat/legacy%20user","/v1/agents/"+component+"/chat/legacy%20user","/v2/chatflows/"+component+"/sessions","/v2/chatflows/"+component+"/sessions","/v2/chatflows/builder/"+component+"/test","/v2/chatflows/builder/"+component+"/test"};
    for(int i=0;i<7;i++){Map<String,Object> row=seen.get(first+i);check(routes[i].equals(row.get("path")),"literal route");check(Objects.equals(i==2||i==4||i==6?ID:null,row.get("header")),"default/explicit attribution");}
    for(int i=0;i<7;i++){Object expected=i==0?Map.of("message","hello"):i<3?Map.of("message","hello","conversationId","conversation"):INPUT;check(expected.equals(seen.get(first+i).get("body")),"all overload bodies "+i);check("POST".equals(seen.get(first+i).get("method")),"all overload methods");}
   }
   for(String user:new String[]{null,""}){c.agents().chat("agent","hello",AgentChatOptions.builder().userId(user).build());check("/v1/agents/agent/chat/sdk-user".equals(seen.get(seen.size()-1).get("path")),"user fallback");}
   for(String user:new String[]{".","..","a\n","\ud800"}){int first=seen.size();refused(()->c.agents().chat("agent","hello",AgentChatOptions.builder().userId(user).build()));check(seen.size()==first,"unsafe user transport");}
   c.agents().chat("agent","hello",AgentChatOptions.builder().userId("opaque /\\?#%é😀").build());check(("/v1/agents/agent/chat/"+encoded("opaque /\\?#%é😀")).equals(seen.get(seen.size()-1).get("path")),"opaque user");
   for(String value:List.of("cfs_"+"a".repeat(32),"avima-session-cfs_"+"b".repeat(32),"legacy session /%é")){int first=seen.size();for(Runnable run:readers(c,value))run.run();String component=encoded(value);String[] expected={"/v2/chatflows/sessions/"+component,"/v2/chatflows/"+component+"/sessions","/v2/chatflows/"+component+"/stats"};for(int i=0;i<3;i++){Map<String,Object> row=seen.get(first+i);check(expected[i].equals(row.get("path")),"all reader routes/query");check("GET".equals(row.get("method"))&&row.get("body")==null&&row.get("header")==null,"all reader wire");}}
   for(Map<String,Object> payload:Arrays.<Map<String,Object>>asList(null,Map.of())){int fallback=seen.size();c.chatflows().startSession("flow",payload);c.chatflows().startSession("flow",payload,CallSite.of(ID));c.chatflows().builder().test("flow",payload);c.chatflows().builder().test("flow",payload,CallSite.of(ID));for(Map<String,Object> row:seen.subList(fallback,seen.size()))check(Map.of().equals(row.get("body")),"null/empty fallback bodies");}
   int first=seen.size();callers(c,null);check(headers(seen,first).stream().allMatch(Objects::isNull),"default stack off");
   String self="com/swfte/sdk/"+NonWorkflowRouteBoundaryTest.class.getSimpleName()+".java:";Map<String,String> lines=Map.of(self+CallerA.agent,ID,self+CallerA.session,ID,self+CallerA.builder,ID,self+CallerB.agent,OTHER,self+CallerB.session,OTHER,self+CallerB.builder,OTHER);
   Path map=temp.resolve("callers.json");Files.write(map,JSON.writeValueAsBytes(Map.of("version",1,"root",temp.toAbsolutePath().toString(),"entries",lines)));env.put(CallsiteResolver.ENV_CALLERS,map.toString());env.put(CallsiteResolver.ENV_STACK,"1");
   first=seen.size();callers(c,null);check(headers(seen,first).equals(List.of(ID,ID,ID,OTHER,OTHER,OTHER)),"two stack caller lines");
   first=seen.size();callers(c,CallSite.of(EXPLICIT));check(headers(seen,first).stream().allMatch(EXPLICIT::equals),"explicit precedence");
   first=seen.size();callers(c,CallSite.of("invalid"));check(headers(seen,first).stream().allMatch(Objects::isNull),"invalid explicit omission");
   for(boolean property:new boolean[]{false,true}){if(property)props.put(CallsiteResolver.PROP_ENV,"production");else env.put(CallsiteResolver.ENV_ENV,"production");first=seen.size();callers(c,null);check(headers(seen,first).stream().allMatch(Objects::isNull),"production refusal");first=seen.size();callers(c,CallSite.of(EXPLICIT));check(headers(seen,first).stream().allMatch(EXPLICIT::equals),"production explicit");props.remove(CallsiteResolver.PROP_ENV);env.remove(CallsiteResolver.ENV_ENV);}
   first=seen.size();for(Runnable run:readers(c,"cfs_read"))run.run();check(headers(seen,first).stream().allMatch(Objects::isNull),"opt-in readers");
   for(int status:new int[]{301,302,303,307,308}){redirect[0]=status;for(Runnable run:entries(c,"flow"))refused(run);for(Runnable run:readers(c,"cfs_read"))refused(run);check(forwarded.isEmpty(),"redirect forwarded credentials/body/callsite");}
   redirect[0]=0;for(Runnable run:entries(c,"missing"))refused(run);refused(()->c.chatflows().builder().test("native-unavailable",null));
   SwfteClient foreign=SwfteClient.builder().apiKey("unit-only-key").apiBaseUrl(base).workspaceId("B").maxRetries(1).build();refused(()->foreign.agents().chat("agent","hello"));refused(()->foreign.chatflows().startSession("flow",null));refused(()->foreign.chatflows().builder().test("flow",null));check(!seen.isEmpty(),"positive wire count");return seen.size();
  }finally{server.stop(0);canary.stop(0);Files.deleteIfExists(temp.resolve("callers.json"));Files.deleteIfExists(temp);}
 }
 @org.junit.jupiter.api.Test void actualNonworkflowWireAndRedirectEffects()throws Exception{check(exercise()>0,"wire positive");}
}
