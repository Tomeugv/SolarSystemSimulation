const canvas = document.getElementById('solarCanvas');
const ctx = canvas.getContext('2d');
let bodies = [];

function resizeCanvas() {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
}

function drawBody(x, y, radius, color) {
    ctx.beginPath();
    ctx.arc(x, y, radius, 0, Math.PI * 2);
    ctx.fillStyle = color;
    ctx.fill();
}

async function fetchData() {
    try {
        const response = await fetch('/SolarSystemSimulation/api/simulation');
        bodies = await response.json();
    } catch (error) {
        console.error("Fetch error:", error);
    }
}

function render() {
    // Clear with semi-transparent black for trails
    ctx.fillStyle = 'rgba(0, 0, 0, 0.05)';
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    
    // Center of screen
    const centerX = canvas.width / 2;
    const centerY = canvas.height / 2;
    
    // Draw all bodies (scale: 1 AU = 100 pixels)
    bodies.forEach(body => {
        const screenX = centerX + (body.x / 1.496e11 * 100);
        const screenY = centerY + (body.y / 1.496e11 * 100);
        drawBody(screenX, screenY, body.radius, body.color);
    });
}

async function animate() {
    await fetchData();
    render();
    requestAnimationFrame(animate);
}

// Initialize
window.addEventListener('resize', resizeCanvas);
resizeCanvas();
animate();