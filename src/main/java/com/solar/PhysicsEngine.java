package com.solar;

import java.util.List;

public class PhysicsEngine {
    private static final double G = 6.67430e-11;
    
    public static void update(List<CelestialBody> bodies, double dt) {
        // Calculate gravitational forces
        for (CelestialBody a : bodies) {
            if (a.getName().equals("Sun")) continue;
            
            double fx = 0, fy = 0;
            for (CelestialBody b : bodies) {
                if (a == b) continue;
                
                double dx = b.getX() - a.getX();
                double dy = b.getY() - a.getY();
                double dist = Math.sqrt(dx*dx + dy*dy);
                double force = G * a.getMass() * b.getMass() / (dist*dist);
                
                fx += force * dx/dist;
                fy += force * dy/dist;
            }
            
            // Update velocities (F=ma)
            a.setVx(a.getVx() + fx/a.getMass() * dt);
            a.setVy(a.getVy() + fy/a.getMass() * dt);
        }
        
        // Update positions
        for (CelestialBody body : bodies) {
            body.setX(body.getX() + body.getVx() * dt);
            body.setY(body.getY() + body.getVy() * dt);
        }
    }
}