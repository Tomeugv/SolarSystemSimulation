package com.solar;

import com.solar.model.CelestialBody;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe per gestionar la connexió i operacions amb la base de dades del sistema solar.
 * Proporciona mètodes per carregar i desar l'estat dels cossos celestes.
 */
public class DbManager {
    // Configuració de connexió a la base de dades
    private static final String DB_URL = "jdbc:mysql://localhost:3306/solar_system";
    private static final String DB_USER = "your_username";
    private static final String DB_PASSWORD = "your_password";

    // Bloc d'inicialització estàtic per carregar el controlador JDBC
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No s'ha trobat el controlador JDBC de MySQL", e);
        }
    }

    /**
     * Carrega tots els cossos celestes de la base de dades.
     * @return Llista de cossos celestes amb les seves propietats
     * @throws SQLException Si hi ha un error en accedir a la base de dades
     */
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

    /**
     * Carrega només els cossos celestes especificats.
     * @param names Llista de noms dels cossos a carregar
     * @return Llista de cossos celestes sol·licitats
     * @throws SQLException Si hi ha un error en accedir a la base de dades
     */
    public static List<CelestialBody> loadSelectedBodies(List<String> names) throws SQLException {
        List<CelestialBody> bodies = new ArrayList<>();
        
        if (names.isEmpty()) return bodies;

        // Crea els placeholders per a la consulta preparada
        String placeholders = String.join(",", names.stream().map(x -> "?").toArray(String[]::new));

        String query = "SELECT * FROM celestial_bodies WHERE name IN (" + placeholders + ")";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            // Estableix els paràmetres de la consulta
            for (int i = 0; i < names.size(); i++) {
                ps.setString(i + 1, names.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
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
        }
        return bodies;
    }

    /**
     * Desa l'estat actual dels cossos celestes a la base de dades.
     * @param bodies Llista de cossos celestes a desar
     * @throws SQLException Si hi ha un error en accedir a la base de dades
     */
    public static void saveState(List<CelestialBody> bodies) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(
                 "UPDATE celestial_bodies SET x=?, y=?, vx=?, vy=? WHERE name=?")) {

            // Afegeix cada cos celeste com a part d'un batch
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
}