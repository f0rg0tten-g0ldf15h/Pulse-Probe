package com.pulseprobe.model;

import java.time.Instant;

public class CheckResult {
    private final long timestamp;
    private final int statusCode;
    private final long latencyMs;
    private final boolean success;
    private final String error;

    public CheckResult(int statusCode, long latencyMs, boolean success, String error) {
        this.timestamp = Instant.now().toEpochMilli();
        this.statusCode = statusCode;
        this.latencyMs = latencyMs;
        this.success = success;
        this.error = error;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public long getLatencyMs() {
        return latencyMs;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getError() {
        return error;
    }

    public String toJson() {
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
