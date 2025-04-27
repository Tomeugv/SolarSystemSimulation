// Obrir i tancar el panell d'administració
document.getElementById('openAdminBtn').addEventListener('click', () => {
    document.getElementById('adminPanel').style.right = '0';
});
document.getElementById('closeAdminBtn').addEventListener('click', () => {
    document.getElementById('adminPanel').style.right = '-400px';
});

// Carregar llista de planetes
async function fetchPlanets() {
    try {
        const response = await fetch('/SolarSystemSimulation/api/planetadmin');
        const planets = await response.json();

        const planetList = document.getElementById('planetList');
        planetList.innerHTML = '';

        planets.forEach(planet => {
            const div = document.createElement('div');
            div.innerHTML = `
                <strong>${planet.name}</strong>
                ${planet.name !== 'Sun' ? `<button onclick="deletePlanet('${planet.name}')" class="control-btn" style="background:#c9302c;">Eliminar</button>` : ''}
            `;
            planetList.appendChild(div);
        });

    } catch (err) {
        console.error('Error carregant planetes', err);
    }
}

// Afegir nou planeta
document.getElementById('addPlanetForm').addEventListener('submit', async (e) => {
    e.preventDefault();
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
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(newPlanet)
        });
        fetchPlanets();
    } catch (err) {
        console.error('Error afegint planeta', err);
    }
});

// Esborrar planeta
async function deletePlanet(name) {
    try {
        await fetch(`/SolarSystemSimulation/api/planetadmin?name=${encodeURIComponent(name)}`, {
            method: 'DELETE'
        });
        fetchPlanets();
    } catch (err) {
        console.error('Error esborrant planeta', err);
    }
}

// Reiniciar la base de dades
document.getElementById('resetDatabaseBtn').addEventListener('click', async () => {
    try {
        await fetch('/SolarSystemSimulation/api/planetadmin/reset', {
            method: 'POST'
        });
        fetchPlanets();
    } catch (err) {
        console.error('Error reiniciant base de dades', err);
    }
});

// Carrega inicial
fetchPlanets();

