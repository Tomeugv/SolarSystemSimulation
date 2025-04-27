package com.solar;

import com.solar.model.SimulationState;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class SimulationContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            SimulationState simulationState = new SimulationState();
            sce.getServletContext().setAttribute("simulationState", simulationState);
            System.out.println(" Simulation State initialized successfully ");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize SimulationState", e);
        }
    }

}
