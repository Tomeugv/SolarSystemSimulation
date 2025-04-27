package com.solar;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.solar.model.CelestialBody;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "PlanetAdminServlet", urlPatterns = {"/api/planetadmin", "/api/planetadmin/reset"})
public class PlanetAdminServlet extends HttpServlet {
    private final Gson gson = new Gson();
    private static final String DB_URL = "jdbc:mysql://localhost:3306/solar_system";
    private static final String DB_USER = "your_username";
    private static final String DB_PASSWORD = "your_password";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name, mass, semi_major_axis, radius, color FROM celestial_bodies")) {

            List<CelestialBody> planets = new ArrayList<>();
            while (rs.next()) {
                planets.add(new CelestialBody(
                        rs.getString("name"),
                        rs.getDouble("mass"),
                        rs.getDouble("semi_major_axis"),
                        0, 0, 0, rs.getInt("radius"),
                        rs.getString("color")
                ));
            }
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(planets));

        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(500);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (req.getRequestURI().endsWith("/reset")) {
            resetDatabase(resp);
            return;
        }

        BufferedReader reader = req.getReader();
        JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

        String name = json.get("name").getAsString();
        double mass = json.get("mass").getAsDouble();
        double semiMajorAxis = json.get("semiMajorAxis").getAsDouble();
        double eccentricity = json.get("eccentricity").getAsDouble();
        int radius = json.get("radius").getAsInt();
        String color = json.get("color").getAsString();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO celestial_bodies (name, mass, semi_major_axis, eccentricity, radius, color, x, y, vx, vy) VALUES (?, ?, ?, ?, ?, ?, 0, 0, 0, 0)")) {

            ps.setString(1, name);
            ps.setDouble(2, mass);
            ps.setDouble(3, semiMajorAxis);
            ps.setDouble(4, eccentricity);
            ps.setInt(5, radius);
            ps.setString(6, color);

            ps.executeUpdate();
            resp.setStatus(201);

        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(500);
        }

    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String name = req.getParameter("name");
        if (name == null) {
            resp.setStatus(400);
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement("DELETE FROM celestial_bodies WHERE name = ?")) {

            ps.setString(1, name);
            ps.executeUpdate();
            resp.setStatus(204);

        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(500);
        }
    }

    private void resetDatabase(HttpServletResponse resp) throws IOException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {

            // 1. Delete all current planets
            stmt.executeUpdate("DELETE FROM celestial_bodies");

            // 2. Reinsert default planets
            stmt.executeUpdate("INSERT INTO celestial_bodies (name, mass, semi_major_axis, eccentricity, radius, color, x, y, vx, vy) VALUES" +
                    "('Sun', 1.989e30, 0, 0, 20, '#FFFF00', 0, 0, 0, 0)," +
                    "('Mercury', 3.3011e23, 5.791e10, 0.2056, 5, '#A9A9A9', 5.791e10, 0, 0, 0)," +
                    "('Venus', 4.8675e24, 1.0821e11, 0.0067, 8, '#FFA500', 1.0821e11, 0, 0, 0)," +
                    "('Earth', 5.972e24, 1.496e11, 0.0167, 8, '#1E90FF', 1.496e11, 0, 0, 0)," +
                    "('Mars', 6.417e23, 2.2794e11, 0.0935, 6, '#FF4500', 2.2794e11, 0, 0, 0)," +
                    "('Jupiter', 1.899e27, 7.7857e11, 0.0489, 15, '#DAA520', 7.7857e11, 0, 0, 0)," +
                    "('Saturn', 5.685e26, 1.4335e12, 0.0565, 12, '#F0E68C', 1.4335e12, 0, 0, 0)," +
                    "('Uranus', 8.682e25, 2.8725e12, 0.0457, 10, '#AFEEEE', 2.8725e12, 0, 0, 0)," +
                    "('Neptune', 1.024e26, 4.4951e12, 0.0113, 10, '#0000CD', 4.4951e12, 0, 0, 0)");

            resp.setStatus(200);

        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(500);
        }
    }

}
