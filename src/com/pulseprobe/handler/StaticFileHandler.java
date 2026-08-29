package com.pulseprobe.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class StaticFileHandler implements HttpHandler {

    private final String baseDir;

    public StaticFileHandler(String baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String path = exchange.getRequestURI().getPath();

        if (path.equals("/")) {
            path = "/index.html";
        }
        if (path.contains("..")) {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
            return;
        }

        Path filePath = Paths.get(baseDir, path);

        if (!Files.exists(filePath)) {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
            return;
        }
        String contentType = getContentType(filePath);
        exchange.getResponseHeaders().set("Content-Type", contentType);

        byte[] data = Files.readAllBytes(filePath);

        exchange.sendResponseHeaders(200, data.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(data);
        }

    }

    private String getContentType(Path filePath) {
        String fileName = filePath.getFileName().toString().toLowerCase();

        if (fileName.endsWith(".html")) {
            return "text/html;charset = UTF - 8";
        } else if (fileName.endsWith(".css")) {
            return "text/css;charset = UTF - 8";
        } else if (fileName.endsWith(".js")) {
            return "application/js;charset = UTF - 8";
        } else if (fileName.endsWith(".svg")) {
            return "image/svg+xml";
        } else if (fileName.endsWith(".ico")) {
            return "image/x-icon";

        }
        return "text/plain";
    }

}
