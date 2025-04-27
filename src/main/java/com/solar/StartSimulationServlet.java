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

@WebServlet("/api/simulation/start")
public class StartSimulationServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "POST");
        resp.setContentType("application/json");

        try {
            // Read the entire body into a string
            StringBuilder jsonBuilder = new StringBuilder();
            BufferedReader reader = req.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            String json = jsonBuilder.toString();

            // Parse it properly
            Type listType = new TypeToken<List<String>>() {}.getType();
            List<String> selectedPlanets = gson.fromJson(json, listType);

            if (selectedPlanets == null || selectedPlanets.isEmpty()) {
                throw new Exception("No planets selected");
            }

            // Create the simulation state FULLY
            SimulationState newState = new SimulationState(selectedPlanets);
            newState.reset(); // MAKE SURE we reset before setting it to context!

            // Save it into ServletContext
            getServletContext().setAttribute("simulationState", newState);

            // Now we can safely reply success
            resp.getWriter().write("{\"status\":\"success\"}");

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }
}

