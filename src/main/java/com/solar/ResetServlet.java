package com.solar;

import com.solar.model.SimulationState;
import com.google.gson.Gson;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

// Definim un Servlet per gestionar el reinici de la simulació
@WebServlet("/api/simulation/reset")
public class ResetServlet extends HttpServlet {

    // Mètode que processa les peticions per reiniciar la simulació
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        // Configuració de CORS per permetre accés des de qualsevol origen
        resp.setHeader("Access-Control-Allow-Origin", "*");  //
        resp.setHeader("Access-Control-Allow-Methods", "POST");
        resp.setContentType("application/json");  // La resposta serà en format JSON
        
        try {
            // Obté l'estat actual de la simulació
            SimulationState state = (SimulationState) getServletContext()
                .getAttribute("simulationState");
            if (state == null) {
                throw new Exception("Simulation state not initialized");
            }           
            // Executa el reinici de la simulació
            state.reset();           
            // Retorna la llista de cossos celestes en format JSON després del reset
            resp.getWriter().write(new Gson().toJson(state.getBodies()));
            
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }
}
