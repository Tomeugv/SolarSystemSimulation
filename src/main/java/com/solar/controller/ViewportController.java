package com.solar.controller;

import java.util.Map;
import java.util.HashMap;

/**
 * Controlador de la vista (viewport) per a la visualització d'objectes astronòmics.
 * Gestiona la posició central, el zoom i les conversions entre coordenades del món i pantalla.
 */
public class ViewportController {
    private double centerX;          // Coordenada X del centre de la vista (píxels)
    private double centerY;          // Coordenada Y del centre de la vista (píxels)
    private double currentScale = 100;  // Escala actual de zoom (%)
    private double targetScale = 100;   // Escala objectiu de zoom (per animacions suaus)
    private final double AU_TO_PIXELS = 1.496e11;  // Conversió d'Unitats Astronòmiques (UA) a píxels

    /**
     * Constructor que inicialitza el viewport amb les dimensions de la pantalla.
     * @param screenWidth Amplada de la pantalla en píxels.
     * @param screenHeight Alçada de la pantalla en píxels.
     */
    public ViewportController(int screenWidth, int screenHeight) {
        resetViewport(screenWidth, screenHeight);
    }
    
    /**
     * Reinicia el viewport als valors per defecte.
     * @param width Amplada actual de la pantalla.
     * @param height Alçada actual de la pantalla.
     */
    public synchronized void resetViewport(int width, int height) {
        this.centerX = width / 2.0;
        this.centerY = height / 2.0;
        this.currentScale = 100;
        this.targetScale = 100;
    }
    
    /**
     * Actualitza l'estat del viewport (per animacions de zoom suaus).
     * Aplica un factor d'alleujament (easing) del 10% per cada actualització.
     */
    public synchronized void update() {
        // Animació de zoom suau (factor d'alleujament 0.1)
        this.currentScale += (targetScale - currentScale) * 0.1;
    }
    
    /**
     * Converteix coordenades del món real (en UA) a coordenades de pantalla.
     * @param worldX Posició X en Unitats Astronòmiques (UA).
     * @param worldY Posició Y en Unitats Astronòmiques (UA).
     * @return Mapa amb les coordenades de pantalla ("x" i "y").
     */
    public Map<String, Double> calculateScreenPosition(double worldX, double worldY) {
        double screenX = centerX + (worldX / AU_TO_PIXELS * currentScale);
        double screenY = centerY + (worldY / AU_TO_PIXELS * currentScale);
        return Map.of("x", screenX, "y", screenY);
    }
    
    // --- Controles de moviment ---
    
    /**
     * Mou el viewport en la direcció especificada.
     * @param dx Desplaçament horitzontal (píxels).
     * @param dy Desplaçament vertical (píxels).
     */
    public synchronized void move(double dx, double dy) {
        this.centerX += dx;
        this.centerY += dy;
    }
    
    /** Augmenta el zoom (un 20% per cada crida). */
    public synchronized void zoomIn() { this.targetScale *= 1.2; }
    
    /** Disminueix el zoom (un 20% per cada crida). */
    public synchronized void zoomOut() { this.targetScale /= 1.2; }
    
    // --- Getters ---
    
    /** Retorna l'escala actual de zoom. */
    public double getCurrentScale() { return currentScale; }
    
    /** Retorna les coordenades del centre de la vista [x, y]. */
    public double[] getCenter() { return new double[]{centerX, centerY}; }
}