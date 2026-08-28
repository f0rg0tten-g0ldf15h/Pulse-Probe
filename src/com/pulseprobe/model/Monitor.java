package com.pulseprobe.model;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Monitor {
    public enum Status {
        UP, DOWN, PENDING, PAUSED
    }

    private final String id;
    private String name;
    private String url;
    private int intervalSeconds;
    private Status status;
    private boolean active;

    // Thread-safe ring buffer: Keeps the latest 30 ping results for sparkline
    // charts
    private static final int MAX_HISTORY = 30;
    private final Deque<CheckResult> history = new ConcurrentLinkedDeque<>();

    public Monitor(String name, String url, int intervalSeconds) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.name = name;
        this.url = url;
        this.intervalSeconds = Math.max(intervalSeconds, 5); // minimum 5s interval
        this.status = Status.PENDING;
        this.active = true;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    public Status getStatus() {
        return status;
    }

    public boolean isActive() {
        return active;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setActive(boolean active) {
        this.active = active;
        if (!active)
            this.status = Status.PAUSED;
    }

    public synchronized void addResult(CheckResult result) {
        if (history.size() >= MAX_HISTORY) {
            history.pollFirst(); // Remove oldest
        }
        history.addLast(result);
        this.status = result.isSuccess() ? Status.UP : Status.DOWN;
    }

    public List<CheckResult> getHistory() {
        return new ArrayList<>(history);
    }

    public double getUptimePercentage() {
        if (history.isEmpty())
            return 100.0;
        long successful = history.stream().filter(CheckResult::isSuccess).count();
        return ((double) successful / history.size()) * 100.0;
    }

    public long getAverageLatency() {
        if (history.isEmpty())
            return 0;
        return (long) history.stream()
                .filter(CheckResult::isSuccess)
                .mapToLong(CheckResult::getLatencyMs)
                .average()
                .orElse(0);
    }

    public String toJson() {
        StringBuilder historyJson = new StringBuilder("[");
        List<CheckResult> list = getHistory();
        for (int i = 0; i < list.size(); i++) {
            historyJson.append(list.get(i).toJson());
            if (i < list.size() - 1)
                historyJson.append(",");
        }
        historyJson.append("]");

        return String.format(
                "{\"id\":\"%s\",\"name\":\"%s\",\"url\":\"%s\",\"intervalSeconds\":%d,\"status\":\"%s\",\"active\":%b,\"uptime\":%.2f,\"avgLatency\":%d,\"history\":%s}",
                id, escapeJson(name), escapeJson(url), intervalSeconds, status.name(), active, getUptimePercentage(),
                getAverageLatency(), historyJson);
    }

    private String escapeJson(String raw) {
        return raw.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }
}
