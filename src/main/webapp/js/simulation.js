// Estat de la simulació
let bodies = [];
let planetTraces = new Map();
const MAX_TRACE_POINTS = 500;

// Elements UI
const canvas = document.getElementById('solarCanvas');
const ctx = canvas.getContext('2d');
const timeSlider = document.getElementById('timeSlider');
const scaleDisplay = document.getElementById('scaleDisplay');
const showTracesCheckbox = document.getElementById('showTraces');

// Ruta base del projecte
const BASE_PATH = '/SolarSystemSimulation';

// Estat de la càmera
let isDragging = false;
let lastX = 0, lastY = 0;
let centerX = canvas.width / 2;
let centerY = canvas.height / 2;
let currentScale = 173; // 1 AU = 173 píxels

// Funcions per mostrar/ocultar el carregador
function showLoading() {
    document.getElementById('loadingOverlay').style.display = 'flex';
}
function hideLoading() {
    document.getElementById('loadingOverlay').style.display = 'none';
}

// Listeners per interacció amb el canvas
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

// Zoom
document.getElementById('zoomIn').addEventListener('click', async () => {
    await fetch(`${BASE_PATH}/api/simulation?zoom=in`);
    await updateSimulation();
});
document.getElementById('zoomOut').addEventListener('click', async () => {
    await fetch(`${BASE_PATH}/api/simulation?zoom=out`);
    await updateSimulation();
});

// Slider de velocitat temporal
timeSlider.addEventListener('input', async () => {
    await updateSimulation();
});

// Botó d'iniciar simulació
document.getElementById('startSim').addEventListener('click', async () => {
    try {
        const selectedPlanets = Array.from(document.querySelectorAll('.planet-select input:checked'))
            .map(cb => cb.value);
        if (!selectedPlanets.includes('Sun')) {
            selectedPlanets.unshift('Sun');
        }
        showLoading();
        const response = await fetch(`${BASE_PATH}/api/simulation/start`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(selectedPlanets)
        });
        if (response.ok) {
            planetTraces.clear();
            centerX = canvas.width / 2;
            centerY = canvas.height / 2;
            setTimeout(async () => {
                await updateSimulation();
            }, 100);
        } else {
            console.error('Error començant la simulació');
        }
    } catch (error) {
        console.error("Error iniciant simulació:", error);
    } finally {
        hideLoading();
    }
});

// Funció per actualitzar la simulació
async function updateSimulation() {
    try {
        const response = await fetch(`${BASE_PATH}/api/simulation?scale=${timeSlider.value}`);
        const data = await response.json();
        bodies = data.bodies;
        currentScale = parseFloat(data.scale) || 173;
        scaleDisplay.textContent = `1 AU = ${currentScale.toFixed(0)}px`;
        render();
    } catch (error) {
        console.error("Error actualitzant simulació:", error);
    }
}

// Funció de renderitzat
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

// Actualitzar traçats
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

// Dibuixar traçats
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

// Conversió de coordenades
function viewportToScreen(worldX, worldY) {
    const AU = 1.496e11;
    return {
        x: centerX + (worldX / AU * currentScale),
        y: centerY + (worldY / AU * currentScale)
    };
}

// Zoom dinàmic
function calculateZoomMultiplier() {
    const baseScale = 173;
    return baseScale / currentScale;
}

// Inicialització
function resizeCanvas() {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
    centerX = canvas.width / 2;
    centerY = canvas.height / 2;
    fetch(`${BASE_PATH}/api/simulation?resetViewport=true`);
}

window.addEventListener('resize', resizeCanvas);
resizeCanvas();
setInterval(updateSimulation, 1000 / 30);
