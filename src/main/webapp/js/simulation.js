/**
 * Obté els planetes seleccionats dels checkboxes
 * @returns {Array} Llista de noms de planetes seleccionats
 */
function getSelectedPlanets() {
    const selected = Array.from(document.querySelectorAll('.planet-select input:checked'))
        .map(checkbox => checkbox.value);
    console.log("Planetes seleccionats:", selected); // Per depuració
    return selected;
}

/* ==================== ESTAT GLOBAL ==================== */
let bodies = []; // Llista de cossos celestes actuals
let planetTraces = new Map(); // Traces orbitals dels planetes
const MAX_TRACE_POINTS = 500; // Punts màxims per traça orbital

/* ==================== ELEMENTS UI ==================== */
const canvas = document.getElementById('solarCanvas');
const ctx = canvas.getContext('2d');
const timeSlider = document.getElementById('timeSlider');
const scaleDisplay = document.getElementById('scaleDisplay');
const showTracesCheckbox = document.getElementById('showTraces');

// Ruta base dinàmica per a les crides API
const BASE_PATH = '/SolarSystemSimulation';

/* ==================== ESTAT DE LA CÀMERA ==================== */
let isDragging = false; // Si l'usuari està arrossegant la vista
let lastX = 0, lastY = 0; // Última posició del ratolí
let centerX = canvas.width / 2; // Centre X de la vista
let centerY = canvas.height / 2; // Centre Y de la vista
let currentScale = 173; // Escala actual (1 AU = 173px per defecte)

/* ==================== FUNCIONS D'INTERFÍCIE ==================== */

/**
 * Mostra l'overlay de càrrega
 */
function showLoading() {
    document.getElementById('loadingOverlay').style.display = 'flex';
}

/**
 * Amaga l'overlay de càrrega
 */
function hideLoading() {
    document.getElementById('loadingOverlay').style.display = 'none';
}

/* ==================== GESTIÓ D'EVENTS ==================== */

// Event de clic i arrossegament al canvas
canvas.addEventListener('mousedown', (e) => {
    isDragging = true;
    lastX = e.clientX;
    lastY = e.clientY;
});

// Event de moviment del ratolí (per arrossegament de la vista)
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

// Event per finalitzar l'arrossegament
window.addEventListener('mouseup', () => isDragging = false);

// Zoom in
document.getElementById('zoomIn').addEventListener('click', async () => {
    await fetch(`${BASE_PATH}/api/simulation?zoom=in`);
    await updateSimulation();
});

// Zoom out
document.getElementById('zoomOut').addEventListener('click', async () => {
    await fetch(`${BASE_PATH}/api/simulation?zoom=out`);
    await updateSimulation();
});

// Control de velocitat de simulació
timeSlider.addEventListener('input', async () => {
    await updateSimulation();
});

document.getElementById('startSim').addEventListener('click', async () => {
    try {
        const selectedPlanets = getSelectedPlanets(); // Ara hauria de funcionar
        
        // Assegura't que el Sol està sempre present
        if (!selectedPlanets.includes('Sun')) {
            selectedPlanets.unshift('Sun');
        }

        showLoading();

        const response = await fetch(`${BASE_PATH}/api/simulation/start`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(selectedPlanets)
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        // Reinicia les traces i la vista
        planetTraces.clear(); 
        centerX = canvas.width / 2;
        centerY = canvas.height / 2;

        // Petita espera per assegurar la càrrega
        setTimeout(async () => {
            await updateSimulation();
            hideLoading();
        }, 100);

    } catch (error) {
        console.error("Error en iniciar simulació:", error);
        hideLoading();
        alert("Error en iniciar la simulació. Si us plau, torna-ho a provar.");
    }
});

// Reiniciar simulació
document.getElementById('resetSim').addEventListener('click', async () => {
    try {
        await fetch(`${BASE_PATH}/api/simulation/reset`, { method: 'POST' });
        planetTraces.clear();
        await updateSimulation();
    } catch (error) {
        console.error("Error en reiniciar:", error);
    }
});

/* ==================== FUNCIONS PRINCIPALS ==================== */

/**
 * Actualitza l'estat de la simulació des del servidor
 */
async function updateSimulation() {
    try {
        const response = await fetch(`${BASE_PATH}/api/simulation?scale=${timeSlider.value}`);
        const data = await response.json();
        
        bodies = data.bodies;
        currentScale = parseFloat(data.scale) || 173;
        scaleDisplay.textContent = `1 AU = ${currentScale.toFixed(0)}px`;

        render();
        hideLoading();

    } catch (error) {
        console.error("Error en actualitzar simulació:", error);
        hideLoading();
    }
}

/**
 * Renderitza tots els elements al canvas
 */
function render() {
    // Neteja el canvas
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    
    // Dibuixa traces orbitals si estan activades
    if (showTracesCheckbox.checked) {
        updateTraces();
        drawTraces();
    }
    
    // Dibuixa tots els cossos celestes
    bodies.forEach(body => {
        const screenPos = viewportToScreen(body.worldX, body.worldY);

        // Ajusta la mida segons el zoom
        const zoomMultiplier = calculateZoomMultiplier();
        const clampedMultiplier = Math.min(zoomMultiplier, 1.3);
        const dynamicRadius = Math.max(1, body.radius * clampedMultiplier);

        // Dibuixa el planeta
        ctx.beginPath();
        ctx.arc(screenPos.x, screenPos.y, dynamicRadius, 0, Math.PI * 2);
        ctx.fillStyle = body.color;
        ctx.fill();

        // Etiqueta el planeta
        ctx.fillStyle = 'white';
        ctx.font = '12px Arial';
        ctx.fillText(body.name, screenPos.x + dynamicRadius + 2, screenPos.y);
    });
}

/* ==================== FUNCIONS AUXILIARS ==================== */

/**
 * Calcula el multiplicador de zoom respecte l'escala base
 */
function calculateZoomMultiplier() {
    const baseScale = 173;
    return currentScale / baseScale;
}

/**
 * Actualitza les traces orbitals dels planetes
 */
function updateTraces() {
    bodies.forEach(body => {
        if (!planetTraces.has(body.name)) {
            planetTraces.set(body.name, []);
        }
        planetTraces.get(body.name).push({ x: body.worldX, y: body.worldY });

        // Limita el nombre de punts emmagatzemats
        if (planetTraces.get(body.name).length > MAX_TRACE_POINTS) {
            planetTraces.get(body.name).shift();
        }
    });
}

/**
 * Dibuixa les traces orbitals al canvas
 */
function drawTraces() {
    planetTraces.forEach((points, name) => {
        if (points.length < 2) return;
        
        ctx.beginPath();
        const start = viewportToScreen(points[0].x, points[0].y);
        ctx.moveTo(start.x, start.y);

        // Connexió de tots els punts de la traça
        for (let i = 1; i < points.length; i++) {
            const pos = viewportToScreen(points[i].x, points[i].y);
            ctx.lineTo(pos.x, pos.y);
        }

        // Estil de la traça (color del planeta + transparència)
        ctx.strokeStyle = (bodies.find(b => b.name === name)?.color || '#ffffff') + "60";
        ctx.lineWidth = 1;
        ctx.stroke();
    });
}

/**
 * Converteix coordenades del món a coordenades de pantalla
 */
function viewportToScreen(worldX, worldY) {
    const AU = 1.496e11; // 1 Unitat Astronòmica en metres
    return {
        x: centerX + (worldX / AU * currentScale),
        y: centerY + (worldY / AU * currentScale)
    };
}

/* ==================== INICIALITZACIÓ ==================== */

/**
 * Ajusta la mida del canvas a la finestra
 */
function resizeCanvas() {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;

    // Centra la vista
    centerX = canvas.width / 2;
    centerY = canvas.height / 2;

    // Notifica al servidor el canvi de mida
    fetch(`${BASE_PATH}/api/simulation?resetViewport=true`);
}

// Configura events i inicia la simulació
window.addEventListener('resize', resizeCanvas);
resizeCanvas();
setInterval(updateSimulation, 1000 / 30); // Actualitza a ~30fps