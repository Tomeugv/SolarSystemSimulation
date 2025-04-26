package com.solar;

import com.solar.model.SimulationState;
import com.google.gson.Gson;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/api/simulation")
public class SimulationServlet extends HttpServlet {
    private SimulationState state;
    private final Gson gson = new Gson();
    
    @Override
    public void init() throws ServletException {
        try {
            // Initialize or get existing state from context
            ServletContext context = getServletContext();
            this.state = (SimulationState) context.getAttribute("simulationState");
            
            if (this.state == null) {
                this.state = new SimulationState();
                context.setAttribute("simulationState", this.state);
            }
        } catch (SQLException e) {
            throw new ServletException("Database initialization failed", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        // Enable CORS
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET");
        resp.setContentType("application/json");
        
        try {
            // Handle time scale parameter
            double timeScale = 1.0;
            String scaleParam = req.getParameter("scale");
            
            if (scaleParam != null && !scaleParam.isEmpty()) {
                try {
                    timeScale = Double.parseDouble(scaleParam);
                    timeScale = Math.max(0.01, Math.min(timeScale, 100.0));
                } catch (NumberFormatException e) {
                    System.err.println("Invalid time scale parameter: " + scaleParam);
                }
            }

            // Update physics
            PhysicsEngine.update(state.getBodies(), timeScale);
            
            // Prepare response
            resp.getWriter().write(gson.toJson(state.getBodies()));
            
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        // Enable CORS
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "POST");
        resp.setContentType("application/json");
        
        try {
            String pathInfo = req.getPathInfo();
            
            if (pathInfo != null && pathInfo.equals("/reset")) {
                // Handle reset request
                state.reset();
                resp.getWriter().write(gson.toJson(state.getBodies()));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Invalid endpoint\"}");
            }
            
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }
}