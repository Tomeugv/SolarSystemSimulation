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

@WebServlet("/api/simulation")
public class SimulationServlet extends HttpServlet {
    private ViewportController viewport;
    private final Gson gson = new Gson();
    
    @Override
    public void init() throws ServletException {
        try {
            this.viewport = new ViewportController(800, 600); // Default size
        } catch (Exception e) {
            throw new ServletException("Initialization failed", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws IOException {
        // Enable CORS
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setContentType("application/json");
        
        try {
            SimulationState state = (SimulationState) getServletContext().getAttribute("simulationState");
            if (state == null) {
                throw new IllegalStateException("Simulation state not initialized.");
            }

            handleViewportChanges(req);
            
            double timeScale = parseTimeScale(req);
            PhysicsEngine.update(state.getBodies(), timeScale * 200);
           
            Map<String, Object> response = new HashMap<>();
            response.put("scale", viewport.getCurrentScale());
            response.put("bodies", prepareBodyData(state.getBodies()));
            
            resp.getWriter().write(gson.toJson(response));
            
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    private void handleViewportChanges(HttpServletRequest req) {
        if (req.getParameter("moveX") != null) {
            viewport.move(
                Double.parseDouble(req.getParameter("moveX")),
                Double.parseDouble(req.getParameter("moveY"))
            );
        }
        if ("in".equals(req.getParameter("zoom"))) viewport.zoomIn();
        if ("out".equals(req.getParameter("zoom"))) viewport.zoomOut();
        viewport.update();
    }
    
    private double parseTimeScale(HttpServletRequest req) {
        String scaleParam = req.getParameter("scale");
        if (scaleParam == null) return 1.0;
        
        double scale = Double.parseDouble(scaleParam);
        return Math.max(0.01, Math.min(scale, 100.0));
    }
    
    private List<Map<String, Object>> prepareBodyData(List<CelestialBody> bodies) {
        List<Map<String, Object>> bodyData = new ArrayList<>();
        for (CelestialBody body : bodies) {
            Map<String, Object> data = new HashMap<>();
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

