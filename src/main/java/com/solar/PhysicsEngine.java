package com.solar;

import com.solar.model.CelestialBody;
import java.util.List;

public class PhysicsEngine {
    // Physical constants
    private static final double G = 6.67430e-11; // Constant gravitacional
    private static final double AU = 1.496e11;   // Unitat Astronòmica
    private static final double SOFTENING = 1e9; // Evita divisions per zero
    private static final double BASE_TIME_STEP = 600; // 1 hora
    
    // Actualitza posicions i velocitats
    public static void update(List<CelestialBody> bodies, double timeScale) {
        double effectiveTimeStep = BASE_TIME_STEP * timeScale;
        double[][] forces = calculateGravitationalForces(bodies);
        updatePositionsAndVelocities(bodies, forces, effectiveTimeStep);
    }
    
    // Inicialitza òrbites estables
    public static void initializeOrbits(List<CelestialBody> bodies) {
        CelestialBody sun = findSun(bodies);
        for (CelestialBody body : bodies) {
            if (body == sun) continue;
            double dx = body.getX() - sun.getX();
            double dy = body.getY() - sun.getY();
            double r = Math.sqrt(dx * dx + dy * dy + SOFTENING);
            double semiMajorAxis = body.getSemiMajorAxis();
            double orbitalVelocity;
            if (semiMajorAxis > 0) {
                // formules per a calcular les òrbites
                orbitalVelocity = Math.sqrt(G * sun.getMass() * (2.0 / r - 1.0 / semiMajorAxis));
            } else {
                // formula simplificada per a casos límits
                orbitalVelocity = Math.sqrt(G * sun.getMass() / r);
            }
            double angle = Math.atan2(dy, dx);
            body.setVx(-orbitalVelocity * Math.sin(angle));
            body.setVy(orbitalVelocity * Math.cos(angle));
        }
    }

    
    // Calculam les forçe per a cada cos
    private static double[][] calculateGravitationalForces(List<CelestialBody> bodies) {
        double[][] forces = new double[bodies.size()][2];       
        for (int i = 0; i < bodies.size(); i++) {
            CelestialBody a = bodies.get(i);
            for (int j = i + 1; j < bodies.size(); j++) {
                CelestialBody b = bodies.get(j);                
                // Calculam els components de distància
                double dx = b.getX() - a.getX();
                double dy = b.getY() - a.getY();
                double rSquared = dx*dx + dy*dy + SOFTENING;
                double r = Math.sqrt(rSquared);              
                // Força gravitatòria (F = G*m1*m2/r^2)
                double force = G * a.getMass() * b.getMass() / rSquared;
                double fx = force * dx/r;
                double fy = force * dy/r;                
                // Components x i y de les foces
                forces[i][0] += fx;
                forces[i][1] += fy;
                forces[j][0] -= fx;
                forces[j][1] -= fy;
            }
        }
        return forces;
    }
    // Actualitza posicions i velocitats    
    private static void updatePositionsAndVelocities(List<CelestialBody> bodies, 
            double[][] forces, double timeStep) {
        for (int i = 0; i < bodies.size(); i++) {
            CelestialBody body = bodies.get(i);
            double ax = forces[i][0] / body.getMass();
            double ay = forces[i][1] / body.getMass();
            body.setVx(body.getVx() + ax * timeStep);
            body.setVy(body.getVy() + ay * timeStep);
            body.setX(body.getX() + body.getVx() * timeStep);
            body.setY(body.getY() + body.getVy() * timeStep);
        }
    }
    // Ens asseguram que el sol existesqui per poder resoldre el sistema
    private static CelestialBody findSun(List<CelestialBody> bodies) {
        return bodies.stream()
            .filter(b -> "Sun".equals(b.getName()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Solar system must contain a Sun"));
    }

}