package com.solar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

import com.solar.model.CelestialBody;
import com.solar.model.SimulationState;
import com.google.gson.Gson;

public class SimulationServlet extends HttpServlet {
    private static final double G = 6.67430e-11;
    private static final double AU = 1.496e11;
    private SimulationState simulationState;
    private Gson gson = new Gson();

    @Override
    public void init() throws ServletException {
        simulationState = new SimulationState();
        initializeSolarSystem();
    }

    private void initializeSolarSystem() {
        // Sun
        simulationState.addBody(new CelestialBody("Sun", 1.989e30, 0, 0, 0, 0, 20, "#FFFF00"));
        
        // Planets
        simulationState.addBody(new CelestialBody("Mercury", 3.3011e23, 0.387*AU, 0, 0, 47.87e3, 5, "#A9A9A9"));
        simulationState.addBody(new CelestialBody("Venus", 4.8675e24, 0.723*AU, 0, 0, 35.02e3, 8, "#FFA500"));
        simulationState.addBody(new CelestialBody("Earth", 5.972e24, 1.0*AU, 0, 0, 29.78e3, 8, "#1E90FF"));
        simulationState.addBody(new CelestialBody("Mars", 6.417e23, 1.524*AU, 0, 0, 24.07e3, 6, "#FF4500"));
        simulationState.addBody(new CelestialBody("Jupiter", 1.899e27, 5.203*AU, 0, 0, 13.07e3, 15, "#DAA520"));
        simulationState.addBody(new CelestialBody("Saturn", 5.685e26, 9.537*AU, 0, 0, 9.69e3, 12, "#F0E68C"));
        simulationState.addBody(new CelestialBody("Uranus", 8.682e25, 19.191*AU, 0, 0, 6.81e3, 10, "#AFEEEE"));
        simulationState.addBody(new CelestialBody("Neptune", 1.024e26, 30.069*AU, 0, 0, 5.43e3, 10, "#0000CD"));
        
        // Comet
        simulationState.addBody(new CelestialBody("Halley's Comet", 2.2e14, 17.8*AU, 0, 0, 5.4e3, 5, "#FFFFFF"));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.getWriter().write(gson.toJson(simulationState));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Calculate next simulation step (12 hours per frame)
        simulationState.update(60 * 60 * 12);
        
        // Save to database
        try {
            Bbdd.saveState(simulationState);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
            return;
        }
        
        // Return updated state
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.getWriter().write(gson.toJson(simulationState));
    }
}