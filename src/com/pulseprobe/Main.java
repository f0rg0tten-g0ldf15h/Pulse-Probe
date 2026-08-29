package com.pulseprobe;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.io.IOException;
import com.sun.net.httpserver.HttpServer;

import com.pulseprobe.service.MonitorService;
import com.pulseprobe.service.ProberEngine;
import com.pulseprobe.handler.ApiHandler;
import com.pulseprobe.handler.StaticFileHandler;

public class Main {

    public static void main(String[] args) throws IOException {
        int port = System.getenv("PORT") != null ? Integer.parseInt(System.getenv("PORT")) : 8080;

        MonitorService monitorService = new MonitorService();
        ProberEngine proberEngine = new ProberEngine(monitorService);

        proberEngine.start();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/api/monitors", new ApiHandler(monitorService));
        server.createContext("/", new StaticFileHandler("web"));

        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();

        System.out.println("Pulse Probe started on port " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down PulseProbe...");
            proberEngine.stop();
            server.stop(0);
            System.out.println("PulseProbe stopped.");
        }));
    }
}
