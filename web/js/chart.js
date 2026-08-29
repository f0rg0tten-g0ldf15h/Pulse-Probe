/**
 * PulseProbe — Canvas Latency Sparkline Renderer
 */
function renderSparkline(canvas, history = [], maxSlots = 30) {
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    const dpr = window.devicePixelRatio || 1;

    const width = canvas.parentElement.clientWidth || 300;
    const height = 40;

    canvas.width = width * dpr;
    canvas.height = height * dpr;
    ctx.scale(dpr, dpr);
    ctx.clearRect(0, 0, width, height);

    const gap = 3;
    const totalGaps = (maxSlots - 1) * gap;
    const barWidth = Math.max(2, (width - totalGaps) / maxSlots);

    // Determine max latency to scale bar heights smoothly (clamp minimum max to 200ms)
    const validLatencies = history.filter(h => h.success).map(h => h.latencyMs);
    const maxLatency = Math.max(200, ...validLatencies, 1);

    // Pad array to fixed maxSlots (empty slots on left, latest data on right)
    const slots = new Array(maxSlots).fill(null);
    const offset = maxSlots - history.length;
    for (let i = 0; i < history.length; i++) {
        slots[offset + i] = history[i];
    }

    // Draw bars
    slots.forEach((item, index) => {
        const x = index * (barWidth + gap);

        if (!item) {
            // Empty placeholder bar
            ctx.fillStyle = '#21262d';
            ctx.fillRect(x, height - 6, barWidth, 6);
            return;
        }

        if (item.success) {
            ctx.fillStyle = '#3fb950'; // Green
            const normalizedHeight = Math.max(6, (item.latencyMs / maxLatency) * (height - 8));
            const y = height - normalizedHeight;
            ctx.fillRect(x, y, barWidth, normalizedHeight);
        } else {
            ctx.fillStyle = '#f85149'; // Red (down/error)
            ctx.fillRect(x, 4, barWidth, height - 4);
        }
    });
}

window.renderSparkline = renderSparkline;
