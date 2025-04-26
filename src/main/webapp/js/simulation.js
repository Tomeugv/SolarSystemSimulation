const canvas = document.getElementById('solarCanvas');
const ctx = canvas.getContext('2d');
let bodies = [];
let scale = 100; // pixels per AU
let centerX = 0; // in simulation coordinates
let centerY = 0;

// UI controls
document.getElementById('zoomIn').addEventListener('click', () => scale *= 1.2);
document.getElementById('zoomOut').addEventListener('click', () => scale /= 1.2);

function resizeCanvas() {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
}

function drawBody(x, y, radius, color) {
    ctx.beginPath();
    ctx.arc(x, y, Math.max(1, radius), 0, Math.PI * 2);
    ctx.fillStyle = color;
    ctx.fill();
}

async function fetchData(timeScale = 1.0) {
    try {
        const response = await fetch(`/SolarSystemSimulation/api/simulation?scale=${timeScale}`);
        bodies = await response.json();
    } catch (error) {
        console.error("Fetch error:", error);
    }
}

function render() {
    // Clear with semi-transparent black for trails
    ctx.fillStyle = 'rgba(0, 0, 0, 0.05)';
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    
    // Convert simulation coords to screen coords
    const screenCenterX = canvas.width / 2;
    const screenCenterY = canvas.height / 2;
    
    // Draw all bodies
    bodies.forEach(body => {
        const screenX = screenCenterX + (body.x - centerX) / 1.496e11 * scale;
        const screenY = screenCenterY + (body.y - centerY) / 1.496e11 * scale;
        drawBody(screenX, screenY, body.radius, body.color);
        
        // Draw orbits (simplified)
        if (body.name !== "Sun") {
            ctx.beginPath();
            ctx.arc(screenCenterX, screenCenterY, 
                   Math.abs(body.x - centerX) / 1.496e11 * scale, 
                   0, Math.PI * 2);
            ctx.strokeStyle = body.color + "60";
            ctx.stroke();
        }
    });
}

async function animate() {
    await fetchData(1.0); // Default time scale
    render();
    requestAnimationFrame(animate);
}

// Initialize
window.addEventListener('resize', resizeCanvas);
resizeCanvas();
animate();