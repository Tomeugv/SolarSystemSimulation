const canvas = document.getElementById('solarCanvas');
const ctx = canvas.getContext('2d');
let bodies = [];
let scale = 100;
let targetScale = 100;
let currentScale = 100;
let showOrbits = true;
let centerX = 0;
let centerY = 0;
let isDragging = false;
let lastX = 0, lastY = 0;

// UI Controls
const uiContainer = document.createElement('div');
uiContainer.style.position = 'absolute';
uiContainer.style.top = '20px';
uiContainer.style.left = '20px';
uiContainer.style.backgroundColor = 'rgba(0,0,0,0.7)';
uiContainer.style.padding = '10px';
uiContainer.style.borderRadius = '5px';
uiContainer.style.color = 'white';
document.body.appendChild(uiContainer);

// Zoom controls
const zoomInBtn = document.createElement('button');
zoomInBtn.textContent = '+';
const zoomOutBtn = document.createElement('button');
zoomOutBtn.textContent = '-';
uiContainer.appendChild(zoomInBtn);
uiContainer.appendChild(zoomOutBtn);

// Time controls
const timeLabel = document.createElement('div');
timeLabel.textContent = `Time Scale: 1.0x`;
uiContainer.appendChild(timeLabel);

const timeSlider = document.createElement('input');
timeSlider.type = 'range';
timeSlider.min = '0.1';
timeSlider.max = '100';
timeSlider.step = '0.1';
timeSlider.value = '1';
uiContainer.appendChild(timeSlider);

// Scale display
const scaleDisplay = document.createElement('div');
scaleDisplay.textContent = `1 AU = ${currentScale.toFixed(0)}px`;
uiContainer.appendChild(scaleDisplay);

// Orbit toggle
const orbitToggle = document.createElement('button');
orbitToggle.textContent = 'Hide Orbits';
uiContainer.appendChild(orbitToggle);

// Event Listeners
zoomInBtn.addEventListener('click', () => {
    targetScale *= 1.2;
});

zoomOutBtn.addEventListener('click', () => {
    targetScale /= 1.2;
});

timeSlider.addEventListener('input', (e) => {
    timeLabel.textContent = `Time Scale: ${e.target.value}x`;
});

orbitToggle.addEventListener('click', () => {
    showOrbits = !showOrbits;
    orbitToggle.textContent = showOrbits ? 'Hide Orbits' : 'Show Orbits';
});

// Camera dragging
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

// Rendering
function resizeCanvas() {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
    if (!isDragging) {
        centerX = canvas.width / 2;
        centerY = canvas.height / 2;
    }
}

function drawBody(x, y, radius, color) {
    // Glow effect
    const gradient = ctx.createRadialGradient(x, y, 0, x, y, radius * 1.5);
    gradient.addColorStop(0, color);
    gradient.addColorStop(1, 'rgba(0,0,0,0)');
    
    ctx.beginPath();
    ctx.arc(x, y, radius * 1.5, 0, Math.PI * 2);
    ctx.fillStyle = gradient;
    ctx.fill();
    
    // Planet body
    ctx.beginPath();
    ctx.arc(x, y, radius, 0, Math.PI * 2);
    ctx.fillStyle = color;
    ctx.fill();
}

async function fetchData() {
    try {
        const timeScale = timeSlider.value;
        const response = await fetch(`/SolarSystemSimulation/api/simulation?scale=${timeScale}`);
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        return await response.json();
    } catch (error) {
        console.error("Fetch error:", error);
        return [];
    }
}

function render() {
    // Clear canvas completely
    ctx.fillStyle = 'rgb(0, 0, 0)';
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    
    // Smooth zoom transition
    currentScale += (targetScale - currentScale) * 0.1;
    scaleDisplay.textContent = `1 AU = ${currentScale.toFixed(0)}px`;
    
    // Find the Sun for orbit calculations
    const sun = bodies.find(b => b.name === "Sun");
    const sunX = sun ? centerX + (sun.x / 1.496e11 * currentScale) : centerX;
    const sunY = sun ? centerY + (sun.y / 1.496e11 * currentScale) : centerY;
    
    // Draw orbits if enabled
    if (showOrbits && sun) {
        bodies.forEach(body => {
            if (body.name !== "Sun" && body.semiMajorAxis) {
                ctx.beginPath();
                ctx.ellipse(
                    sunX, sunY,
                    body.semiMajorAxis / 1.496e11 * currentScale,
                    body.semiMajorAxis / 1.496e11 * currentScale * (1 - body.eccentricity),
                    0, 0, Math.PI * 2
                );
                ctx.strokeStyle = body.color + "40";
                ctx.lineWidth = 1;
                ctx.stroke();
            }
        });
    }
    
    // Draw all bodies
    bodies.forEach(body => {
        const screenX = centerX + (body.x / 1.496e11 * currentScale);
        const screenY = centerY + (body.y / 1.496e11 * currentScale);
        drawBody(screenX, screenY, body.radius, body.color);
        
        // Draw planet labels
        if (body.radius > 5) {
            ctx.fillStyle = 'white';
            ctx.font = '12px Arial';
            ctx.fillText(body.name, screenX + body.radius + 5, screenY);
        }
    });
}

// Animation loop
async function animate() {
    bodies = await fetchData();
    render();
    requestAnimationFrame(animate);
}

// Initialize
window.addEventListener('resize', resizeCanvas);
resizeCanvas();
animate();