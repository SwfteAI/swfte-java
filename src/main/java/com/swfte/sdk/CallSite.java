package com.swfte.sdk;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A code-map call-site id ({@code cs_} followed by 24 lowercase hex characters), as written by
 * {@code swfte scan --tag} next to each call and recorded in the scan's manifest.
 *
 * <p>Passing one to an artifact-invoking method ({@code workflows().invoke/invokeAndWait/execute},
 * {@code agents().chat}, {@code chatflows().startSession}) sends it as the
 * {@value #HEADER} header, so the run can be attributed to the line of code that started it.</p>
 *
 * <pre>{@code
 * client.workflows().invoke("wf_123", inputs, CallSite.of("cs_0123456789abcdef01234567"));
 * }</pre>
 *
 * <p>An id that does not match {@code ^cs_[0-9a-f]{24}$} is kept but never sent: the call goes
 * out without the header rather than failing. See {@link CallsiteResolver} for the opt-in stack
 * capture used when no id is given.</p>
 */
public final class CallSite {

    /** The request header that carries the id. */
    public static final String HEADER = "X-Swfte-Callsite";

    private static final Pattern VALID = Pattern.compile("^cs_[0-9a-f]{24}$");

    private final String id;

    private CallSite(String id) {
        this.id = id;
    }

    /**
     * Wrap an id. Never throws; an invalid id (including {@code null}) is simply never sent.
     */
    public static CallSite of(String id) {
        return new CallSite(id);
    }

    /** True when {@code id} is exactly {@code cs_} plus 24 lowercase hex characters. */
    public static boolean isValidId(String id) {
        return id != null && VALID.matcher(id).matches();
    }

    /** The id as given (may be invalid). */
    public String getId() {
        return id;
    }

    /** True when this id would be sent. */
    public boolean isValid() {
        return isValidId(id);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CallSite && Objects.equals(id, ((CallSite) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "CallSite(" + id + ")";
    }
}
