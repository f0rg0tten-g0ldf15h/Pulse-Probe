package com.pulseprobe.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import com.pulseprobe.service.MonitorService;
import com.pulseprobe.util.JsonUtil;
import com.pulseprobe.model.Monitor;

public class ApiHandler implements HttpHandler {

    private final MonitorService monitorService;

    public ApiHandler(MonitorService monitorService) {
        this.monitorService = monitorService;
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String responseText) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

        byte[] bytes = responseText.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);

        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod().toUpperCase();
        String path = exchange.getRequestURI().getPath();

        String[] parts = path.split("/");

        try {
            // 1.CORS prefilght
            if ("OPTIONS".equals(method)) {
                sendResponse(exchange, 204, "");
                return;
            }

            // 2.GET /api/monitor
            if ("GET".equals(method) && parts.length == 3) {
                sendResponse(exchange, 200, monitorService.getAllAsJson());
                return;
            }

            // 3.POST /api/monitor (create new monitor)
            if ("POST".equals(method) && parts.length == 3) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

                String name = JsonUtil.extractString(body, "name");
                String url = JsonUtil.extractString(body, "url");
                int intervalSeconds = JsonUtil.extractInt(body, "intervalSeconds", 0);

                if (url == null || !(url.startsWith("http://") || url.startsWith("https://"))) {
                    sendResponse(exchange, 400, "{\"error\": \"Bad Request\"}");
                    return;
                }

                Monitor monitor = monitorService.create(name, url, intervalSeconds);
                sendResponse(exchange, 201, monitor.toJson());
                return;
            }

            // 4.POST /api/monitor/{id}/toggle
            if ("POST".equals(method) && parts.length == 5 && "toggle".equals(parts[4])) {
                String id = parts[3];
                Optional<Monitor> monitor = monitorService.toggleActive(id);

                if (!monitor.isEmpty()) {
                    sendResponse(exchange, 200, monitor.get().toJson());
                    return;
                } else {
                    sendResponse(exchange, 404, "{\"error\":\"Monitor not found\"}");
                    return;
                }

            }

            // 5.DELETE /api/monitor/{id}
            if ("DELETE".equals(method) && parts.length == 4) {
                String id = parts[3];
                boolean bool = monitorService.delete(id);

                if (bool) {
                    sendResponse(exchange, 200, "{\"success\":true}");
                    return;
                } else {
                    sendResponse(exchange, 404, "{\"error\":\"Not Found\"}");
                    return;
                }
            }

            sendResponse(exchange, 404, "{\"error\":\"Not Found\"}");

        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\":\"Internal Server Error: " + e.getMessage() + "\"}");

        }
    }

}
