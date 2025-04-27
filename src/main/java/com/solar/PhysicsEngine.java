package com.solar;

import com.solar.model.CelestialBody;
import java.util.List;

public class PhysicsEngine {
    // Physical constants
    private static final double G = 6.67430e-11;  // Gravitational constant (m^3 kg^-1 s^-2)
    private static final double AU = 1.496e11;     // Astronomical unit in meters
    private static final double SOFTENING = 1e9;   // Prevent division by zero (meters)
    
    // Time parameters
    private static final double BASE_TIME_STEP = 3600; // 1 hour in seconds
    
    /**
     * Updates all celestial bodies' positions and velocities
     * @param bodies List of celestial bodies
     * @param timeScale Time scaling factor (1.0 = real-time)
     */
    public static void update(List<CelestialBody> bodies, double timeScale) {
        double effectiveTimeStep = BASE_TIME_STEP * timeScale;
        
        // Calculate all forces first (O(n^2) n-body problem)
        double[][] forces = calculateGravitationalForces(bodies);
        
        // Update velocities and positions
        updatePositionsAndVelocities(bodies, forces, effectiveTimeStep);
    }
    
    /**
     * Initializes stable orbital velocities around the Sun
     */
    public static void initializeOrbits(List<CelestialBody> bodies) {
        CelestialBody sun = findSun(bodies);
        
        for (CelestialBody body : bodies) {
            if (body == sun) continue;
            
            // Calculate orbital parameters
            double dx = body.getX() - sun.getX();
            double dy = body.getY() - sun.getY();
            double r = Math.sqrt(dx*dx + dy*dy + SOFTENING);
            
            // Circular orbit velocity (v = sqrt(G*M/r))
            double orbitalVelocity = Math.sqrt(G * sun.getMass() / r);
            
            // Set velocity perpendicular to position vector
            double angle = Math.atan2(dy, dx);
            body.setVx(-orbitalVelocity * Math.sin(angle));
            body.setVy(orbitalVelocity * Math.cos(angle));
        }
    }
    
    // Private helper methods
    private static double[][] calculateGravitationalForces(List<CelestialBody> bodies) {
        double[][] forces = new double[bodies.size()][2];
        
        for (int i = 0; i < bodies.size(); i++) {
            CelestialBody a = bodies.get(i);
            for (int j = i + 1; j < bodies.size(); j++) {
                CelestialBody b = bodies.get(j);
                
                // Calculate distance components
                double dx = b.getX() - a.getX();
                double dy = b.getY() - a.getY();
                double rSquared = dx*dx + dy*dy + SOFTENING;
                double r = Math.sqrt(rSquared);
                
                // Gravitational force (F = G*m1*m2/r^2)
                double force = G * a.getMass() * b.getMass() / rSquared;
                double fx = force * dx/r;
                double fy = force * dy/r;
                
                // Apply equal and opposite forces
                forces[i][0] += fx;
                forces[i][1] += fy;
                forces[j][0] -= fx;
                forces[j][1] -= fy;
            }
        }
        return forces;
    }
    
    private static void updatePositionsAndVelocities(List<CelestialBody> bodies, 
            double[][] forces, double timeStep) {
        for (int i = 0; i < bodies.size(); i++) {
            CelestialBody body = bodies.get(i);
            
            // Update velocity (F = ma -> a = F/m)
            double ax = forces[i][0] / body.getMass();
            double ay = forces[i][1] / body.getMass();
            body.setVx(body.getVx() + ax * timeStep);
            body.setVy(body.getVy() + ay * timeStep);
            
            // Update position
            body.setX(body.getX() + body.getVx() * timeStep);
            body.setY(body.getY() + body.getVy() * timeStep);
        }
    }
    
    private static CelestialBody findSun(List<CelestialBody> bodies) {
        return bodies.stream()
            .filter(b -> "Sun".equals(b.getName()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Solar system must contain a Sun"));
    }
    
    /**
     * Converts world coordinates to screen coordinates
     */
    public static double[] toScreenCoordinates(double worldX, double worldY, 
            double centerX, double centerY, double scale) {
        return new double[] {
            centerX + (worldX / AU * scale),
            centerY + (worldY / AU * scale)
        };
    }
}