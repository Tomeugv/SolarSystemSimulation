package com.solar.model;

/**
 * Classe que representa un cos celeste (planeta, estrella, etc.) en un sistema solar.
 */
public class CelestialBody {
    // Propietats bàsiques
    private final String name;    
    private final double mass;    
    private double x, y;          
    private double vx, vy;        
    private double prevX, prevY;    
    private final int radius;      
    private final String color;    
    
    // Paràmetres orbitals
    private double semiMajorAxis;  
    private double eccentricity;    

    /**
     * Constructor del cos.
     */
    public CelestialBody(String name, double mass, double x, double y, 
                        double vx, double vy, int radius, String color) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nom no pot ser null o buit");
        }
        this.name = name.trim();
        this.mass = mass;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.radius = radius;
        this.color = color;
        
        // Inicialitza la posició anterior assumint 1 hora abans
        this.prevX = x - vx * 3600;
        this.prevY = y - vy * 3600;
    }


    // GETTERS


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

    // SETTERS
    
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setVx(double vx) { this.vx = vx; }
    public void setVy(double vy) { this.vy = vy; }
    public void setPrevX(double prevX) { this.prevX = prevX; }
    public void setPrevY(double prevY) { this.prevY = prevY; }
    
    /** Defineix el semieix major de l'òrbita */
    public void setSemiMajorAxis(double semiMajorAxis) { 
        this.semiMajorAxis = semiMajorAxis; 
    }
    
    /** Defineix l'excentricitat de l'òrbita */
    public void setEccentricity(double eccentricity) { 
        this.eccentricity = eccentricity; 
    }

    /**
     * Representació textual del cos celeste.

     */
    @Override
    public String toString() {
        return String.format("%s[mass=%.2e, x=%.2e, y=%.2e]", name, mass, x, y);
    }
}
