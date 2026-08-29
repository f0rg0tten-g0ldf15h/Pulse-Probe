/**
 * PulseProbe — Frontend Application Controller
 */
const API_URL = '/api/monitors';
let monitorsData = [];

// DOM Elements
const monitorsContainer = document.getElementById('monitors-container');
const statTotal = document.getElementById('stat-total');
const statUp = document.getElementById('stat-up');
const statDown = document.getElementById('stat-down');
const statLatency = document.getElementById('stat-latency');
const lastUpdated = document.getElementById('last-updated');

// Modal Elements
const modalOverlay = document.getElementById('modal-overlay');
const openModalBtn = document.getElementById('open-modal-btn');
const closeModalBtn = document.getElementById('close-modal-btn');
const cancelBtn = document.getElementById('cancel-btn');
const addMonitorForm = document.getElementById('add-monitor-form');

// --- Fetch & Render Loop ---
async function fetchMonitors() {
    try {
        const res = await fetch(API_URL);
        if (!res.ok) throw new Error(`HTTP error ${res.status}`);
        monitorsData = await res.json();
        renderStats();
        renderMonitors();
        lastUpdated.innerText = `Updated at ${new Date().toLocaleTimeString()}`;
    } catch (err) {
        lastUpdated.innerText = 'Connection lost. Retrying...';
        console.error('Failed to fetch monitors:', err);
    }
}

// --- Render Overview Metrics ---
function renderStats() {
    const total = monitorsData.length;
    const up = monitorsData.filter(m => m.status === 'UP').length;
    const down = monitorsData.filter(m => m.status === 'DOWN').length;

    const activeLatencies = monitorsData
        .filter(m => m.active && m.avgLatency > 0)
        .map(m => m.avgLatency);

    const avg = activeLatencies.length > 0
        ? Math.round(activeLatencies.reduce((a, b) => a + b, 0) / activeLatencies.length)
        : 0;

    statTotal.innerText = total;
    statUp.innerText = up;
    statDown.innerText = down;
    statLatency.innerText = `${avg} ms`;
}

// --- Render Monitor Cards ---
function renderMonitors() {
    if (monitorsData.length === 0) {
        monitorsContainer.innerHTML = `
            <div style="text-align: center; padding: 3rem; color: var(--text-muted); background: var(--bg-secondary); border-radius: var(--radius-md); border: 1px dashed var(--border-color);">
                <p>No endpoints configured yet.</p>
                <p style="font-size: 0.875rem; margin-top: 0.5rem;">Click <strong>+ Add Monitor</strong> above to track your first service.</p>
            </div>
        `;
        return;
    }

    monitorsContainer.innerHTML = monitorsData.map(monitor => {
        const statusClass = `status-${monitor.status.toLowerCase()}`;
        return `
            <div class="monitor-card" data-id="${monitor.id}">
                <div class="monitor-header">
                    <div class="monitor-title-group">
                        <span class="status-pill ${statusClass}">${monitor.status}</span>
                        <span class="monitor-name">${escapeHtml(monitor.name)}</span>
                    </div>
                    <div class="monitor-actions">
                        <button class="btn btn-secondary btn-sm" onclick="toggleMonitor('${monitor.id}')">
                            ${monitor.active ? 'Pause' : 'Resume'}
                        </button>
                        <button class="btn btn-danger btn-sm" onclick="deleteMonitor('${monitor.id}')">
                            Delete
                        </button>
                    </div>
                </div>

                <div class="monitor-meta">
                    <a href="${escapeHtml(monitor.url)}" target="_blank" rel="noopener noreferrer" class="monitor-url">${escapeHtml(monitor.url)}</a>
                    <div class="meta-item">Interval: <strong>${monitor.intervalSeconds}s</strong></div>
                    <div class="meta-item">Uptime: <strong>${monitor.uptime.toFixed(1)}%</strong></div>
                    <div class="meta-item">Avg Latency: <strong>${monitor.avgLatency}ms</strong></div>
                </div>

                <div class="chart-container">
                    <div class="chart-header">
                        <span>Latency history (last 30 checks)</span>
                        <span>Latest: <strong>${getLatestLatency(monitor)}</strong></span>
                    </div>
                    <canvas id="chart-${monitor.id}" class="sparkline"></canvas>
                </div>
            </div>
        `;
    }).join('');

    // Draw sparklines for all cards
    monitorsData.forEach(monitor => {
        const canvas = document.getElementById(`chart-${monitor.id}`);
        if (canvas) {
            window.renderSparkline(canvas, monitor.history || []);
        }
    });
}

function getLatestLatency(monitor) {
    if (!monitor.history || monitor.history.length === 0) return 'No data';
    const latest = monitor.history[monitor.history.length - 1];
    return latest.success ? `${latest.latencyMs} ms` : `Error (${latest.error || 'Down'})`;
}

function escapeHtml(str) {
    return (str || '').replace(/[&<>"']/g, m => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    }[m]));
}

// --- API Actions ---
async function toggleMonitor(id) {
    try {
        await fetch(`${API_URL}/${id}/toggle`, { method: 'POST' });
        fetchMonitors();
    } catch (err) {
        alert('Failed to toggle monitor.');
    }
}

async function deleteMonitor(id) {
    if (!confirm('Are you sure you want to delete this monitor?')) return;
    try {
        await fetch(`${API_URL}/${id}`, { method: 'DELETE' });
        fetchMonitors();
    } catch (err) {
        alert('Failed to delete monitor.');
    }
}

// --- Modal & Form Handlers ---
openModalBtn.addEventListener('click', () => modalOverlay.classList.remove('hidden'));
closeModalBtn.addEventListener('click', () => modalOverlay.classList.add('hidden'));
cancelBtn.addEventListener('click', () => modalOverlay.classList.add('hidden'));

modalOverlay.addEventListener('click', (e) => {
    if (e.target === modalOverlay) modalOverlay.classList.add('hidden');
});

addMonitorForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const name = document.getElementById('monitor-name').value.trim();
    const url = document.getElementById('monitor-url').value.trim();
    const intervalSeconds = parseInt(document.getElementById('monitor-interval').value, 10);

    try {
        const res = await fetch(API_URL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, url, intervalSeconds })
        });

        if (!res.ok) {
            const data = await res.json();
            throw new Error(data.error || 'Failed to create monitor');
        }

        addMonitorForm.reset();
        modalOverlay.classList.add('hidden');
        fetchMonitors();
    } catch (err) {
        alert(err.message);
    }
});

// --- Boot & Realtime Polling Loop ---
fetchMonitors();
setInterval(fetchMonitors, 3000); // Poll every 3 seconds for live updates
