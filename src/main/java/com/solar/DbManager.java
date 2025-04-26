package com.solar;

import com.solar.model.CelestialBody;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DbManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/solar_system";
    private static final String DB_USER = "your_username";
    private static final String DB_PASSWORD = "your_password";
    
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        }
    }
    
    public static List<CelestialBody> loadInitialState() throws SQLException {
        List<CelestialBody> bodies = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM celestial_bodies")) {
            
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
                body.setSemiMajorAxis(rs.getDouble("semi_major_axis"));
                body.setEccentricity(rs.getDouble("eccentricity"));
                bodies.add(body);
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
    
    public static void resetToInitialState() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate(
                "UPDATE celestial_bodies SET " +
                "x = CASE name " +
                "  WHEN 'Sun' THEN 0 " +
                "  WHEN 'Mercury' THEN 5.791e10 " +
                "  WHEN 'Venus' THEN 1.0821e11 " +
                "  WHEN 'Earth' THEN 1.4960e11 " +
                "  WHEN 'Mars' THEN 2.2794e11 " +
                "  WHEN 'Jupiter' THEN 7.7857e11 " +
                "  WHEN 'Saturn' THEN 1.4335e12 " +
                "  WHEN 'Uranus' THEN 2.8725e12 " +
                "  WHEN 'Neptune' THEN 4.4951e12 " +
                "END, " +
                "y = 0, " +
                "vx = 0, " +
                "vy = 0"
            );
        }
    }
}