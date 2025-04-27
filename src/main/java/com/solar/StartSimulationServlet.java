package com.solar;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.solar.model.SimulationState;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Servlet per iniciar una nova simulació amb els planetes seleccionats.
 * Aquest endpoint rep una llista de noms de planetes i inicialitza l'estat de la simulació.
 */
@WebServlet("/api/simulation/start")
public class StartSimulationServlet extends HttpServlet {
    private final Gson gson = new Gson();

    /**
     * Processa les peticions POST per iniciar una nova simulació.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "POST");
        resp.setContentType("application/json");

        try {
            // 1. Llegeix el cos de la petició
            String json = readRequestBody(req);
            
            // 2. Analitza la llista de planetes seleccionats
            List<String> selectedPlanets = parseSelectedPlanets(json);
            
            // 3. Valida que s'hagin seleccionat planetes
            validatePlanetSelection(selectedPlanets);
            
            // 4. Crea i inicialitza l'estat de la simulació
            SimulationState newState = initializeSimulationState(selectedPlanets);
            
            // 5. Desa l'estat al context del servlet
            getServletContext().setAttribute("simulationState", newState);
            
            // 6. Respon amb èxit
            sendSuccessResponse(resp);

        } catch (Exception e) {
            handleError(resp, e);
        }
    }

 
    private String readRequestBody(HttpServletRequest req) throws IOException {
        StringBuilder jsonBuilder = new StringBuilder();
        BufferedReader reader = req.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line);
        }
        return jsonBuilder.toString();
    }

    /**
     * Analitza el JSON rebut a una llista de noms de planetes.
     */
    private List<String> parseSelectedPlanets(String json) {
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(json, listType);
    }

    /**
     * Valida que s'hagi seleccionat almenys un planeta.
     */
    private void validatePlanetSelection(List<String> selectedPlanets) throws Exception {
        if (selectedPlanets == null || selectedPlanets.isEmpty()) {
            throw new Exception("No s'ha seleccionat cap planeta");
        }
    }

    /**
     * Inicialitza l'estat de la simulació amb els planetes seleccionats.
     */
    private SimulationState initializeSimulationState(List<String> selectedPlanets) 
            throws Exception {
        SimulationState newState = new SimulationState(selectedPlanets);
        newState.reset(); // Assegura que comença en estat inicial
        return newState;
    }

    /**
     * Envia resposta d'èxit al client.
     */
    private void sendSuccessResponse(HttpServletResponse resp) throws IOException {
        resp.getWriter().write("{\"status\":\"success\"}");
    }

    /**
     * Gestiona errors i envia resposta d'error al client.
     */
    private void handleError(HttpServletResponse resp, Exception e) throws IOException {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        e.printStackTrace();
    }
}
