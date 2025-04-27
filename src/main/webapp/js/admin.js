// Función principal de inicialización
function initAdminPanel() {
    // Elementos del panel de administración
    const openBtn = document.getElementById('openAdminBtn');
    const closeBtn = document.getElementById('closeAdminBtn');
    const adminPanel = document.getElementById('adminPanel');
    
    // Verificar que todos los elementos existen
    if (!openBtn || !closeBtn || !adminPanel) {
        console.error("No se encontraron todos los elementos necesarios");
        return;
    }

    // Evento para abrir el panel
    openBtn.addEventListener('click', function() {
        adminPanel.style.right = '0';
        openBtn.style.display = 'none';
    });

    // Evento para cerrar el panel
    closeBtn.addEventListener('click', function() {
        adminPanel.style.right = '-400px';
        openBtn.style.display = 'block';
    });

    // Cargar lista de planetas al iniciar
    fetchPlanets();

    // Configurar evento para añadir planetas
    document.getElementById('addPlanetForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        await addPlanet();
    });

    // Configurar evento para resetear la base de datos
    document.getElementById('resetDatabaseBtn').addEventListener('click', resetDatabase);
}

// Función para cargar los planetas
async function fetchPlanets() {
    try {
        const response = await fetch('/SolarSystemSimulation/api/planetadmin');
        const planets = await response.json();
        renderPlanetList(planets);
    } catch (err) {
        console.error('Error al cargar planetas:', err);
    }
}

// Función para renderizar la lista de planetas
function renderPlanetList(planets) {
    const planetList = document.getElementById('planetList');
    planetList.innerHTML = '';
    
    planets.forEach(planet => {
        const div = document.createElement('div');
        div.className = 'planet-item';
        div.innerHTML = `
            <strong>${planet.name}</strong>
            ${planet.name !== 'Sun' ? 
                `<button onclick="deletePlanet('${planet.name}')" class="control-btn" style="background:#c9302c;">
                    Delete
                </button>` : ''
            }
        `;
        planetList.appendChild(div);
    });
}

// Función para añadir un nuevo planeta
async function addPlanet() {
    const newPlanet = {
        name: document.getElementById('newName').value,
        mass: parseFloat(document.getElementById('newMass').value),
        semiMajorAxis: parseFloat(document.getElementById('newAxis').value),
        eccentricity: parseFloat(document.getElementById('newEccentricity').value),
        radius: parseInt(document.getElementById('newRadius').value),
        color: document.getElementById('newColor').value
    };

    try {
        await fetch('/SolarSystemSimulation/api/planetadmin', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(newPlanet)
        });
        document.getElementById('addPlanetForm').reset();
        fetchPlanets();
    } catch (err) {
        console.error('Error al añadir planeta:', err);
    }
}

// Función para eliminar un planeta
async function deletePlanet(name) {
    if (!confirm(`¿Seguro que quieres eliminar ${name}?`)) return;
    
    try {
        await fetch(`/SolarSystemSimulation/api/planetadmin?name=${encodeURIComponent(name)}`, {
            method: 'DELETE'
        });
        fetchPlanets();
    } catch (err) {
        console.error('Error al eliminar planeta:', err);
    }
}

// Función para resetear la base de datos
async function resetDatabase() {
    if (!confirm('¿Resetear todos los planetas a los valores por defecto?')) return;
    
    try {
        await fetch('/SolarSystemSimulation/api/planetadmin/reset', {
            method: 'POST'
        });
        fetchPlanets();
    } catch (err) {
        console.error('Error al resetear la base de datos:', err);
    }
}

// Inicialización cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', initAdminPanel);
