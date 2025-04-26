package com.solar.model;

import java.util.ArrayList;
import java.util.List;

public class SimulationState {
    private static final double G = 6.67430e-11;
    private List<CelestialBody> bodies = new ArrayList<>();
    
    public void addBody(CelestialBody body) {
        bodies.add(body);
    }
    public List<CelestialBody> getBodies() {
        return bodies; // Returns the list of celestial bodies
    }

    public void update(double dt) {
        // Same physics calculations as your original
        for (CelestialBody a : bodies) {
            if (a.getName().equals("Sun")) continue;

            double ax = 0, ay = 0;
            
            for (CelestialBody b : bodies) {
                if (a == b) continue;
                double dx = b.getX() - a.getX();
                double dy = b.getY() - a.getY();
                double distSq = dx*dx + dy*dy;
                double dist = Math.sqrt(distSq);
                double force = G * b.getMass() / distSq;
                ax += force * dx / dist;
                ay += force * dy / dist;
            }

            a.setVx(a.getVx() + ax * dt);
            a.setVy(a.getVy() + ay * dt);
        }

        // Update positions
        for (CelestialBody body : bodies) {
            if (body.getName().equals("Sun")) continue;
            body.setX(body.getX() + body.getVx() * dt);
            body.setY(body.getY() + body.getVy() * dt);
        }
    }

    public String toJSON() {
        StringBuilder json = new StringBuilder("[");
        for (CelestialBody body : bodies) {
            if (json.length() > 1) json.append(",");
            json.append(String.format(
                "{\"name\":\"%s\",\"mass\":%.2e,\"x\":%.2f,\"y\":%.2f,\"vx\":%.2f,\"vy\":%.2f,\"radius\":%d,\"color\":\"%s\"}",
                body.getName(),
                body.getMass(),
                body.getX(),
                body.getY(),
                body.getVx(),
                body.getVy(),
                body.getRadius(),
                body.getColor()
            ));
        }
        return json.append("]").toString();
    }
    
    // Getters and setters
}