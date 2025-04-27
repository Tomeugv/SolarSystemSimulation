//  Solar System Simulation (Fixed + Fabulous Final Version)

// State
let bodies = [];
let planetTraces = new Map();
const MAX_TRACE_POINTS = 500;

// UI Elements
const canvas = document.getElementById('solarCanvas');
const ctx = canvas.getContext('2d');
const timeSlider = document.getElementById('timeSlider');
const scaleDisplay = document.getElementById('scaleDisplay');
const showTracesCheckbox = document.getElementById('showTraces');

//  Dynamic project base path
const BASE_PATH = '/SolarSystemSimulation';

// Camera State
let isDragging = false;
let lastX = 0, lastY = 0;

// Viewport center tracking (local version)
let centerX = canvas.width / 2;
let centerY = canvas.height / 2;
let currentScale = 100;

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
        
        centerX += dx;
        centerY += dy;

        await fetch(`${BASE_PATH}/api/simulation?moveX=${dx}&moveY=${dy}`);
        await updateSimulation();
    }
});

window.addEventListener('mouseup', () => isDragging = false);

document.getElementById('zoomIn').addEventListener('click', async () => {
    await fetch(`${BASE_PATH}/api/simulation?zoom=in`);
    await updateSimulation();
});

document.getElementById('zoomOut').addEventListener('click', async () => {
    await fetch(`${BASE_PATH}/api/simulation?zoom=out`);
    await updateSimulation();
});

timeSlider.addEventListener('input', async () => {
    await updateSimulation();
});

document.getElementById('resetSim').addEventListener('click', async () => {
    try {
        await fetch(`${BASE_PATH}/api/simulation/reset`, { method: 'POST' });
        planetTraces.clear(); //  Clear traces properly on reset
        await updateSimulation();
    } catch (error) {
        console.error("Reset failed:", error);
    }
});

// Main Functions
async function updateSimulation() {
    try {
        const response = await fetch(`${BASE_PATH}/api/simulation?scale=${timeSlider.value}`);
        const data = await response.json();
        
        bodies = data.bodies;
        currentScale = parseFloat(data.scale) || 100;
        scaleDisplay.textContent = `1 AU = ${currentScale.toFixed(0)}px`;

        render();
    } catch (error) {
        console.error("Update failed:", error);
    }
}

function render() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    
    if (showTracesCheckbox.checked) {
        updateTraces();
        drawTraces();
    }
    
    bodies.forEach(body => {
        const screenPos = viewportToScreen(body.worldX, body.worldY);
        const zoomMultiplier = calculateZoomMultiplier();
        const dynamicRadius = Math.max(1, body.radius * zoomMultiplier);

        ctx.beginPath();
        ctx.arc(screenPos.x, screenPos.y, dynamicRadius, 0, Math.PI * 2);
        ctx.fillStyle = body.color;
        ctx.fill();

        ctx.fillStyle = 'white';
        ctx.font = '12px Arial';
        ctx.fillText(body.name, screenPos.x + dynamicRadius + 2, screenPos.y);
    });
}



function calculateZoomMultiplier() {
    const baseScale = 100;
    return currentScale / baseScale;
}

function updateTraces() {
    bodies.forEach(body => {
        if (!planetTraces.has(body.name)) {
            planetTraces.set(body.name, []);
        }
        planetTraces.get(body.name).push({ x: body.worldX, y: body.worldY });

        if (planetTraces.get(body.name).length > MAX_TRACE_POINTS) {
            planetTraces.get(body.name).shift();
        }
    });
}

function drawTraces() {
    planetTraces.forEach((points, name) => {
        if (points.length < 2) return;
        
        ctx.beginPath();
        const start = viewportToScreen(points[0].x, points[0].y);
        ctx.moveTo(start.x, start.y);

        for (let i = 1; i < points.length; i++) {
            const pos = viewportToScreen(points[i].x, points[i].y);
            ctx.lineTo(pos.x, pos.y);
        }

        ctx.strokeStyle = (bodies.find(b => b.name === name)?.color || '#ffffff') + "60";
        ctx.lineWidth = 1;
        ctx.stroke();
    });
}

function viewportToScreen(worldX, worldY) {
    const AU = 1.496e11;
    return {
        x: centerX + (worldX / AU * currentScale),
        y: centerY + (worldY / AU * currentScale)
    };
}


// Initialize
function resizeCanvas() {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;

    centerX = canvas.width / 2;
    centerY = canvas.height / 2;

    fetch(`${BASE_PATH}/api/simulation?resetViewport=true`);
}

window.addEventListener('resize', resizeCanvas);
resizeCanvas();
setInterval(updateSimulation, 1000 / 30); // ~30fps (smooth but slower)

