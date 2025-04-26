let scene, camera, renderer, bodies = [];
const scale = 1e9; // Scale factor for visualization

init();

function init() {
    // Three.js setup
    scene = new THREE.Scene();
    camera = new THREE.PerspectiveCamera(75, window.innerWidth/window.innerHeight, 0.1, 1000);
    renderer = new THREE.WebGLRenderer({ antialias: true });
    renderer.setSize(window.innerWidth, window.innerHeight);
    document.body.appendChild(renderer.domElement);

    // Lighting
    const light = new THREE.PointLight(0xffffff, 1, 100);
    light.position.set(0, 0, 0);
    scene.add(light);
    scene.add(new THREE.AmbientLight(0x404040));

    // Load initial state
    fetchSimulationState();
    
    // Animation loop
    animate();
}

function fetchSimulationState() {
    fetch('/SolarSystemSimulation/simulation')
        .then(response => response.json())
        .then(data => {
            updateBodies(data.bodies);
            // Schedule next update
            setTimeout(() => {
                fetch('/SolarSystemSimulation/simulation', { method: 'POST' })
                    .then(fetchSimulationState);
            }, 100);
        });
}

function updateBodies(bodiesData) {
    // Clear old bodies
    bodies.forEach(body => scene.remove(body.mesh));
    bodies = [];
    
    // Create new bodies
    bodiesData.forEach(bodyData => {
        const radius = Math.max(0.1, bodyData.radius / 10); // Scale down for visualization
        const geometry = new THREE.SphereGeometry(radius, 32, 32);
        const material = new THREE.MeshPhongMaterial({ color: new THREE.Color(bodyData.color) });
        const mesh = new THREE.Mesh(geometry, material);
        
        // Position in 3D space (z=0 for 2D simulation)
        mesh.position.set(bodyData.x/scale, bodyData.y/scale, 0);
        
        scene.add(mesh);
        bodies.push({
            data: bodyData,
            mesh: mesh
        });
    });
    
    // Center camera on Sun
    if (bodies.length > 0 && bodies[0].data.name === "Sun") {
        camera.position.z = 30; // Zoom level
    }
}

function animate() {
    requestAnimationFrame(animate);
    renderer.render(scene, camera);
    
    // Update body positions
    bodies.forEach(body => {
        body.mesh.position.x = body.data.x / scale;
        body.mesh.position.y = body.data.y / scale;
    });
}

window.addEventListener('resize', () => {
    camera.aspect = window.innerWidth / window.innerHeight;
    camera.updateProjectionMatrix();
    renderer.setSize(window.innerWidth, window.innerHeight);
});