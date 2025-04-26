package com.solar.model;

public class CelestialBody {
    private final String name;
    private final double mass;
    private double x, y;
    private double vx, vy;
    private double prevX, prevY;
    private final int radius;
    private final String color;
    private double semiMajorAxis;
    private double eccentricity;

    public CelestialBody(String name, double mass, double x, double y, 
                        double vx, double vy, int radius, String color) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        this.name = name.trim();
        this.mass = mass;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.radius = radius;
        this.color = color;
        this.prevX = x - vx * 3600; // Initialize previous position
        this.prevY = y - vy * 3600;
    }

    // Getters
    public String getName() { return name; }
    public double getMass() { return mass; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getVx() { return vx; }
    public double getVy() { return vy; }
    public double getPrevX() { return prevX; }
    public double getPrevY() { return prevY; }
    public int getRadius() { return radius; }
    public String getColor() { return color; }
    public double getSemiMajorAxis() { return semiMajorAxis; }
    public double getEccentricity() { return eccentricity; }

    // Setters
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setVx(double vx) { this.vx = vx; }
    public void setVy(double vy) { this.vy = vy; }
    public void setPrevX(double prevX) { this.prevX = prevX; }
    public void setPrevY(double prevY) { this.prevY = prevY; }
    public void setSemiMajorAxis(double semiMajorAxis) { 
        this.semiMajorAxis = semiMajorAxis; 
    }
    public void setEccentricity(double eccentricity) { 
        this.eccentricity = eccentricity; 
    }

    @Override
    public String toString() {
        return String.format("%s[mass=%.2e, x=%.2e, y=%.2e]", name, mass, x, y);
    }
}