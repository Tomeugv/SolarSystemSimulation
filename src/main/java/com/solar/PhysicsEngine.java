package com.solar;

import com.solar.model.CelestialBody;
import java.util.List;

public class PhysicsEngine {
    private static final double G = 6.67430e-11;
    private static final double TIME_STEP = 60 * 60; // 1 hour in seconds
    private static final double SUN_MASS = 1.989e30;
    
    public static void update(List<CelestialBody> bodies, double timeScale) {
        // First find the Sun (for barycenter calculation)
        CelestialBody sun = bodies.stream()
            .filter(b -> b.getName().equals("Sun"))
            .findFirst()
            .orElse(null);
        
        if (sun == null) return;

        // Calculate gravitational forces
        for (CelestialBody body : bodies) {
            if (body == sun) continue;
            
            double dx = sun.getX() - body.getX();
            double dy = sun.getY() - body.getY();
            double r = Math.sqrt(dx*dx + dy*dy);
            
            // Prevent extreme forces at small distances
            double minDistance = (sun.getRadius() + body.getRadius()) * 1000;
            double effectiveDistance = Math.max(r, minDistance);
            
            double force = G * sun.getMass() * body.getMass() / (effectiveDistance * effectiveDistance);
            double angle = Math.atan2(dy, dx);
            
            // Update velocities (tangential velocity for stable orbits)
            double orbitalVelocity = Math.sqrt(G * sun.getMass() / effectiveDistance);
            body.setVx(-orbitalVelocity * Math.sin(angle));
            body.setVy(orbitalVelocity * Math.cos(angle));
            
            // Update positions
            body.setX(body.getX() + body.getVx() * TIME_STEP * timeScale);
            body.setY(body.getY() + body.getVy() * TIME_STEP * timeScale);
        }
    }
}