package com.solar.controller;

import java.util.Map;
import java.util.HashMap;

public class ViewportController {
    private double centerX;
    private double centerY;
    private double currentScale = 100;
    private double targetScale = 100;
    private final double AU_TO_PIXELS = 1.496e11;
    
    public ViewportController(int screenWidth, int screenHeight) {
        resetViewport(screenWidth, screenHeight);
    }
    
    public synchronized void resetViewport(int width, int height) {
        this.centerX = width / 2.0;
        this.centerY = height / 2.0;
        this.currentScale = 100;
        this.targetScale = 100;
    }
    
    public synchronized void update() {
        // Smooth zoom animation (ease factor 0.1)
        this.currentScale += (targetScale - currentScale) * 0.1;
    }
    
    public Map<String, Double> calculateScreenPosition(double worldX, double worldY) {
        double screenX = centerX + (worldX / AU_TO_PIXELS * currentScale);
        double screenY = centerY + (worldY / AU_TO_PIXELS * currentScale);
        return Map.of("x", screenX, "y", screenY);
    }
    
    // Movement controls
    public synchronized void move(double dx, double dy) {
        this.centerX += dx;
        this.centerY += dy;
    }
    
    public synchronized void zoomIn() { this.targetScale *= 1.2; }
    public synchronized void zoomOut() { this.targetScale /= 1.2; }
    
    // Getters
    public double getCurrentScale() { return currentScale; }
    public double[] getCenter() { return new double[]{centerX, centerY}; }
}