package objectsToDraw;

import java.awt.*;

import static main.Game.GAME_HEIGHT;
import static main.Game.GAME_WIDTH;

public class CircleConstraints {
    private int x;
    private int y;
    private int radius;

    public CircleConstraints(int x, int y, int radius){
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getRadius() {
        return radius;
    }


    public void draw(Graphics g){
        g.setColor(Color.gray);
        g.fillOval((x - radius), (y - radius), 2 * (int)radius, 2 * radius);
    }
}
