package com.solar.model;

import com.solar.DbManager;
import com.solar.PhysicsEngine;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class SimulationState {
    private List<CelestialBody> bodies;
    private final List<CelestialBody> initialBodies;
    
    // Default constructor (load ALL planets)
    public SimulationState() throws SQLException {
        this.initialBodies = Collections.unmodifiableList(DbManager.loadInitialState());
        reset();
    }
    
    //  New constructor (load SELECTED planets)
    public SimulationState(List<String> selectedNames) throws SQLException {
        this.initialBodies = Collections.unmodifiableList(DbManager.loadSelectedBodies(selectedNames));
        reset();
    }
    
    public synchronized void reset() throws SQLException {
        this.bodies = new CopyOnWriteArrayList<>();
        for (CelestialBody original : initialBodies) {
            CelestialBody copy = new CelestialBody(
                original.getName(),
                original.getMass(),
                original.getX(),
                original.getY(),
                original.getVx(),
                original.getVy(),
                original.getRadius(),
                original.getColor()
            );
            copy.setSemiMajorAxis(original.getSemiMajorAxis());
            copy.setEccentricity(original.getEccentricity());
            this.bodies.add(copy);
        }
        PhysicsEngine.initializeOrbits(this.bodies);
    }
    
    public List<CelestialBody> getBodies() {
        return Collections.unmodifiableList(bodies);
    }
    
    public void saveState() throws SQLException {
        DbManager.saveState(this.bodies);
    }
}
