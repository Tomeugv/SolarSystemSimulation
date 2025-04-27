package com.solar.controller;

import java.util.Map;
import java.util.HashMap;

/**
 * Controlador de la vista (viewport) per a la visualitzaci� d'objectes astron�mics.
 * Gestiona la posici� central, el zoom i les conversions entre coordenades del m�n i pantalla.
 */
public class ViewportController {
    private double centerX;          // Coordenada X del centre de la vista (p�xels)
    private double centerY;          // Coordenada Y del centre de la vista (p�xels)
    private double currentScale = 100;  // Escala actual de zoom (%)
    private double targetScale = 100;   // Escala objectiu de zoom (per animacions suaus)
    private final double AU_TO_PIXELS = 1.496e11;  // Conversi� d'Unitats Astron�miques (UA) a p�xels

    /**
     * Constructor que inicialitza el viewport amb les dimensions de la pantalla.
     * @param screenWidth Amplada de la pantalla en p�xels.
     * @param screenHeight Al�ada de la pantalla en p�xels.
     */
    public ViewportController(int screenWidth, int screenHeight) {
        resetViewport(screenWidth, screenHeight);
    }
    
    /**
     * Reinicia el viewport als valors per defecte.
     * @param width Amplada actual de la pantalla.
     * @param height Al�ada actual de la pantalla.
     */
    public synchronized void resetViewport(int width, int height) {
        this.centerX = width / 2.0;
        this.centerY = height / 2.0;
        this.currentScale = 100;
        this.targetScale = 100;
    }
    
    /**
     * Actualitza l'estat del viewport (per animacions de zoom suaus).
     * Aplica un factor d'alleujament (easing) del 10% per cada actualitzaci�.
     */
    public synchronized void update() {
        // Animaci� de zoom suau (factor d'alleujament 0.1)
        this.currentScale += (targetScale - currentScale) * 0.1;
    }
    
    /**
     * Converteix coordenades del m�n real (en UA) a coordenades de pantalla.
     * @param worldX Posici� X en Unitats Astron�miques (UA).
     * @param worldY Posici� Y en Unitats Astron�miques (UA).
     * @return Mapa amb les coordenades de pantalla ("x" i "y").
     */
    public Map<String, Double> calculateScreenPosition(double worldX, double worldY) {
        double screenX = centerX + (worldX / AU_TO_PIXELS * currentScale);
        double screenY = centerY + (worldY / AU_TO_PIXELS * currentScale);
        return Map.of("x", screenX, "y", screenY);
    }
    
    // --- Controles de moviment ---
    
    /**
     * Mou el viewport en la direcci� especificada.
     * @param dx Despla�ament horitzontal (p�xels).
     * @param dy Despla�ament vertical (p�xels).
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