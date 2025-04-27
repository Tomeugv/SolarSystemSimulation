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

//Servlet per iniciar una nova simulació amb els planetes seleccionats
 
@WebServlet("/api/simulation/start")
public class StartSimulationServlet extends HttpServlet {
    private final Gson gson = new Gson();  //Gson per a la conversió JSON

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Configuració de CORS i tipus de contingut
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "POST");
        resp.setContentType("application/json");  // La resposta serà en format JSON

        try {
            // 1. Llegeix el cos de la petició (JSON amb planetes seleccionats)
            StringBuilder jsonBuilder = new StringBuilder();
            BufferedReader reader = req.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            String json = jsonBuilder.toString();

            // 2. Converteix el JSON a una llista de noms de planetes
            Type listType = new TypeToken<List<String>>() {}.getType();
            List<String> selectedPlanets = gson.fromJson(json, listType);  // Converteix de JSON a List<String>
            
            if (selectedPlanets == null || selectedPlanets.isEmpty()) {
                throw new Exception("No planets selected");
            }

            // 4. Crea un nou estat de simulació amb els planetes seleccionats
            SimulationState newState = new SimulationState(selectedPlanets);
            newState.reset();  // Reinicia l'estat per assegurar valors inicials correctes

            // 5. Guarda el nou
            getServletContext().setAttribute("simulationState", newState);

            resp.getWriter().write("{\"status\":\"success\"}");

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);  // Codi 400 per errors de client
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
            e.printStackTrace(); 
        }
    }
}
