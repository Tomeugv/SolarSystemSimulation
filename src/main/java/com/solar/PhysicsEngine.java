package com.solar;
import com.solar.model.CelestialBody;
import java.util.List;

public class PhysicsEngine {
    private static final double G = 6.67430e-11;
    
    // Using Verlet integration for better stability
    public static void update(List<CelestialBody> bodies, double dt) {
        // Store previous positions
        for (CelestialBody body : bodies) {
            body.setPrevX(body.getX());
            body.setPrevY(body.getY());
        }
        
        // Calculate forces and update positions
        for (CelestialBody a : bodies) {
            double fx = 0, fy = 0;
            for (CelestialBody b : bodies) {
                if (a == b) continue;
                
                double dx = b.getX() - a.getX();
                double dy = b.getY() - a.getY();
                double distSq = dx*dx + dy*dy;
                double dist = Math.sqrt(distSq);
                double force = G * a.getMass() * b.getMass() / distSq;
                
                fx += force * dx/dist;
                fy += force * dy/dist;
            }
            
            // Verlet integration
            double newX = 2*a.getX() - a.getPrevX() + fx/a.getMass() * dt*dt;
            double newY = 2*a.getY() - a.getPrevY() + fy/a.getMass() * dt*dt;
            
            // Update velocities
            a.setVx((newX - a.getPrevX()) / (2*dt));
            a.setVy((newY - a.getPrevY()) / (2*dt));
            
            a.setX(newX);
            a.setY(newY);
        }
    }
    
    // Helper method to calculate initial state from orbital elements
    public static double[] calculateInitialState(double a, double e, double i, double M, double mass) {
        // Simplified conversion - assumes orbit in xy plane
        double E = M; // Initial guess for eccentric anomaly
        for (int j = 0; j < 5; j++) { // Newton-Raphson iteration
            E = E - (E - e * Math.sin(E) - M) / (1 - e * Math.cos(E));
        }
        
        double x = a * (Math.cos(E) - e);
        double y = a * Math.sqrt(1 - e*e) * Math.sin(E);
        double vx = -Math.sqrt(G * 1.989e30 / a) * Math.sin(E) / (1 - e * Math.cos(E));
        double vy = Math.sqrt(G * 1.989e30 / a) * Math.sqrt(1 - e*e) * Math.cos(E) / (1 - e * Math.cos(E));
        
        // Apply inclination (simplified)
        double z = y * Math.sin(i);
        y = y * Math.cos(i);
        
        return new double[]{x, y, vx, vy};
    }
}