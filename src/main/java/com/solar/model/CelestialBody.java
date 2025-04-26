package com.solar.model;

import java.util.ArrayList;
import java.util.List;
import com.solar.model.Point2D;

public class CelestialBody {
    private String name;
    private double mass;
    private double x, y;      // Position
    private double vx, vy;    // Velocity
    private int radius;
    private String color;
    private List<Point2D> trail;  // For orbit trails (optional)

    // Constructor (MUST MATCH HOW YOU CALL IT IN SimulationServlet.java)
    public CelestialBody(String name, double mass, double x, double y, 
                        double vx, double vy, int radius, String color) {
        this.name = name;
        this.mass = mass;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.radius = radius;
        this.color = color;
        this.trail = new ArrayList<>();  // Initialize trail
    }
    // Velocity setters (required by SimulationState.update())
    public void setVx(double vx) {
        this.vx = vx;
    }

    public void setVy(double vy) {
        this.vy = vy;
    }

    // Position setters (optional but recommended)
    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    // Getters (required for JSON serialization)
    public String getName() { return name; }
    public double getMass() { return mass; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getVx() { return vx; }
    public double getVy() { return vy; }
    public int getRadius() { return radius; }
    public String getColor() { return color; }
}