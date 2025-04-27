// State
let bodies = [];
let planetTraces = new Map();
const MAX_TRACE_POINTS = 500;

// UI Elements
const canvas = document.getElementById('solarCanvas');
const ctx = canvas.getContext('2d');
const timeSlider = document.getElementById('timeSlider');
const scaleDisplay = document.getElementById('scaleDisplay');

// Camera State
let isDragging = false;
let lastX = 0, lastY = 0;

// Event Listeners
canvas.addEventListener('mousedown', (e) => {
    isDragging = true;
    lastX = e.clientX;
    lastY = e.clientY;
});

canvas.addEventListener('mousemove', async (e) => {
    if (isDragging) {
        const dx = e.clientX - lastX;
        const dy = e.clientY - lastY;
        lastX = e.clientX;
        lastY = e.clientY;
        
        await fetch(`/api/simulation?moveX=${dx}&moveY=${dy}`);
        await updateSimulation();
    }
});

window.addEventListener('mouseup', () => isDragging = false);

document.getElementById('zoomIn').addEventListener('click', async () => {
    await fetch('/api/simulation?zoom=in');
    await updateSimulation();
});

document.getElementById('zoomOut').addEventListener('click', async () => {
    await fetch('/api/simulation?zoom=out');
    await updateSimulation();
});

// Main Functions
async function updateSimulation() {
    try {
        const response = await fetch(`/api/simulation?scale=${timeSlider.value}`);
        const data = await response.json();
        
        bodies = data.bodies;
        scaleDisplay.textContent = `1 AU = ${data.scale.toFixed(0)}px`;
        render();
    } catch (error) {
        console.error("Update failed:", error);
    }
}

function render() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    
    // Draw traces (minimal client-side logic)
    if (document.getElementById('showTraces').checked) {
        updateTraces();
        drawTraces();
    }
    
    // Draw bodies using server-calculated positions
    bodies.forEach(body => {
        ctx.beginPath();
        ctx.arc(body.screenX, body.screenY, body.radius, 0, Math.PI*2);
        ctx.fillStyle = body.color;
        ctx.fill();
    });
}

function updateTraces() {
    bodies.forEach(body => {
        if (!planetTraces.has(body.name)) {
            planetTraces.set(body.name, []);
        }
        planetTraces.get(body.name).push({x: body.screenX, y: body.screenY});
        if (planetTraces.get(body.name).length > MAX_TRACE_POINTS) {
            planetTraces.get(body.name).shift();
        }
    });
}

function drawTraces() {
    planetTraces.forEach((points, name) => {
        if (points.length < 2) return;
        
        ctx.beginPath();
        ctx.moveTo(points[0].x, points[0].y);
        for (let i = 1; i < points.length; i++) {
            ctx.lineTo(points[i].x, points[i].y);
        }
        ctx.strokeStyle = bodies.find(b => b.name === name)?.color + "60";
        ctx.lineWidth = 1;
        ctx.stroke();
    });
}

// Initialize
function resizeCanvas() {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
    fetch('/api/simulation?resetViewport=true');
}

window.addEventListener('resize', resizeCanvas);
resizeCanvas();
setInterval(updateSimulation, 1000 / 60); // ~60fps