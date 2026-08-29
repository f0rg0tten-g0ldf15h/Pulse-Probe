package com.pulseprobe.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.pulseprobe.model.Monitor;

public class MonitorService {

    private final ConcurrentHashMap<String, Monitor> Monitors = new ConcurrentHashMap<>();

    // Methods
    public Monitor create(String name, String url, int intervalSeconds) {
        Monitor monitor = new Monitor(name, url, intervalSeconds);
        Monitors.put(monitor.getId(), monitor);

        return monitor;
    }

    public List<Monitor> getAll() {
        return new LinkedList<>(Monitors.values());
    }

    public Optional<Monitor> getByID(String id) {
        return Optional.ofNullable(Monitors.get(id));
    }

    public boolean delete(String id) {
        return Monitors.remove(id) != null;
    }

    public Optional<Monitor> toggleActive(String id) {
        Monitor monitor = Monitors.get(id);

        if (monitor != null) {
            monitor.setActive(!monitor.isActive());
            return Optional.of(monitor);
        }
        return Optional.empty();

    }

    public String getAllAsJson() {
        return getAll()
                .stream()
                .map(Monitor::toJson)
                .collect(java.util.stream.Collectors.joining(",", "[", "]"));
    }

}
