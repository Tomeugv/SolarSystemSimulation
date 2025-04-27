package com.solar.model;

import com.solar.DbManager;
import com.solar.PhysicsEngine;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
//Gestiona l'estat complet de la simulació
public class SimulationState {
    private List<CelestialBody> bodies;
    private final List<CelestialBody> initialBodies;

    public SimulationState() throws SQLException {
        this.initialBodies = Collections.unmodifiableList(DbManager.loadInitialState());
        reset();
    }

    // Constructor per carregar unicament els planetes seleccionats
    public SimulationState(List<String> selectedNames) throws SQLException {
        this.initialBodies = Collections.unmodifiableList(DbManager.loadSelectedBodies(selectedNames));
        reset();
    }

    public synchronized void reset() throws SQLException {
        // Ens asseguram que cuan reiniciam la simulació el splanetes tornen a la posició inicial
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


    public synchronized void reload() throws SQLException {
        // Ens asseguram que cuan recarregam la simulació el splanetes tornen a la posició inicial
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

