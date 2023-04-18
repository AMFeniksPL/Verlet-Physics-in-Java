package main;

import addons.TimeCounter;

import javax.swing.*;
import java.awt.*;

public class InfoPanel extends JPanel {

    private final Game game;
    private float physics_time[];
    private float render_time;
    private int ballCount;

    long lastTime;
    public InfoPanel(Game game) {

        this.game = game;

        Dimension size = new Dimension(200, 900);
        setMinimumSize(size);
        setPreferredSize(size);
        setMaximumSize(size);
        setBackground(Color.GRAY);

        lastTime = System.currentTimeMillis();
    }

    public void getInfoFromGame(){
        physics_time =  game.getGamePanel().getPhysics_time();
        render_time = game.getGamePanel().getRender_time();
        ballCount = game.getGamePanel().getBallCount();
    }

    public void paintComponent(Graphics g){
        if (System.currentTimeMillis() - lastTime > 100){
            getInfoFromGame();
            lastTime = System.currentTimeMillis();
        }


        super.paintComponent(g);

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.DIALOG,  Font.PLAIN, 15));
        g.drawString("Add to grid time: " + physics_time[0] + " ms", 10, 30);
        g.drawString("Collision time: " + physics_time[1] + " ms", 10, 50);
        g.drawString("Apply Forces time: " + physics_time[2] + " ms", 10, 70);
        g.drawString("Update positions: " + physics_time[3] + " ms", 10, 90);
        g.drawString("Render time: " + render_time + " ms", 10, 110);
        g.drawString("Balls count: " + ballCount, 10, 130);
    }
}
