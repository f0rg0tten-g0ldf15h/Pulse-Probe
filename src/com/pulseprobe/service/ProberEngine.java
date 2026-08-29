package com.pulseprobe.service;

import java.net.URI;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.pulseprobe.model.CheckResult;
import com.pulseprobe.service.MonitorService;
import com.pulseprobe.model.Monitor;

public class ProberEngine {

    private final MonitorService monitorService;
    private final ScheduledExecutorService scheduler;
    private final HttpClient client;

    // constructor
    public ProberEngine(MonitorService monitorService) {
        this.monitorService = monitorService;
        this.scheduler = Executors.newScheduledThreadPool(4);
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public void start() {
        scheduler.scheduleAtFixedRate(
                this::tick,
                0,
                1,
                TimeUnit.SECONDS);

    }

    private void tick() {
        long currentTimeSeconds = System.currentTimeMillis() / 1000;
        for (Monitor monitor : monitorService.getAll()) {

            if (!monitor.isActive()) {
                continue;
            }

            int interval = monitor.getIntervalSeconds();

            if (interval > 0 && currentTimeSeconds % interval == 0) {
                scheduler.submit(() -> probe(monitor));
            }

        }
    }

    public void probe(Monitor monitor) {
        long startTime = System.currentTimeMillis();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(monitor.getUrl()))
                    .timeout(Duration.ofSeconds(5))
                    .build();
            HttpResponse<Void> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.discarding());

            long latencyMs = System.currentTimeMillis() - startTime;
            int statusCode = response.statusCode();

            CheckResult result;

            if (statusCode >= 200 && statusCode <= 399) {
                result = new CheckResult(
                        statusCode,
                        latencyMs,
                        true,
                        null);
            } else {
                result = new CheckResult(statusCode, latencyMs, false, "HTTP " + statusCode);
            }

            monitor.addResult(result);
        } catch (IOException
                | InterruptedException
                | IllegalArgumentException e) {
            long latencyMs = System.currentTimeMillis() - startTime;
            CheckResult result = new CheckResult(
                    0,
                    latencyMs,
                    false,
                    e.getMessage());
            monitor.addResult(result);

            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void stop() {
        scheduler.shutdown();
    }

}
