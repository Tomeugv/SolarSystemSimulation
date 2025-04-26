const canvas = document.getElementById('solarCanvas');
const ctx = canvas.getContext('2d');
canvas.width = window.innerWidth;
canvas.height = window.innerHeight;
const AU_TO_PIXELS = 50;

// Default data if backend fails
let currentData = [
    { name: "Sun", x: 0, y: 0, radius: 20, color: "yellow" },
    { name: "Earth", x: 1.496e11, y: 0, radius: 8, color: "blue" }
];

function drawBody(x, y, radius, color) {
    ctx.beginPath();
    ctx.arc(x, y, radius, 0, Math.PI * 2);
    ctx.fillStyle = color;
    ctx.fill();
}

function render() {
    ctx.fillStyle = 'rgba(0, 0, 0, 0.1)';
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    
    const centerX = canvas.width / 2;
    const centerY = canvas.height / 2;
    
    currentData.forEach(body => {
        const screenX = centerX + (body.x / 1.496e11 * AU_TO_PIXELS);
        const screenY = centerY + (body.y / 1.496e11 * AU_TO_PIXELS);
        drawBody(screenX, screenY, body.radius, body.color);
    });
    
    requestAnimationFrame(render);
}

async function fetchData() {
    try {
        const response = await fetch('/SolarSystemSimulation/simulation');
        
        // Check if response is HTML (error case)
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('text/html')) {
            console.error('Server returned HTML instead of JSON');
            return;
        }
        
        const data = await response.json();
        currentData = data;
    } catch (error) {
        console.error('Fetch error:', error);
    }
}

// Start rendering and polling
render();
setInterval(fetchData, 1000); // Update every second