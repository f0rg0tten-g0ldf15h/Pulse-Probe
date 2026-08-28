package com.pulseprobe.model;

import java.util.*;

public class Monitor {

    // fields
    private enum Status {
        UP, DOWN, PENDING, PAUSED
    }

    private final String id;
    private String name;
    private String url;
    private Integer intervalSeconds;
    private Status status;
    private Boolean active;
    private final Integer MAX_HISTORY = 30;
    private Queue<CheckResult> History = new LinkedList<>();

    // constructor
    public Monitor(String name, String url, int intervalSeconds) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.name = name;
        this.url = url;
        this.intervalSeconds = Math.max(intervalSeconds, 5); // minimum 5s interval
        this.status = Status.PENDING;
        this.active = true;
    }

    // getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public Integer getIntervalSeconds() {
        return intervalSeconds;
    }

    public Status getStatus() {
        return status;
    }

    public Boolean isActive() {
        return active;
    }

    public Queue<CheckResult> getHistory() {
        return new LinkedList<>(History);
    }

    public Double getUpTimePercentage() {
        if (History.size() == 0)
            return 100.0;
        long successful = History.stream().filter(CheckResult::isSuccess).count();
        return ((Double) successful / History.size()) / 100.0;
    }

    public Long getAverageLatency(){
        if(History.isEmpty()) return 0;
        return (Long) History.stream()
                        .filter(CheckResult::isSuccess)
                        .mapToLong(CheckResult::getlatencyMs())
                        .average()
                        .orElse(0);
    }

    // setters
    public void setStatus(Status status) {
        this.status = status;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    // methods
    public synchronized void addResult(CheckResult result) {
        if (History.size() >= MAX_HISTORY) {
            CheckResult idc = History.remove();
        }
        History.add(result);
        this.status = result.isSuccess() ? status.UP : status.DOWN;
    }

    public String toJson() {
        StringBuilder historyJson = new StringBuilder("[");
        Queue<CheckResult> queue = getHistory();
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
