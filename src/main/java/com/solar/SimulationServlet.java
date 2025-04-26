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
        } catch (SQLException e) {
            throw new ServletException("Database initialization failed", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        try {
            // Get time scale parameter (default to 1 hour)
            double timeScale = 1.0;
            String scaleParam = req.getParameter("scale");
            if (scaleParam != null) {
                timeScale = Double.parseDouble(scaleParam);
            }
            
            // Simulate with smaller time steps (1 hour by default)
            PhysicsEngine.update(state.getBodies(), 60 * 60 * timeScale);
            
            // Save state periodically
            if (Math.random() < 0.01) { // 1% chance to save
                DbManager.saveState(state.getBodies());
            }
            
            resp.setContentType("application/json");
            resp.setHeader("Access-Control-Allow-Origin", "*");
            resp.getWriter().write(gson.toJson(state.getBodies()));
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                         "Simulation error: " + e.getMessage());
        }
    }
}