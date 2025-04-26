package com.solar;

import java.sql.*;
import com.solar.model.SimulationState;
import com.solar.model.CelestialBody;

public class Bbdd {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/solar_sim";
    private static final String USER = "root";
    private static final String PASS = "";

    public static void saveState(SimulationState state) throws SQLException {
        String sql = "INSERT INTO celestial_bodies (name, mass, x, y, vx, vy, radius, color) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Clear old data
            conn.createStatement().execute("DELETE FROM celestial_bodies");
            
            // Insert all bodies
            for (CelestialBody body : state.getBodies()) {
                stmt.setString(1, body.getName());
                stmt.setDouble(2, body.getMass());
                stmt.setDouble(3, body.getX());
                stmt.setDouble(4, body.getY());
                stmt.setDouble(5, body.getVx());
                stmt.setDouble(6, body.getVy());
                stmt.setInt(7, body.getRadius());
                stmt.setString(8, body.getColor());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }
    
    public static SimulationState loadState() throws SQLException {
        SimulationState state = new SimulationState();
        String sql = "SELECT * FROM celestial_bodies";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                CelestialBody body = new CelestialBody(
                    rs.getString("name"),
                    rs.getDouble("mass"),
                    rs.getDouble("x"),
                    rs.getDouble("y"),
                    rs.getDouble("vx"),
                    rs.getDouble("vy"),
                    rs.getInt("radius"),
                    rs.getString("color")
                );
                state.addBody(body);
            }
        }
        return state;
    }
}