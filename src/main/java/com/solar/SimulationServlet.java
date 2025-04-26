package com.solar;

import com.solar.model.SimulationState;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import com.google.gson.Gson;
import java.sql.SQLException;

@WebServlet("/api/simulation")
public class SimulationServlet extends HttpServlet {
    private SimulationState state;
    private final Gson gson = new Gson();
    
    @Override
    public void init() throws ServletException {
        try {
            this.state = new SimulationState();
            System.out.println("Simulation state initialized successfully");
        } catch (SQLException e) {
            throw new ServletException("Database initialization failed", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        try {
            // Handle time scale parameter
            double timeScale = 1.0;
            String scaleParam = req.getParameter("scale");
            
            if (scaleParam != null && !scaleParam.isEmpty()) {
                try {
                    timeScale = Double.parseDouble(scaleParam);
                    timeScale = Math.max(0.01, Math.min(timeScale, 100.0));
                    System.out.println("Using time scale: " + timeScale);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid time scale parameter: " + scaleParam);
                }
            }
            
            // Update physics
            PhysicsEngine.update(state.getBodies(), timeScale);
            
            // Save state periodically (1% chance)
            if (Math.random() < 0.01) {
                try {
                    state.saveState();
                    System.out.println("Auto-saved simulation state");
                } catch (SQLException e) {
                    System.err.println("Failed to auto-save state: " + e.getMessage());
                }
            }
            
            // Prepare response
            resp.setContentType("application/json");
            resp.setHeader("Access-Control-Allow-Origin", "*");
            resp.getWriter().write(gson.toJson(state.getBodies()));
            
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }
}