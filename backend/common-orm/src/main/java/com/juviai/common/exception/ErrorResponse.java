package com.juviai.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.slf4j.MDC;

import java.time.Instant;
import java.util.List;

/**
 * Standard error response envelope used by {@link GlobalExceptionHandler}.
 *
 * <p>Every error carries a {@code correlationId} so callers can quote it in
 * support tickets and operators can grep for it in centralized logs.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String correlationId;

    /** Non-null only for validation errors — list of per-field messages. */
    private List<String> errors;

    public ErrorResponse(int status, String error, String message) {
        this.timestamp     = Instant.now();
        this.status        = status;
        this.error         = error;
        this.message       = message;
        this.correlationId = MDC.get("correlationId");
    }

    public ErrorResponse(int status, String error, String message, List<String> errors) {
        this(status, error, message);
        this.errors = errors;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public Instant getTimestamp()     { return timestamp;     }
    public int     getStatus()        { return status;        }
    public String  getError()         { return error;         }
    public String  getMessage()       { return message;       }
    public String  getCorrelationId() { return correlationId; }
    public List<String> getErrors()   { return errors;        }
}
