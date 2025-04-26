const canvas = document.getElementById('solarCanvas');
const ctx = canvas.getContext('2d');

// Simulation State
let bodies = [];
const planetWorldTraces = new Map();
const MAX_TRACE_POINTS = 500;

// Display Settings
let currentScale = 100;
let targetScale = 100;
let showTraces = true;
let centerX = 0;
let centerY = 0;
let isDragging = false;
let lastX = 0, lastY = 0;

// UI Elements
const uiContainer = document.createElement('div');
uiContainer.className = 'sim-controls';
document.body.appendChild(uiContainer);

// Control Buttons
const zoomInBtn = createButton('+', () => targetScale *= 1.2);
const zoomOutBtn = createButton('-', () => targetScale /= 1.2);
const traceToggle = createButton('Hide Traces', toggleTraces);
const resetBtn = createButton('Reset Simulation', resetSimulation);
resetBtn.classList.add('reset-btn');

// Sliders and Labels
const timeSlider = createSlider(0.1, 100, 1);
const timeLabel = createLabel('Time Scale: 1.0x');
const scaleDisplay = createLabel(`1 AU = ${currentScale.toFixed(0)}px`);

// Initialize UI
setupUI();
resizeCanvas();
animate();

// Main Functions
function createButton(text, onClick) {
    const btn = document.createElement('button');
    btn.textContent = text;
    btn.className = 'control-btn';
    btn.addEventListener('click', onClick);
    return btn;
}

function createSlider(min, max, value) {
    const slider = document.createElement('input');
    slider.type = 'range';
    slider.min = min;
    slider.max = max;
    slider.value = value;
    slider.className = 'control-slider';
    return slider;
}

function createLabel(text) {
    const label = document.createElement('div');
    label.className = 'control-label';
    label.textContent = text;
    return label;
}

function setupUI() {
    uiContainer.append(
        zoomInBtn, zoomOutBtn,
        document.createElement('br'),
        timeLabel,
        document.createElement('br'),
        timeSlider,
        document.createElement('br'),
        scaleDisplay,
        document.createElement('br'),
        traceToggle,
        document.createElement('br'),
        resetBtn
    );
}

function toggleTraces() {
    showTraces = !showTraces;
    traceToggle.textContent = showTraces ? 'Hide Traces' : 'Show Traces';
}

async function resetSimulation() {
    try {
        // Clear traces immediately for better UX
        planetWorldTraces.clear();
        
        // Reset camera position
        targetScale = 100;
        if (!isDragging) {
            centerX = canvas.width / 2;
            centerY = canvas.height / 2;
        }
        
        // Get your application context (same as in fetchData)
        const APP_CONTEXT = '/SolarSystemSimulation'; // CHANGE TO YOUR APP NAME
        
        const response = await fetch(`${APP_CONTEXT}/api/simulation/reset`, {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Reset failed');
        }
        
        // Update with fresh data
        bodies = await response.json();
        console.log("Reset successful, bodies:", bodies);
        
    } catch (error) {
        console.error("Reset failed:", error);
        alert("Reset failed: " + error.message.split('<')[0]); // Clean HTML error
    }
}

function resizeCanvas() {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
    if (!isDragging) {
        centerX = canvas.width / 2;
        centerY = canvas.height / 2;
    }
}

function updateTraces() {
    bodies.forEach(body => {
        if (!planetWorldTraces.has(body.name)) {
            planetWorldTraces.set(body.name, []);
        }
        
        planetWorldTraces.get(body.name).push({
            x: body.x,
            y: body.y,
            color: body.color
        });
        
        if (planetWorldTraces.get(body.name).length > MAX_TRACE_POINTS) {
            planetWorldTraces.get(body.name).shift();
        }
    });
}

function drawTraces() {
    planetWorldTraces.forEach((points, name) => {
        if (points.length < 2) return;

        ctx.beginPath();
        const first = points[0];
        const firstX = centerX + (first.x / 1.496e11 * currentScale);
        const firstY = centerY + (first.y / 1.496e11 * currentScale);
        ctx.moveTo(firstX, firstY);

        for (let i = 1; i < points.length; i++) {
            const alpha = i / points.length * 0.7;
            const x = centerX + (points[i].x / 1.496e11 * currentScale);
            const y = centerY + (points[i].y / 1.496e11 * currentScale);
            
            ctx.strokeStyle = `${points[i].color}${Math.floor(alpha * 255).toString(16).padStart(2, '0')}`;
            ctx.lineWidth = 1;
            ctx.lineTo(x, y);
            ctx.stroke();
            ctx.beginPath();
            ctx.moveTo(x, y);
        }
    });
}

function drawPlanets() {
    bodies.forEach(body => {
        const x = centerX + (body.x / 1.496e11 * currentScale);
        const y = centerY + (body.y / 1.496e11 * currentScale);

        // Planet glow
        const gradient = ctx.createRadialGradient(x, y, 0, x, y, body.radius * 1.5);
        gradient.addColorStop(0, body.color);
        gradient.addColorStop(1, 'rgba(0,0,0,0)');
        
        ctx.beginPath();
        ctx.arc(x, y, body.radius * 1.5, 0, Math.PI * 2);
        ctx.fillStyle = gradient;
        ctx.fill();

        // Planet body
        ctx.beginPath();
        ctx.arc(x, y, body.radius, 0, Math.PI * 2);
        ctx.fillStyle = body.color;
        ctx.fill();

        // Planet label
        if (body.radius > 2) {
            ctx.fillStyle = 'rgba(0,0,0,0.7)';
            const textWidth = ctx.measureText(body.name).width;
            ctx.fillRect(x + body.radius + 3, y - 9, textWidth + 6, 18);
            
            ctx.fillStyle = 'white';
            ctx.font = '12px Arial';
            ctx.fillText(body.name, x + body.radius + 6, y + 3);
        }
    });
}

function render() {
    ctx.fillStyle = 'rgb(10, 5, 20)';
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    currentScale += (targetScale - currentScale) * 0.1;
    scaleDisplay.textContent = `1 AU = ${currentScale.toFixed(0)}px`;

    updateTraces();
    if (showTraces) drawTraces();
    drawPlanets();
}

async function fetchData() {
    try {
        // Use absolute URL including your application's context path
        const APP_CONTEXT = '/SolarSystemSimulation'; // CHANGE THIS TO YOUR APP NAME
        const response = await fetch(`${APP_CONTEXT}/api/simulation?scale=${timeSlider.value}`);
        
        if (!response.ok) {
            const error = await response.text();
            console.error("API Error:", error);
            throw new Error(`HTTP ${response.status}`);
        }
        
        return await response.json();
    } catch (error) {
        console.error("Fetch failed:", error);
        
        // Show error on canvas
        ctx.fillStyle = 'red';
        ctx.font = '16px Arial';
        ctx.fillText('API Connection Failed', 50, 50);
        
        return [];
    }
}

async function animate() {
    bodies = await fetchData();
    render();
    requestAnimationFrame(animate);
}

// Event Listeners
canvas.addEventListener('mousedown', (e) => {
    isDragging = true;
    lastX = e.clientX;
    lastY = e.clientY;
});

canvas.addEventListener('mousemove', (e) => {
    if (isDragging) {
        centerX += e.clientX - lastX;
        centerY += e.clientY - lastY;
        lastX = e.clientX;
        lastY = e.clientY;
    }
});

window.addEventListener('mouseup', () => isDragging = false);
window.addEventListener('resize', resizeCanvas);