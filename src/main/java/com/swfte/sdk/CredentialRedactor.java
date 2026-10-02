package com.swfte.sdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swfte.sdk.models.WorkflowExecution;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/** Copies failure diagnostics only; successful SDK values must not be redacted. */
public final class CredentialRedactor {
    private final List<Pattern> patterns = new ArrayList<>();
    private final String marker;

    public CredentialRedactor(String credential) {
        Set<String> literals = new LinkedHashSet<>();
        if (credential != null && !credential.isEmpty()) {
            literals.add(credential);
            try {
                String quoted = new ObjectMapper().writeValueAsString(credential);
                literals.add(quoted.substring(1, quoted.length() - 1));
            } catch (JsonProcessingException ignored) { /* raw literal still protected */ }
            String form = URLEncoder.encode(credential, StandardCharsets.UTF_8);
            literals.add(form);
            literals.add(form.replace("+", "%20"));
            String component = encodeComponent(credential);
            literals.add(component);
            String uri = component;
            for (String reserved : new String[]{";", "/", "?", ":", "@", "&", "=", "+", "$", ",", "#"}) {
                uri = uri.replace(URLEncoder.encode(reserved, StandardCharsets.UTF_8), reserved);
            }
            literals.add(uri);
            for (String literal : new ArrayList<>(literals)) {
                literals.add(URLEncoder.encode(literal, StandardCharsets.UTF_8));
                literals.add(encodeComponent(literal));
            }
        }
        marker = literals.stream().anyMatch(literal -> "[REDACTED]".contains(literal)) ? "*" : "[REDACTED]";
        literals.stream().sorted(Comparator.comparingInt(String::length).reversed()).forEach(literal -> {
            StringBuilder expression = new StringBuilder();
            for (int i = 0; i < literal.length(); i++) {
                char c = literal.charAt(i);
                if (c == '%' && i + 2 < literal.length()
                        && Character.digit(literal.charAt(i + 1), 16) >= 0 && Character.digit(literal.charAt(i + 2), 16) >= 0) {
                    expression.append('%');
                    for (int n = 0; n < 2; n++) {
                        char hex = literal.charAt(++i);
                        expression.append(Character.isLetter(hex) ? "[" + Character.toLowerCase(hex) + Character.toUpperCase(hex) + "]" : String.valueOf(hex));
                    }
                } else {
                    expression.append(Pattern.quote(String.valueOf(c)));
                }
            }
            patterns.add(Pattern.compile(expression.toString()));
        });
    }

    private static String encodeComponent(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20").replace("%7E", "~").replace("%21", "!")
            .replace("%27", "'").replace("%28", "(").replace("%29", ")");
    }

    public String text(String value) {
        if (value == null) return null;
        for (Pattern pattern : patterns) value = pattern.matcher(value).replaceAll(java.util.regex.Matcher.quoteReplacement(marker));
        return value;
    }

    /** Detached JSON-shaped diagnostic data, with bounded recursion and no original references. */
    @SuppressWarnings("unchecked")
    public <T> T data(T value) {
        return (T) copy(value, new IdentityHashMap<>(), 0);
    }

    private Object copy(Object value, IdentityHashMap<Object, Object> seen, int depth) {
        if (value == null || value instanceof Number || value instanceof Boolean) return value;
        if (value instanceof String) return text((String) value);
        if (seen.containsKey(value)) return seen.get(value);
        if (depth > 32 || seen.size() >= 5000) return text("[diagnostic omitted]");
        if (value instanceof Map) {
            Map<String, Object> out = new LinkedHashMap<>();
            seen.put(value, out);
            ((Map<?, ?>) value).forEach((key, child) -> out.put(text(String.valueOf(key)), copy(child, seen, depth + 1)));
            return out;
        }
        if (value instanceof Iterable) {
            List<Object> out = new ArrayList<>();
            seen.put(value, out);
            for (Object child : (Iterable<?>) value) out.add(copy(child, seen, depth + 1));
            return out;
        }
        return text(String.valueOf(value));
    }

    /** Drop parser/connection objects that can retain the raw response or headers. */
    public Throwable cause(Throwable value) {
        return copyCause(value, new IdentityHashMap<>(), 0);
    }

    private Throwable copyCause(Throwable value, IdentityHashMap<Throwable, Throwable> seen, int depth) {
        if (value == null) return null;
        if (seen.containsKey(value)) return seen.get(value);
        if (depth > 32 || seen.size() >= 100) return new IOException(text("[diagnostic omitted]"));
        String message = text(value.getMessage());
        Throwable out;
        if (value instanceof SocketTimeoutException) out = new SocketTimeoutException(message);
        else if (value instanceof InterruptedIOException) out = new InterruptedIOException(message);
        else if (value instanceof IOException) out = new IOException(message);
        else if (value instanceof InterruptedException) out = new InterruptedException(message);
        else if (value instanceof RuntimeException) out = new RuntimeException(message);
        else out = new Exception(message);
        seen.put(value, out);
        if (value.getCause() != null) {
            Throwable cause = copyCause(value.getCause(), seen, depth + 1);
            if (cause != out) out.initCause(cause);
        }
        for (Throwable suppressed : value.getSuppressed()) {
            Throwable safe = copyCause(suppressed, seen, depth + 1);
            if (safe != out) out.addSuppressed(safe);
        }
        StackTraceElement[] trace = value.getStackTrace();
        for (int i = 0; i < trace.length; i++) {
            StackTraceElement frame = trace[i];
            trace[i] = new StackTraceElement(text(frame.getClassName()), text(frame.getMethodName()), text(frame.getFileName()), frame.getLineNumber());
        }
        out.setStackTrace(trace);
        return out;
    }

    public WorkflowExecution execution(WorkflowExecution value) {
        if (value == null) return null;
        WorkflowExecution out = new WorkflowExecution();
        out.setId(text(value.getId()));
        out.setExecutionId(text(value.getExecutionId()));
        out.setWorkflowId(text(value.getWorkflowId()));
        out.setStatus(value.getStatus());
        String status = value.getStatusRaw();
        boolean known = "PENDING".equals(status) || "RUNNING".equals(status)
            || WorkflowExecution.SUCCESS_STATUSES.contains(status) || WorkflowExecution.FAILURE_STATUSES.contains(status)
            || WorkflowExecution.CANCELLED_STATUSES.contains(status) || WorkflowExecution.PAUSED_STATUSES.contains(status);
        out.setStatusRaw(known ? status : text(status));
        out.setProgress(value.getProgress());
        out.setInputs(data(value.getInputs()));
        out.setOutputs(data(value.getOutputs()));
        out.setError(text(value.getError()));
        out.setStartedAt(value.getStartedAt());
        out.setCompletedAt(value.getCompletedAt());
        out.setNodeExecutions(data(value.getNodeExecutions()));
        out.setRaw(data(value.getRaw()));
        return out;
    }
}
