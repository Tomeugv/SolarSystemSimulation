package com.solar;

import com.google.gson.Gson;
import com.solar.controller.ViewportController;
import com.solar.model.CelestialBody;
import com.solar.model.SimulationState;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.util.*;

/**
 * Servlet principal per a la simulaci� del sistema solar.
 * Gestiona les peticions GET per actualitzar l'estat de la simulaci�
 * i retornar les dades dels cossos celestes en format JSON.
 */
@WebServlet("/api/simulation")
public class SimulationServlet extends HttpServlet {
    private ViewportController viewport;
    private final Gson gson = new Gson();
    
    /**
     * Inicialitza el servlet creant un nou ViewportController.
     */
    @Override
    public void init() throws ServletException {
        try {
            // Inicialitza el viewport amb una mida per defecte (800x600)
            this.viewport = new ViewportController(800, 600);
        } catch (Exception e) {
            throw new ServletException("Error en la inicialitzaci� del ViewportController", e);
        }
    }

    /**
     * Processa les peticions per actualitzar i obtenir l'estat de la simulaci�.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws IOException {

        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setContentType("application/json");
        
        try {
            // Obtenim l'estat de la simulaci�
            SimulationState state = (SimulationState) getServletContext().getAttribute("simulationState");
            if (state == null) {
                throw new IllegalStateException("L'estat de la simulaci� no s'ha inicialitzat.");
            }

            // Processa els canvis en el viewport 
            handleViewportChanges(req);
            
            // Actualitza la simulaci�
            double timeScale = parseTimeScale(req);
            PhysicsEngine.update(state.getBodies(), timeScale * 200);
           
            // Prepara la resposta JSON
            Map<String, Object> response = new HashMap<>();
            response.put("scale", viewport.getCurrentScale());
            response.put("bodies", prepareBodyData(state.getBodies()));
            
            resp.getWriter().write(gson.toJson(response));
            
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    /**
     * Gestiona els canvis en el viewport basats en els par�metres de la petici�.
     */
    private void handleViewportChanges(HttpServletRequest req) {
        // Processa moviment del viewport
        if (req.getParameter("moveX") != null) {
            viewport.move(
                Double.parseDouble(req.getParameter("moveX")),
                Double.parseDouble(req.getParameter("moveY"))
            );
        }
        
        // Processa zoom in/out
        if ("in".equals(req.getParameter("zoom"))) viewport.zoomIn();
        if ("out".equals(req.getParameter("zoom"))) viewport.zoomOut();
        
        // Actualitza l'estat del viewport
        viewport.update();
    }
    
    /**
     * Analitza i valida l'escala de temps de la simulaci�.
     */
    private double parseTimeScale(HttpServletRequest req) {
        String scaleParam = req.getParameter("scale");
        if (scaleParam == null) return 1.0;
        
        double scale = Double.parseDouble(scaleParam);
        return Math.max(0.01, Math.min(scale, 100.0)); // Assegura valors dins del rang perm�s
    }
    
    /**
     * Prepara les dades dels cossos celestes per a la resposta JSON.
     */
    private List<Map<String, Object>> prepareBodyData(List<CelestialBody> bodies) {
        List<Map<String, Object>> bodyData = new ArrayList<>();
        
        for (CelestialBody body : bodies) {
            Map<String, Object> data = new HashMap<>();
            
            // Calcula posici� en pantalla
            Map<String, Double> screenPos = viewport.calculateScreenPosition(
                body.getX(), body.getY()
            );

            data.put("name", body.getName());
            data.put("screenX", screenPos.get("x"));
            data.put("screenY", screenPos.get("y"));
            data.put("worldX", body.getX());
            data.put("worldY", body.getY());
            data.put("radius", body.getRadius());
            data.put("color", body.getColor());

            bodyData.add(data);
        }
        
        return bodyData;
    }
}
