package com.solar;

import com.solar.model.SimulationState;
import com.google.gson.Gson;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

@WebServlet("/api/simulation/reset")
public class ResetServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        // Enable CORS
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "POST");
        resp.setContentType("application/json");
        
        try {
            SimulationState state = (SimulationState) getServletContext()
                .getAttribute("simulationState");
            
            if (state == null) {
                throw new Exception("Simulation state not initialized");
            }
            
            state.reset();
            resp.getWriter().write(new Gson().toJson(state.getBodies()));
            
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }
}
