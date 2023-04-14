package main;

import addons.TimeCounter;

import javax.swing.*;
import java.awt.*;

public class InfoPanel extends JPanel {

    private final Game game;
    private float physics_time;
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
        g.drawString("Physics time: " + physics_time + " ms", 10, 30);
        g.drawString("Render time: " + render_time + " ms", 10, 50);
        g.drawString("Balls count: " + ballCount, 10, 70);
    }
}
