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

    // New constructor (load SELECTED planets)
    public SimulationState(List<String> selectedNames) throws SQLException {
        this.initialBodies = Collections.unmodifiableList(DbManager.loadSelectedBodies(selectedNames));
        reset();
    }

    public synchronized void reset() throws SQLException {
        // Reload initialBodies from database
        List<CelestialBody> freshBodies = DbManager.loadInitialState();
        this.bodies = new CopyOnWriteArrayList<>();

        for (CelestialBody body : freshBodies) {
            CelestialBody copy = new CelestialBody(
                body.getName(),
                body.getMass(),
                body.getX(),
                body.getY(),
                body.getVx(),
                body.getVy(),
                body.getRadius(),
                body.getColor()
            );
            copy.setSemiMajorAxis(body.getSemiMajorAxis());
            copy.setEccentricity(body.getEccentricity());
            this.bodies.add(copy);
        }
        PhysicsEngine.initializeOrbits(this.bodies);
    }


    //  Added reload() here
    public synchronized void reload() throws SQLException {
        // Reload full database
        List<CelestialBody> freshBodies = DbManager.loadInitialState();

        this.bodies = new CopyOnWriteArrayList<>();
        for (CelestialBody body : freshBodies) {
            CelestialBody copy = new CelestialBody(
                body.getName(),
                body.getMass(),
                body.getX(),
                body.getY(),
                body.getVx(),
                body.getVy(),
                body.getRadius(),
                body.getColor()
            );
            copy.setSemiMajorAxis(body.getSemiMajorAxis());
            copy.setEccentricity(body.getEccentricity());
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

