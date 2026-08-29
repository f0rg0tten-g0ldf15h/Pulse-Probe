# ⚡ PulseProbe — Real-Time API & Microservice Uptime Monitor

A lightweight, autonomous health-monitoring engine and real-time status dashboard built **from scratch with zero external dependencies and zero third-party frameworks**.

---

## 🎯 Architectural Highlights & Engineering Concepts

- **Autonomous Multi-Threaded Prober Engine:** Utilizes Java's `ScheduledExecutorService` and native `java.net.http.HttpClient` to probe distributed endpoints asynchronously without blocking the main event loop.
- **Thread-Safe In-Memory Ring Buffer:** Stores bounded latency histories per monitor using `ConcurrentLinkedDeque` and `ConcurrentHashMap` to maintain constant-time $O(1)$ memory bounds and thread safety under concurrent reads/writes.
- **Frameworkless HTTP Server & REST API:** Implements custom routing, CORS preflight handling, error dispatching, and static asset streaming directly on top of JDK's built-in `com.sun.net.httpserver.HttpServer`.
- **Pure HTML5 Canvas Sparkline Engine:** Custom data visualization engine rendering high-DPI sub-pixel latency sparklines without heavy charting libraries.
- **Production-Ready Multi-Stage Dockerfile:** Compiles with JDK in builder stage and runs in an optimized, minimal JRE Alpine container under a non-root security user.

---

## 🛠️ Tech Stack

- **Backend:** Pure Vanilla Java 17+ (Standard Library only: `HttpServer`, `HttpClient`, `Executors`)
- **Frontend:** Vanilla HTML5, CSS3 (Modern Dark Theme), ES6+ JavaScript, HTML5 Canvas API

---

## 🚀 Getting Started

### Local Execution (Without Docker)
```bash
# 1. Compile all Java sources
javac -d bin $(find src -name "*.java")

# 2. Run the application
java -cp bin com.pulseprobe.Main

# 3. Open browser at http://localhost:8080
