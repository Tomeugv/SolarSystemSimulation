package com.solar;

import com.solar.model.CelestialBody;
import java.util.List;

public class PhysicsEngine {
    private static final double G = 6.67430e-11;
    private static final double TIME_STEP = 60 * 60 * 6; // 6 hours in seconds
    private static final double SOFTENING = 1e9;

    public static void update(List<CelestialBody> bodies, double timeScale) {
        // Calculate all forces first
        for (CelestialBody body : bodies) {
            double fx = 0.0, fy = 0.0;
            
            for (CelestialBody other : bodies) {
                if (body == other) continue;
                
                double dx = other.getX() - body.getX();
                double dy = other.getY() - body.getY();
                double rSquared = dx*dx + dy*dy;
                double r = Math.sqrt(rSquared + SOFTENING*SOFTENING);
                
                double force = G * body.getMass() * other.getMass() / (r*r);
                fx += force * dx/r;
                fy += force * dy/r;
            }
            
            // Update velocity
            body.setVx(body.getVx() + fx/body.getMass() * TIME_STEP * timeScale);
            body.setVy(body.getVy() + fy/body.getMass() * TIME_STEP * timeScale);
        }
        
        // Update positions
        for (CelestialBody body : bodies) {
            body.setX(body.getX() + body.getVx() * TIME_STEP * timeScale);
            body.setY(body.getY() + body.getVy() * TIME_STEP * timeScale);
        }
    }
    
    public static void initializeOrbits(List<CelestialBody> bodies) {
        CelestialBody sun = bodies.stream()
            .filter(b -> "Sun".equals(b.getName()))
            .findFirst()
            .orElse(null);
        
        if (sun == null) return;
        
        for (CelestialBody body : bodies) {
            if (body == sun) continue;
            
            double dx = body.getX() - sun.getX();
            double dy = body.getY() - sun.getY();
            double r = Math.sqrt(dx*dx + dy*dy);
            
            // Circular orbit velocity
            double v = Math.sqrt(G * sun.getMass() / r);
            
            // Tangential direction
            body.setVx(-v * dy/r);
            body.setVy(v * dx/r);
        }
    }
}