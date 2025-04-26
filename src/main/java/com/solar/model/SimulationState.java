package com.solar.model;
import com.solar.DbManager;
import java.sql.SQLException;
import java.util.List;

public class SimulationState {
    private List<CelestialBody> bodies;
    
    public SimulationState() throws SQLException {
        this.bodies = DbManager.loadInitialState();
    }
    
    public List<CelestialBody> getBodies() {
        return bodies;
    }
    
    public void saveState() throws SQLException {
        DbManager.saveState(bodies);
    }
}