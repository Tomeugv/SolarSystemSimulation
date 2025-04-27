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

@WebServlet("/api/simulation")  // Defineix la ruta d'accés al servlet
public class SimulationServlet extends HttpServlet {
    private ViewportController viewport; 
    private final Gson gson = new Gson();  // Convertidor JSON

    @Override
    public void init() throws ServletException {
        // Inicialitza el viewport
        this.viewport = new ViewportController(800, 600);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Permet accés des de qualsevol origen
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setContentType("application/json");

        try {
            // Obté l'estat actual de la simulació
            SimulationState state = (SimulationState) getServletContext().getAttribute("simulationState");
            if (state == null) throw new IllegalStateException("Simulation state not initialized.");
            // Processa canvis en la vista
            handleViewportChanges(req);
            // Calcula l'escala de temps per a la simulació
            double timeScale = parseTimeScale(req);
            // Actualitza les posicions dels cossos
            PhysicsEngine.update(state.getBodies(), timeScale * 200);

            // Prepara la resposta JSON
            Map<String, Object> response = new HashMap<>();
            response.put("scale", viewport.getCurrentScale());  // Escala actual
            response.put("bodies", prepareBodyData(state.getBodies()));  // Dades dels planetes
            // Envia la resposta
            resp.getWriter().write(gson.toJson(response));

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // Gestiona els canvis de vista
    private void handleViewportChanges(HttpServletRequest req) {
        if (req.getParameter("moveX") != null) {
            viewport.move(
                Double.parseDouble(req.getParameter("moveX")),
                Double.parseDouble(req.getParameter("moveY"))
            );
        }
        // Zoom
        if ("in".equals(req.getParameter("zoom"))) viewport.zoomIn();
        if ("out".equals(req.getParameter("zoom"))) viewport.zoomOut();
        viewport.update();
    }

    // Processa l'escala de temps de la simulació
    private double parseTimeScale(HttpServletRequest req) {
        String scaleParam = req.getParameter("scale");
        if (scaleParam == null) return 1.0;  // Valor per defecte
        double scale = Double.parseDouble(scaleParam);
        // Limitam el maxim i el minim de l'escala
        return Math.max(0.01, Math.min(scale, 100.0));
    }

    // Prepara les dades dels cossos celestes per a la visualització
    private List<Map<String, Object>> prepareBodyData(List<CelestialBody> bodies) {
        List<Map<String, Object>> bodyData = new ArrayList<>();
        for (CelestialBody body : bodies) {
            Map<String, Object> data = new HashMap<>();
            Map<String, Double> screenPos = viewport.calculateScreenPosition(body.getX(), body.getY());

            // Afegeix totes les propietats del cos celeste
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