package objectsToDraw;

import java.awt.*;

public class Circle {
    private double x_cur;
    private double y_cur;

    private double x_old;
    private double y_old;

    private double radius;

    private double accX;
    private double accY;

    private double velX;
    private double velY;

    private Color color;

    public Circle(double x, double y, double radius, Color color) {
        velX = 0;
        velY = 0;

        x_cur = x;
        y_cur = y;

        x_old = x;
        y_old = y;

        this.radius = radius;

        accX = 0;
        accY = 100;

        this.color = color;
    }

    public void update_position(double dt) {
        velX = x_cur - x_old;
        velY = y_cur - y_old;

        x_old = x_cur;
        y_old = y_cur;

        x_cur += velX + (accX - velX * 200) * dt * dt;
        y_cur += velY + (accY - velY * 200) * dt * dt;

        accX = 0;
        accY = 0;
    }

    public void accelerate(double accX, double accY) {
        this.accX += accX;
        this.accY += accY;
    }


    public double getX_cur() {
        return x_cur;
    }

    public double getY_cur() {
        return y_cur;
    }

    public double getRadius() {
        return radius;
    }

    public void setX_cur(double x_cur) {
        this.x_cur = x_cur;
    }

    public void setY_cur(double y_cur) {
        this.y_cur = y_cur;
    }


    public void draw(Graphics g){
        g.setColor(color);
        g.fillOval((int)(x_cur - radius), (int)(y_cur - radius), (int)radius * 2, (int)radius * 2);

    }

    public String getColor(){
        return color.getRed() + " " + color.getGreen() + " " + color.getBlue();
    }
}
