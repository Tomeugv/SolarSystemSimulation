package com.solar;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.solar.model.CelestialBody;
import com.solar.model.SimulationState;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Servlet per a l'administració de planetes del sistema solar.
 * Gestiona operacions CRUD sobre els cossos celestes i permet reiniciar la base de dades.
 */
@WebServlet(name = "PlanetAdminServlet", urlPatterns = {"/api/planetadmin", "/api/planetadmin/reset"})
public class PlanetAdminServlet extends HttpServlet {
    private final Gson gson = new Gson();
    private static final String DB_URL = "jdbc:mysql://localhost:3306/solar_system";
    private static final String DB_USER = "your_username";   // <-- Reemplaçar si cal
    private static final String DB_PASSWORD = "your_password"; // <-- Reemplaçar si cal

    /**
     * Processa peticions GET per obtenir la llista de planetes.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT name, mass, semi_major_axis, eccentricity, radius, color FROM celestial_bodies")) {

            List<CelestialBody> planets = new ArrayList<>();
            while (rs.next()) {
                CelestialBody body = new CelestialBody(
                    rs.getString("name"),
                    rs.getDouble("mass"),
                    rs.getDouble("semi_major_axis"),
                    0, 0, 0,  // Posició i velocitat inicials a 0
                    rs.getInt("radius"),
                    rs.getString("color")
                );
                body.setSemiMajorAxis(rs.getDouble("semi_major_axis"));
                body.setEccentricity(rs.getDouble("eccentricity"));
                planets.add(body);
            }

            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(planets));

        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Processa peticions POST per afegir un nou planeta o reiniciar la base de dades.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Gestiona la petició de reinici
        if (req.getRequestURI().endsWith("/reset")) {
            resetDatabase(resp);
            return;
        }

        // Processa la creació d'un nou planeta
        try {
            JsonObject json = JsonParser.parseReader(req.getReader()).getAsJsonObject();
            
            CelestialBody newPlanet = parsePlanetFromJson(json);
            insertPlanetIntoDatabase(newPlanet);
            
            resp.setStatus(HttpServletResponse.SC_CREATED);
            updateSimulationState();

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Processa peticions DELETE per eliminar un planeta.
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String name = req.getParameter("name");
        if (name == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(
                 "DELETE FROM celestial_bodies WHERE name = ?")) {

            ps.setString(1, name);
            ps.executeUpdate();
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            updateSimulationState();

        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Reinicia la base de dades als valors per defecte del sistema solar.
     */
    private void resetDatabase(HttpServletResponse resp) throws IOException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {

            // Esborra tots els registres existents
            stmt.executeUpdate("DELETE FROM celestial_bodies");

            // Inserta els planetes del sistema solar per defecte
            stmt.executeUpdate(
                "INSERT INTO celestial_bodies (name, mass, semi_major_axis, eccentricity, radius, color, x, y, vx, vy) VALUES " +
                "('Sun', 1.989e30, 0, 0, 20, '#FFFF00', 0, 0, 0, 0)," +
                "('Mercury', 3.3011e23, 5.791e10, 0.2056, 5, '#A9A9A9', 5.791e10 * (1-0.2056), 0, 0, 0)," +
                "('Venus', 4.8675e24, 1.0821e11, 0.0067, 8, '#FFA500', 1.0821e11 * (1-0.0067), 0, 0, 0)," +
                "('Earth', 5.972e24, 1.496e11, 0.0167, 8, '#1E90FF', 1.496e11 * (1-0.0167), 0, 0, 0)," +
                "('Mars', 6.417e23, 2.2794e11, 0.0935, 6, '#FF4500', 2.2794e11 * (1-0.0935), 0, 0, 0)," +
                "('Jupiter', 1.899e27, 7.7857e11, 0.0489, 15, '#DAA520', 7.7857e11 * (1-0.0489), 0, 0, 0)," +
                "('Saturn', 5.685e26, 1.4335e12, 0.0565, 12, '#F0E68C', 1.4335e12 * (1-0.0565), 0, 0, 0)," +
                "('Uranus', 8.682e25, 2.8725e12, 0.0457, 10, '#AFEEEE', 2.8725e12 * (1-0.0457), 0, 0, 0)," +
                "('Neptune', 1.024e26, 4.4951e12, 0.0113, 10, '#0000CD', 4.4951e12 * (1-0.0113), 0, 0, 0)");

            resp.setStatus(HttpServletResponse.SC_OK);
            updateSimulationState();

        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // ================================================
    // Mètodes privats d'ajuda
    // ================================================

    /**
     * Analitza un objecte JSON per crear un nou planeta.
     */
    private CelestialBody parsePlanetFromJson(JsonObject json) {
        String name = json.get("name").getAsString();
        double mass = json.get("mass").getAsDouble();
        double semiMajorAxis = json.get("semiMajorAxis").getAsDouble();
        double eccentricity = json.get("eccentricity").getAsDouble();
        int radius = json.get("radius").getAsInt();
        String color = json.get("color").getAsString();

        return new CelestialBody(
            name, mass, semiMajorAxis * (1 - eccentricity), 
            0, 0, 0, radius, color
        );
    }

    /**
     * Insereix un nou planeta a la base de dades.
     */
    private void insertPlanetIntoDatabase(CelestialBody planet) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(
                 "INSERT INTO celestial_bodies (name, mass, semi_major_axis, eccentricity, radius, color, x, y, vx, vy) " +
                 "VALUES (?, ?, ?, ?, ?, ?, ?, 0, 0, 0)")) {

            ps.setString(1, planet.getName());
            ps.setDouble(2, planet.getMass());
            ps.setDouble(3, planet.getSemiMajorAxis());
            ps.setDouble(4, planet.getEccentricity());
            ps.setInt(5, planet.getRadius());
            ps.setString(6, planet.getColor());
            ps.setDouble(7, planet.getX()); // Posició inicial al periheli
            ps.executeUpdate();
        }
    }

    /**
     * Actualitza l'estat de la simulació després de canvis a la base de dades.
     */
    private void updateSimulationState() {
        SimulationState state = (SimulationState) getServletContext().getAttribute("simulationState");
        if (state != null) {
            try {
                state.reload();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

