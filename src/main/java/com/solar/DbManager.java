package com.solar;
import com.solar.model.CelestialBody; 
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DbManager {
    private static final String URL = "jdbc:mysql://localhost:3306/solar_system";
    private static final String USER = "root";
    private static final String PASS = "";

    public static List<CelestialBody> loadInitialState() throws SQLException {
        List<CelestialBody> bodies = new ArrayList<>();
        String sql = "SELECT name, mass, semi_major_axis, eccentricity, radius, color FROM celestial_bodies";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String name = rs.getString("name");
                double mass = rs.getDouble("mass");
                double a = rs.getDouble("semi_major_axis");
                double e = rs.getDouble("eccentricity");
                int radius = rs.getInt("radius");
                String color = rs.getString("color");
                
                // Initialize at perihelion
                double distance = a * (1 - e);
                double velocity = Math.sqrt((6.67430e-11 * 1.989e30) * (2/distance - 1/a));
                
                bodies.add(new CelestialBody(
                    name, mass, 
                    distance, 0, // x, y
                    0, velocity, // vx, vy
                    radius, color
                ));
            }
        }
        return bodies;
    }
}