package com.solar;

import com.solar.model.CelestialBody;
import java.util.List;

/**
 * Motor de simulació física per al sistema solar.
 * Gestiona les forces gravitatòries i el moviment orbital dels cossos celestes.
 */
public class PhysicsEngine {
    // Constants físiques
    private static final double G = 6.67430e-11;  // Constant gravitacional 
    private static final double AU = 1.496e11;     // Unitat astronòmica en metres
    private static final double SOFTENING = 1e9;   // Factor de suavitzat per evitar divisions per zero
    
    // Paràmetres temporals
    private static final double BASE_TIME_STEP = 600; // Pas de temps base

    /**
     * Actualitza les posicions i velocitats de tots els cossos celestes.
     */
    public static void update(List<CelestialBody> bodies, double timeScale) {
        double effectiveTimeStep = BASE_TIME_STEP * timeScale;
        
        // 1. Calcula totes les forces gravitatòries 
        double[][] forces = calculateGravitationalForces(bodies);
        
        // 2. Actualitza velocitats i posicions
        updatePositionsAndVelocities(bodies, forces, effectiveTimeStep);
    }
    
    /**
     * Inicialitza òrbites estables al voltant del Sol.

     */
    public static void initializeOrbits(List<CelestialBody> bodies) {
        CelestialBody sun = findSun(bodies);

        for (CelestialBody body : bodies) {
            if (body == sun) continue; // El Sol no orbita a si mateix

            // Càlcul de distància al Sol
            double dx = body.getX() - sun.getX();
            double dy = body.getY() - sun.getY();
            double r = Math.sqrt(dx * dx + dy * dy + SOFTENING);

            double semiMajorAxis = body.getSemiMajorAxis();
            double orbitalVelocity;

            if (semiMajorAxis > 0) {
                // Fórmula vis-viva per a òrbites el·líptiques
                orbitalVelocity = Math.sqrt(G * sun.getMass() * (2.0 / r - 1.0 / semiMajorAxis));
            } else {
                // Velocitat orbital circular
                orbitalVelocity = Math.sqrt(G * sun.getMass() / r);
            }

            // Càlcul de components de velocitat
            double angle = Math.atan2(dy, dx);
            body.setVx(-orbitalVelocity * Math.sin(angle)); // Component x
            body.setVy(orbitalVelocity * Math.cos(angle));  // Component y
        }
    }


    /**
     * Calcula les forces gravitatòries entre tots els cossos.
     */
    private static double[][] calculateGravitationalForces(List<CelestialBody> bodies) {
        double[][] forces = new double[bodies.size()][2]; // Matriu de forces [x,y] per cada cos
        
        for (int i = 0; i < bodies.size(); i++) {
            CelestialBody a = bodies.get(i);
            for (int j = i + 1; j < bodies.size(); j++) {
                CelestialBody b = bodies.get(j);
                
                // Vector distància entre cossos
                double dx = b.getX() - a.getX();
                double dy = b.getY() - a.getY();
                double rSquared = dx*dx + dy*dy + SOFTENING; // Distància al quadrat + suavitzat
                double r = Math.sqrt(rSquared);
                
                // Llei de gravitació universal
                double force = G * a.getMass() * b.getMass() / rSquared;
                double fx = force * dx/r; // Component x de la força
                double fy = force * dy/r; // Component y de la força
                
                // Aplicar forces iguals i oposades (3a llei de Newton)
                forces[i][0] += fx;
                forces[i][1] += fy;
                forces[j][0] -= fx;
                forces[j][1] -= fy;
            }
        }
        return forces;
    }
    
    /**
     * Actualitza posicions i velocitats usant integració Euler simple.
     */
    private static void updatePositionsAndVelocities(List<CelestialBody> bodies, 
            double[][] forces, double timeStep) {
        for (int i = 0; i < bodies.size(); i++) {
            CelestialBody body = bodies.get(i);
            
            // Acceleració = Força / Massa 
            double ax = forces[i][0] / body.getMass();
            double ay = forces[i][1] / body.getMass();
            
            // Actualitzar velocitat 
            body.setVx(body.getVx() + ax * timeStep);
            body.setVy(body.getVy() + ay * timeStep);
            
            // Actualitzar posició
            body.setX(body.getX() + body.getVx() * timeStep);
            body.setY(body.getY() + body.getVy() * timeStep);
        }
    }
    
    /**
     * Troba el Sol a la llista de cossos celestes.
     */
    private static CelestialBody findSun(List<CelestialBody> bodies) {
        return bodies.stream()
            .filter(b -> "Sun".equals(b.getName()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("El sistema solar ha de contenir un Sol"));
    }
    
    /**
     * Converteix coordenades a coordenades de pantalla.
     */
    public static double[] toScreenCoordinates(double worldX, double worldY, 
            double centerX, double centerY, double scale) {
        return new double[] {
            centerX + (worldX / AU * scale), // Conversió UA a píxels
            centerY + (worldY / AU * scale)
        };
    }
}