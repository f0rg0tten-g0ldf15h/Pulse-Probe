package com.pulseprobe.model;

import java.time.Instant;

public class CheckResult {

    private final Long timestamp;
    private final Integer statusCode;
    private final Long latencyMs;
    private final Boolean success;
    private final String error;

    // constructor
    public CheckResult(Integer statusCode, Long latencyMs, Boolean success, String error) {
        this.timestamp = Instant.now().toEpochMilli();
        this.statusCode = statusCode;
        this.latencyMs = latencyMs;
        this.success = success;
        this.error = error;
    }

    // getters
    public Long getTimeStamp() {
        return timestamp;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public Long getlatencyMs() {
        return latencyMs;
    }

    public Boolean isSuccess() {
        return success;
    }

    public String getError() {
        return error;
    }

    // methods
    public String toJSON() {
        return String.format(
                "{\"timestamp\":%d,\"statusCode\":%d,\"latencyMs\":%d,\"success\":%b,\"error\":%s}",
                timestamp,
                statusCode,
                latencyMs,
                success,
                error == null ? "null" : "\"" + escapeJson(error) + "\"");
    }

    private String escapeJson(String raw) {
        return raw.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }

}
