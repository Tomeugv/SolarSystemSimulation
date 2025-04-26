package com.solar;
import com.solar.model.SimulationState; 
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import com.google.gson.Gson;

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
        // Simulate 1 Earth day per request
        PhysicsEngine.update(state.getBodies(), 60 * 60 * 24);
        
        resp.setContentType("application/json");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.getWriter().write(gson.toJson(state.getBodies()));
    }
}