package com.solar;
import com.solar.model.CelestialBody;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DbManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/solar_system";
    private static final String DB_USER = "root"; // Default username
    private static final String DB_PASSWORD = ""; // Default password (empty)
    

    
    public static List<CelestialBody> loadInitialState() throws SQLException {
        List<CelestialBody> bodies = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM celestial_bodies")) {
            
            while (rs.next()) {
                double[] initialState = PhysicsEngine.calculateInitialState(
                    rs.getDouble("semi_major_axis"),
                    rs.getDouble("eccentricity"),
                    rs.getDouble("inclination"),
                    rs.getDouble("mean_anomaly"),
                    rs.getDouble("mass")
                );
                
                bodies.add(new CelestialBody(
                    rs.getString("name"),
                    rs.getDouble("mass"),
                    initialState[0],
                    initialState[1],
                    initialState[2],
                    initialState[3],
                    rs.getInt("radius"),
                    rs.getString("color")
                ));
            }
        }
        return bodies;
    }
    
    public static void saveState(List<CelestialBody> bodies) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(
                 "UPDATE celestial_bodies SET x=?, y=?, vx=?, vy=? WHERE name=?")) {
            
            for (CelestialBody body : bodies) {
                ps.setDouble(1, body.getX());
                ps.setDouble(2, body.getY());
                ps.setDouble(3, body.getVx());
                ps.setDouble(4, body.getVy());
                ps.setString(5, body.getName());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
    static {
        try {
            System.out.println("=== LOADING DRIVER ===");
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("=== DRIVER LOADED ===");
        } catch (ClassNotFoundException e) {
            System.err.println("DRIVER NOT FOUND!");
            e.printStackTrace();
        }
    }
}