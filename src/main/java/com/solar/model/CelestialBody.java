package com.solar.model;

public class CelestialBody {
    private String name;
    private double mass;
    private double x, y;
    private double vx, vy;
    private int radius;
    private String color;
    private double semiMajorAxis;
    private double eccentricity;
    
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
    }

    // Getters and Setters
    public String getName() { return name; }
    public double getMass() { return mass; }
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public double getVx() { return vx; }
    public void setVx(double vx) { this.vx = vx; }
    public double getVy() { return vy; }
    public void setVy(double vy) { this.vy = vy; }
    public int getRadius() { return radius; }
    public String getColor() { return color; }
    public double getSemiMajorAxis() { return semiMajorAxis; }
    public void setSemiMajorAxis(double semiMajorAxis) { this.semiMajorAxis = semiMajorAxis; }
    public double getEccentricity() { return eccentricity; }
    public void setEccentricity(double eccentricity) { this.eccentricity = eccentricity; }
}