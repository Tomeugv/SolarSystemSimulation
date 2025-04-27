package com.solar;

import com.solar.model.SimulationState;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;


@WebListener  // Indica que aquesta classe és un listener del servlet container
public class SimulationContextListener implements ServletContextListener {

    // Mètode que s'executa quan s'inicialitza el context de l'aplicació 
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            // 1. Creem una nova instància de la simulació
            SimulationState simulationState = new SimulationState();           
            // 2. Guardem l'estat al context de l'aplicació
            sce.getServletContext().setAttribute("simulationState", simulationState);
            System.out.println(" Simulation State initialized successfully ");          
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize SimulationState", e);
        }
    }

}