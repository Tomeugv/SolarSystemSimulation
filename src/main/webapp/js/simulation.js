// Canvas setup
const canvas = document.getElementById('solarCanvas');
const ctx = canvas.getContext('2d');
const centerX = canvas.width / 2;
const centerY = canvas.height / 2;

// Scale down astronomical units for display
const scale = 1e9; 

function drawBody(x, y, radius, color) {
    ctx.beginPath();
    ctx.arc(x, y, radius, 0, Math.PI * 2);
    ctx.fillStyle = color;
    ctx.fill();
}

function updateSimulation(data) {
    // Clear canvas
    ctx.fillStyle = 'rgba(0, 0, 0, 0.1)'; // Semi-transparent for trails
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    
    // Draw all celestial bodies
    data.forEach(body => {
        const screenX = centerX + (body.x / scale);
        const screenY = centerY + (body.y / scale);
        drawBody(screenX, screenY, body.radius, body.color);
    });
}

// Fetch data from backend
function fetchData() {
    fetch('/SolarSystemSimulation/simulation')
        .then(response => response.json())
        .then(data => {
            updateSimulation(data);
            requestAnimationFrame(fetchData); // Smooth animation
        });
}

// Start simulation
fetchData();