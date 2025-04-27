package com.solar.model;

import com.solar.DbManager;
import com.solar.PhysicsEngine;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Classe que gestiona l'estat de la simulació del sistema solar.
 * Conté la llista de cossos celestes i ofereix mètodes per gestionar-ne l'estat.
 */
public class SimulationState {
    private List<CelestialBody> bodies;                  // Llista actual de cossos celestes
    private final List<CelestialBody> initialBodies;     // Llista inicial de cossos (immutable)

    /**
     * Constructor per defecte que carrega TOTS els planetes de la base de dades.
     * @throws SQLException Si hi ha un error en accedir a la base de dades
     */
    public SimulationState() throws SQLException {
        this.initialBodies = Collections.unmodifiableList(DbManager.loadInitialState());
        reset();
    }

    /**
     * Constructor que carrega només els planetes SELECCIONATS.
     * @param selectedNames Llista de noms dels cossos a carregar
     * @throws SQLException Si hi ha un error en accedir a la base de dades
     */
    public SimulationState(List<String> selectedNames) throws SQLException {
        this.initialBodies = Collections.unmodifiableList(DbManager.loadSelectedBodies(selectedNames));
        reset();
    }

    /**
     * Reinicia l'estat de la simulació al seu estat inicial.
     * @throws SQLException Si hi ha un error en accedir a la base de dades
     */
    public synchronized void reset() throws SQLException {
        // Carrega l'estat inicial de la base de dades
        List<CelestialBody> freshBodies = DbManager.loadInitialState();
        this.bodies = new CopyOnWriteArrayList<>();

        // Crea còpies dels cossos celestes per evitar modificacions accidentals
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

    /**
     * Recarrega tots els cossos celestes des de la base de dades.
     * @throws SQLException Si hi ha un error en accedir a la base de dades
     */
    public synchronized void reload() throws SQLException {
        // Carrega l'estat complet de la base de dades
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

    /**
     * Retorna una llista immodificable dels cossos celestes actuals.
     * @return Llista de cossos celestes
     */
    public List<CelestialBody> getBodies() {
        return Collections.unmodifiableList(bodies);
    }

    /**
     * Desa l'estat actual de la simulació a la base de dades.
     * @throws SQLException Si hi ha un error en accedir a la base de dades
     */
    public void saveState() throws SQLException {
        DbManager.saveState(this.bodies);
    }
}

